package com.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.events.Event;
import com.IanSloat.noodlebot.gateway.sessions.Session;

// TODO Document class
public class threadCountEvent extends Event {

	public threadCountEvent(Session conn, JSONObject message) {
		super(conn, message);
	}
	
	public int getThreadCount() {
		return Thread.activeCount();
	}

}
