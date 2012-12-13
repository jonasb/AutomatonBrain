package se.forskningsavd.automatonbrain;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;

import android.net.Uri;

public class Robot {
	public String HOST;
	public Integer PORT;
	public String NAME;

	public Robot(String name, String host, int port) {
		HOST = host;
		PORT = port;
		NAME = name;
	}

	public Robot(Uri uri) throws ParseException {
		if (uri.getScheme().equals("http") && uri.getAuthority().equals("wiki.forskningsavd.se"))
		{
			List<String> parts = uri.getPathSegments();
			if (parts.get(0).equals("RoboCortex"))
			{
				List<String> other_parts = Arrays.asList(uri.getFragment().split("/"));
				HOST = other_parts.get(0);
				PORT = Integer.parseInt(other_parts.get(1));
				NAME = other_parts.get(2);
			}
			else
			{
				throw new ParseException("Bad path: " + parts.get(0),0);
			}
		}
		else
		{
			throw new ParseException("Bad Scheme or host",0);
		}
	}

	public Uri toUri() {
		return Uri.parse(String.format(Locale.ENGLISH, "http://wiki.forskningsavd.se/RoboCortex#%s/%d/%s",
				HOST, PORT, NAME));
	}

	public Boolean equals(Robot r) {
		return NAME.equals(r.NAME) && HOST.equals(r.HOST)
				&& PORT.equals(r.PORT);
	}

	@Override
	public int hashCode() {
		return NAME.hashCode() + HOST.hashCode() + PORT;
	}

	@Override
	public String toString() {
		return NAME;
	}
}
