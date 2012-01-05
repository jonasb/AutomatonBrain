package se.forskningsavd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private Communicator mCommunicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCommunicator = new Communicator();

        LinearLayout layout = new LinearLayout(this);

        Button button = new Button(this);
        button.setText("Connect");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCommunicator.connect();
            }
        });
        layout.addView(button);

        Button helloWorld = new Button(this);
        helloWorld.setText("Hello World");
        helloWorld.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCommunicator.sendText("Hello world");
            }
        });
        layout.addView(helloWorld);

        setContentView(layout);
    }

    @Override
    protected void onPause() {
        mCommunicator.disconnect();
        super.onPause();
    }
}
