package se.forskningsavd;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends Activity {
    private Communicator mCommunicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ImageView video = new ImageView(this);

        Navigator nav = new Navigator();
        Bitmap target = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888);
        mCommunicator = new Communicator(nav, target, new Communicator.Callback() {
            public void onTargetImageChanged(Bitmap bitmap) {
                video.invalidate();
            }
        });

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
        helloWorld.setText("Automaton brain");
        helloWorld.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCommunicator.sendText("/voice");
                mCommunicator.sendText("Automaton brain");
            }
        });
        layout.addView(helloWorld);

        FrameLayout frame = new FrameLayout(this);

        video.setImageBitmap(target);
        frame.addView(video);

        NavigationView navigationView = new NavigationView(this, nav);
        frame.addView(navigationView);

        layout.addView(frame, MATCH_PARENT, MATCH_PARENT);

        setContentView(layout);
    }

    @Override
    protected void onPause() {
        mCommunicator.disconnect();
        super.onPause();
    }
}
