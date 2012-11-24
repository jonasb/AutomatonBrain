package se.forskningsavd.automatonbrain;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import android.net.Uri;
import android.content.SharedPreferences;

final class Settings {
	private SharedPreferences mPrefs;
	private List<Robot> mRobots;

	public Settings(SharedPreferences prefs) {
		mPrefs = prefs;
		mRobots = new ArrayList<Robot>();
		Set<String> default_robots = new LinkedHashSet<String>();
		default_robots.add("robocortex://kiwiray1:6979/kiwi_wifi");
		Set<String> robot_uris = mPrefs.getStringSet("robots", default_robots);
		// get the Iterator
		Iterator<String> itr = robot_uris.iterator();
		while (itr.hasNext()) {
			Uri r = Uri.parse(itr.next());
			mRobots.add(new Robot(r));
		}
	}

	public void AddRobot(Uri robotdata) {
		Robot r = new Robot(robotdata);
		if (mRobots.get(0).equals(r))
			return;

		Iterator<Robot> itr = mRobots.iterator();
		while (itr.hasNext()) {
			if (itr.next().equals(r)) {
				itr.remove();
				break;
			}
		}
		mRobots.add(0, r);

		Set<String> set_robots = new LinkedHashSet<String>();
		{
			itr = mRobots.iterator();
			while (itr.hasNext()) {
				String s = itr.next().toUri().toString();
				set_robots.add(s);
			}
		}

		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putStringSet("robots", set_robots);
		editor.commit();
	}

	public List<Robot> GetRobotList() {
		return mRobots;
	}

}
