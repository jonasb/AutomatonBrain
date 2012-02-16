package se.forskningsavd;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

class Communicator {
    static class ReceiverThread extends Thread {
        private final DatagramSocket mSocket;
        private boolean mRunning = true;
        private final SenderThread mSenderThread;

        public ReceiverThread(DatagramSocket socket, SenderThread senderThread) {
            mSocket = socket;
            mSenderThread = senderThread;
        }

        @Override
        public void run() {
            byte[] buf = new byte[1000];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            while (mRunning) {
                try {
                    mSocket.receive(dp);
                    StringBuilder debug = new StringBuilder();
                    for (int i = 0; i < dp.getLength(); i++) {
                        debug.append(String.format("%02x", dp.getData()[i]));
                    }
                    String rcvd = "rcvd from " + dp.getAddress() + ", " + dp.getPort() + ": "
                            + debug;
                    Log.d("XXX", rcvd);

                    ByteBuffer data = ByteBuffer.wrap(dp.getData());
                    if (data.get(0) == 'D' &&
                            data.get(1) == 'A' &&
                            data.get(2) == 'T' &&
                            data.get(3) == 'A') {
                        if (data.array().length >= 4 + 6) {
                            // TODO signed vs unsigned
                            final byte trustServer = data.get(4);
                            final byte trustClient = data.get(5);
                            int timer = data.getInt(6);
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
            InetAddress hostAddress;
            try {
                hostAddress = InetAddress.getByName(Settings.HOST);
                final String message = "HELO";
                byte[] buf = message.getBytes();

                DatagramPacket out = new DatagramPacket(buf, buf.length, hostAddress, Settings.PORT);
                mSocket.send(out);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

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
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.put("CTRL".getBytes());
                buffer.put(mTrustServer);
                buffer.put(mTrustClient);
                buffer.put((byte) 0); //padding
                buffer.put((byte) 0); //padding
                buffer.putInt(0); //mx
                buffer.putInt(0); //my
                buffer.putInt(0); //dx
                buffer.putInt(0); //dy
                buffer.put(kb);
                buffer.put((byte) 0); //padding
                buffer.put((byte) 0); //padding
                buffer.put((byte) 0); //padding

                synchronized (mTrustMessages) {
                    if (mTrustTimeout == 0) {
                        if (mTrustMessages.size() > 0) {
                            Log.d("XXX", "add trust message");
                            final byte[] msg = mTrustMessages.get(0);
                            buffer.put(msg);
                            mTrustTimeout = TIMEOUT_TRUST;
                        }
                    } else {
                        mTrustTimeout--;
                    }
                }

                DatagramPacket out = new DatagramPacket(buffer.array(), buffer.position(), hostAddress, 6979);
                try {
                    mSocket.send(out);
                    StringBuilder debug = new StringBuilder();
                    for (int i = 0; i < buffer.position(); i++) {
                        debug.append(String.format("%02x", buffer.get(i)));
                    }
                    Log.d("XXX", "sending " + debug.toString());
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

        public void abort() {
            mRunning = false;
        }

        public void onServerData(byte trustServer, byte trustClient) {
            synchronized (mTrustMessages) {
                if (!mTrustMessages.isEmpty()) {
                    Log.d("XXX", "trustClient : " + trustClient + ", mTrustClient: " + mTrustClient);
                    if (trustClient == mTrustClient) {
                        mTrustMessages.remove(0);
                        mTrustClient++;
                        mTrustTimeout = 0;
                    }
                }
            }
        }

        public void addTrustedMessage(byte[] ident, byte[] message) {
            ByteBuffer buf = ByteBuffer.allocate(ident.length + 1 + message.length);
            buf.put(ident);
            buf.put((byte) message.length);
            buf.put(message);
            Log.d("XXX", "addTrustedMessage: '" + new String(buf.array(), 0, buf.position()) + "'");

            synchronized (mTrustMessages) {
                mTrustMessages.add(buf.array());
            }
        }
    }

    private ReceiverThread mReceiverThread;
    private SenderThread mSenderThread;
    private final Navigator mNavigator;

    public Communicator(Navigator nav) {
        mNavigator = nav;
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

        mReceiverThread = new ReceiverThread(socket, mSenderThread);
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
