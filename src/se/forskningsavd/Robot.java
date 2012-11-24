package se.forskningsavd;

import java.util.Locale;

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

	public Robot(Uri uri) {
		NAME = uri.getPath().substring(1);
		HOST = uri.getHost();
		PORT = uri.getPort();
	}

	public Uri toUri() {
		return Uri.parse(String.format(Locale.ENGLISH, "robocortex://%s:%d/%s",
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