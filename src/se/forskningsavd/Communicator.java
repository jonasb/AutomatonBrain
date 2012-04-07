package se.forskningsavd;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

class Communicator {
    private static final String SEND_TAG = "Sending";
    private static final String RECEIVE_TAG = "Receiving";
    private static final boolean DEBUG_SENDING = false;
    private static final boolean DEBUG_RECEIVING = false;

    public interface Callback {
        public void onTargetImageChanged(Bitmap bitmap);
    }

    static class ReceiverThread extends Thread {
        private final DatagramSocket mSocket;
        private boolean mRunning = true;
        private final SenderThread mSenderThread;
        private final Decoder mDecoder;
        private final Bitmap mTargetBitmap;
        private final Handler mHandler;

        public ReceiverThread(DatagramSocket socket, SenderThread senderThread, Decoder decoder, Bitmap targetBitmap, Handler handler) {
            mSocket = socket;
            mSenderThread = senderThread;
            mDecoder = decoder;
            mTargetBitmap = targetBitmap;
            mHandler = handler;
        }

        @Override
        public void run() {
            final byte[] buf = new byte[3000];
            while (mRunning) {
                try {
                    final DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    mSocket.receive(dp);
                    if (DEBUG_RECEIVING) {
                        final StringBuilder debug = new StringBuilder();
                        final int length = dp.getLength();
                        if (length < 1000) {
                            for (int i = 0; i < length; i++) {
                                debug.append(String.format("%02x", dp.getData()[i]));
                            }
                        }
                        final String rcvd = "rcvd from " + dp.getAddress() + ", " + dp.getPort() + ", " + length + " bytes : "
                                + debug;
                        Log.d(RECEIVE_TAG, rcvd);
                    }

                    final ByteBuffer data = ByteBuffer.wrap(dp.getData());
                    if (data.get(0) == 0 &&
                            data.get(1) == 0 &&
                            data.get(2) == 0 &&
                            data.get(3) == 1) {
                        mDecoder.decode(data.array(), mTargetBitmap);
                        mHandler.sendEmptyMessage(0);
                    } else if (data.get(0) == 'D' &&
                            data.get(1) == 'A' &&
                            data.get(2) == 'T' &&
                            data.get(3) == 'A') {
                        if (data.array().length >= 4 + 6) {
                            // TODO signed vs unsigned
                            final byte trustServer = data.get(4);
                            final byte trustClient = data.get(5);
                            final int timer = data.getInt(6);
                            mSenderThread.onServerData(trustServer, trustClient);

                            //TODO handle server trusted data
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void abort() {
            mRunning = false;
        }
    }

    static class SenderThread extends Thread {
        /**
         * Before retransmitting trusted packets
         */
        private static final int TIMEOUT_TRUST = 16;
        private final DatagramSocket mSocket;
        private final Navigator mNavigator;
        private boolean mRunning = true;
        private byte mTrustServer;
        private byte mTrustClient;
        private final ArrayList<byte[]> mTrustMessages = new ArrayList<byte[]>();
        private int mTrustTimeout;

        public SenderThread(DatagramSocket socket, Navigator navigator) {
            mSocket = socket;
            mNavigator = navigator;
        }

        @Override
        public void run() {
            final InetAddress hostAddress;
            try {
                hostAddress = InetAddress.getByName(Settings.HOST);
                final String message = "HELO";
                final byte[] buf = message.getBytes();

                final DatagramPacket out = new DatagramPacket(buf, buf.length, hostAddress, Settings.PORT);
                mSocket.send(out);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            float rotationCumulative = 0;
            float cameraAngleCumulative = 0;
            while (mRunning) {
                byte kb = 0;
                if (mNavigator.left)
                    kb |= 1;
                if (mNavigator.right)
                    kb |= 1 << 1;
                if (mNavigator.up)
                    kb |= 1 << 2;
                if (mNavigator.down)
                    kb |= 1 << 3;
                //TODO reuse buffer/modify the data
                final ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.put("CTRL".getBytes());
                buffer.put(mTrustServer);
                buffer.put(mTrustClient);
                buffer.put((byte) 0); //padding
                buffer.put((byte) 0); //padding

                rotationCumulative += mNavigator.rotation * 20;
                cameraAngleCumulative += mNavigator.cameraAngle * 10;

                buffer.putInt(closestZero(rotationCumulative)); //mx
                buffer.putInt(closestZero(cameraAngleCumulative)); //my
                buffer.putInt((int) (mNavigator.moveX * 128)); //dx -1..1 -> -127..127
                buffer.putInt((int) (mNavigator.moveY * 128)); //dy -1..1 -> -127..127
                buffer.put(kb);
                buffer.put((byte) 0); //padding
                buffer.put((byte) 0); //padding
                buffer.put((byte) 0); //padding

                synchronized (mTrustMessages) {
                    if (mTrustTimeout == 0) {
                        if (mTrustMessages.size() > 0) {
                            if (DEBUG_SENDING) {
                                Log.d(SEND_TAG, "add trust message");
                            }
                            final byte[] msg = mTrustMessages.get(0);
                            buffer.put(msg);
                            mTrustTimeout = TIMEOUT_TRUST;
                        }
                    } else {
                        mTrustTimeout--;
                    }
                }

                final DatagramPacket out = new DatagramPacket(buffer.array(), buffer.position(), hostAddress, Settings.PORT);
                try {
                    mSocket.send(out);
                    if (DEBUG_SENDING) {
                        final StringBuilder debug = new StringBuilder();
                        for (int i = 0; i < buffer.position(); i++) {
                            debug.append(String.format("%02x", buffer.get(i)));
                        }
                        Log.d(SEND_TAG, "sending " + debug.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }

        private int closestZero(float v) {
            return (int) (v < 0 ? Math.ceil(v) : Math.floor(v));
        }

        public void abort() {
            mRunning = false;
        }

        public void onServerData(byte trustServer, byte trustClient) {
            synchronized (mTrustMessages) {
                if (!mTrustMessages.isEmpty()) {
                    if (DEBUG_RECEIVING) {
                        Log.d(RECEIVE_TAG, "trustClient : " + trustClient + ", mTrustClient: " + mTrustClient);
                    }
                    if (trustClient == mTrustClient) {
                        mTrustMessages.remove(0);
                        mTrustClient++;
                        mTrustTimeout = 0;
                    }
                }
            }
        }

        public void addTrustedMessage(byte[] ident, byte[] message) {
            final ByteBuffer buf = ByteBuffer.allocate(ident.length + 1 + message.length);
            buf.put(ident);
            buf.put((byte) message.length);
            buf.put(message);
            if (DEBUG_SENDING) {
                Log.d(SEND_TAG, "addTrustedMessage: '" + new String(buf.array(), 0, buf.position()) + "'");
            }

            synchronized (mTrustMessages) {
                mTrustMessages.add(buf.array());
            }
        }
    }

    private ReceiverThread mReceiverThread;
    private SenderThread mSenderThread;
    private final Navigator mNavigator;
    private final Bitmap mTargetBitmap;
    private final Callback mCallback;

    public Communicator(Navigator nav, Bitmap target, Callback callback) {
        mNavigator = nav;
        mTargetBitmap = target;
        mCallback = callback;
    }

    public void connect() {
        disconnect();

        final DatagramSocket socket;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        mSenderThread = new SenderThread(socket, mNavigator);
        mSenderThread.start();

        final Decoder d = new Decoder();
        d.init();

        final Handler handler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message message) {
                mCallback.onTargetImageChanged(mTargetBitmap);
                return false;
            }
        });
        mReceiverThread = new ReceiverThread(socket, mSenderThread, d, mTargetBitmap, handler);
        mReceiverThread.start();
    }

    public void sendText(String message) {
        mSenderThread.addTrustedMessage("KIWI".getBytes(), message.toUpperCase().getBytes());
    }

    public void disconnect() {
        if (mReceiverThread != null) {
            mReceiverThread.abort();
            mReceiverThread = null;
        }
        if (mSenderThread != null) {
            mSenderThread.abort();
            mSenderThread = null;
        }
    }
}
