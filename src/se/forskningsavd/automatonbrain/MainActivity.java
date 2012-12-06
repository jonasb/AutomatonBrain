package se.forskningsavd.automatonbrain;

import java.text.ParseException;
import java.util.List;

import se.forskningsavd.automatonbrain.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.net.Uri;

import com.actionbarsherlock.app.SherlockActivity;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends SherlockActivity {
    private Communicator mCommunicator;
    private String mLastMessage;
    public static final String PREFS_NAME = "ConnectionPreferences";
    private Settings mSettings;

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

        // Restore preferences
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        mSettings = new Settings(settings);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final Spinner spinner = new Spinner(this);
		Uri data = getIntent().getData();
		if (data != null)
		{
			try {
				Robot r = new Robot(data);
				mSettings.AddRobot(r);
				mCommunicator.connect(r);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			List<Robot> list = mSettings.GetRobotList();
	        ArrayAdapter<Robot> dataAdapter = new ArrayAdapter<Robot>(this,
	                android.R.layout.simple_spinner_item, list);
	        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinner.setAdapter(dataAdapter);
	        layout.addView(spinner);
	        
	        final Button button = new Button(this);
	        button.setText("Connect!");
	        button.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View view) {
	                mCommunicator.connect((Robot) spinner.getSelectedItem());
	                button.setVisibility(View.GONE);
	                spinner.setVisibility(View.GONE);
	            }
	        });
	        layout.addView(button);
		}

        final FrameLayout frame = new FrameLayout(this);

        video.setImageBitmap(target);
        frame.addView(video);

        final NavigationView navigationView = new NavigationView(this, nav);
        frame.addView(navigationView);

        layout.addView(frame, MATCH_PARENT, MATCH_PARENT);

        setContentView(layout);
    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        final com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_light:
            mCommunicator.sendText("/light");
            return true;
        case R.id.menu_mirror:
            mCommunicator.sendText("/mirror");
            return true;
        case R.id.menu_speak:
            speak();
            return true;
        default:
            return false;
        }
    }

    private void speak() {
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setCanceledOnTouchOutside(true);

        // editor
        final EditText message = new EditText(this);
        message.setText(mLastMessage);
        message.setSelectAllOnFocus(true);
        message.setSingleLine();
        message.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        message.setImeOptions(EditorInfo.IME_ACTION_DONE);
        message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });
        dialog.setView(message);

        // speak button
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Speaketh", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                final String msg = message.getText().toString();
                if (msg.length() > 0) {
                    mCommunicator.sendText(msg);
                    MainActivity.this.mLastMessage = msg;
                }
            }
        });

        // show
        dialog.show();
    }

    @Override
    protected void onPause() {
        mCommunicator.disconnect();
        super.onPause();
    }
}
