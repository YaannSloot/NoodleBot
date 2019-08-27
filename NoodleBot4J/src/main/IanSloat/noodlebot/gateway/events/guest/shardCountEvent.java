package main.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import main.IanSloat.noodlebot.gateway.events.Event;
import main.IanSloat.noodlebot.gateway.sessions.Session;

public class shardCountEvent extends Event {

	public shardCountEvent(Session conn, JSONObject message) {
		super(conn, message);
	}
	
	public int getShardCount() {
		return this.getShardManager().getShardsTotal();
	}

}
