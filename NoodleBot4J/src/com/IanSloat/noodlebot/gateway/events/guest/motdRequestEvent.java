package com.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import com.IanSloat.noodlebot.NoodleBotMain;
import com.IanSloat.noodlebot.gateway.events.Event;
import com.IanSloat.noodlebot.gateway.sessions.Session;

// TODO Document class
public class motdRequestEvent extends Event {

	public motdRequestEvent(Session conn, JSONObject message) {
		super(conn, message);
	}

	public String getMOTD() {
		return NoodleBotMain.devMsg;
	}
	
}
