package main.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.gateway.events.Event;
import main.IanSloat.noodlebot.gateway.sessions.Session;

// TODO Document class
public class versionEvent extends Event {

	public versionEvent(Session conn, JSONObject message) {
		super(conn, message);
	}
	
	public String getBotVersion() {
		return NoodleBotMain.botVersion;
	}

}
