package se.forskningsavd;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

        final Navigator nav = new Navigator();
        final Bitmap target = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888);
        mCommunicator = new Communicator(nav, target, new Communicator.Callback() {
            public void onTargetImageChanged(Bitmap bitmap) {
                video.invalidate();
            }
        });

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final Button button = new Button(this);
        button.setText("Connect");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCommunicator.connect();
                button.setVisibility(View.GONE);
            }
        });
        layout.addView(button);

        final Button helloWorld = new Button(this);
        helloWorld.setText("Automaton brain");
        helloWorld.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCommunicator.sendText("/voice");
                mCommunicator.sendText("Automaton brain");
            }
        });
        layout.addView(helloWorld);

        final FrameLayout frame = new FrameLayout(this);

        video.setImageBitmap(target);
        frame.addView(video);

        final NavigationView navigationView = new NavigationView(this, nav);
        frame.addView(navigationView);

        layout.addView(frame, MATCH_PARENT, MATCH_PARENT);

        setContentView(layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_light) {
            mCommunicator.sendText("/light");
        } else {
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        mCommunicator.disconnect();
        super.onPause();
    }
}
