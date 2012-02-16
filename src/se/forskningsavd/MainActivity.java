package se.forskningsavd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends Activity {
    private Communicator mCommunicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Navigator nav = new Navigator();
        mCommunicator = new Communicator(nav);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

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

        NavigationView navigationView = new NavigationView(this, nav);
        LayoutParams p = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layout.addView(navigationView, p);

        setContentView(layout);
    }

    @Override
    protected void onPause() {
        mCommunicator.disconnect();
        super.onPause();
    }
}
