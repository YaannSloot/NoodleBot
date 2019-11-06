package main.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import main.IanSloat.noodlebot.gateway.events.Event;
import main.IanSloat.noodlebot.gateway.sessions.Session;

// TODO Document class
public class totalGuildCountEvent extends Event {

	public totalGuildCountEvent(Session conn, JSONObject message) {
		super(conn, message);
	}

	public int getGuildCount() {
		return this.getShardManager().getGuilds().size();
	}
	
}
