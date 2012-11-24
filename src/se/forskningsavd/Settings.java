package se.forskningsavd;

import java.util.List;
import java.util.ArrayList;

final class Settings {

    public static List<Robot> GetRobotList() {
		List<Robot> ret = new ArrayList<Robot>();
		ret.add(new Robot("kiwiray_wifi","kiwiray1",6979));
		// TODO: look up previously known hosts in persistent data
		return ret;
    }

}
