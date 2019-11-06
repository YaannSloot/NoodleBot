package main.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import main.IanSloat.noodlebot.gateway.events.Event;
import main.IanSloat.noodlebot.gateway.sessions.Session;

// TODO Document class
public class threadCountEvent extends Event {

	public threadCountEvent(Session conn, JSONObject message) {
		super(conn, message);
	}
	
	public int getThreadCount() {
		return Thread.activeCount();
	}

}
