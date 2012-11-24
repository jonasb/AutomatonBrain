package se.forskningsavd;

public class Robot {
	public String HOST;
	public Integer PORT;
	public String NAME;

	public Robot(String name, String host, int port)
	{
		HOST = host;
		PORT = port;
		NAME = name;
	}

	public String toString()
	{
		return NAME;
	}
}