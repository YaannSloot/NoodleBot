package main.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.gateway.events.Event;
import main.IanSloat.noodlebot.gateway.sessions.Session;

public class motdRequestEvent extends Event {

	public motdRequestEvent(Session conn, JSONObject message) {
		super(conn, message);
	}

	public String getMOTD() {
		return NoodleBotMain.devMsg;
	}
	
}
