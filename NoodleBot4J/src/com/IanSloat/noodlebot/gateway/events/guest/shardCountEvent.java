package com.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.events.Event;
import com.IanSloat.noodlebot.gateway.sessions.Session;

// TODO Document class
public class shardCountEvent extends Event {

	public shardCountEvent(Session conn, JSONObject message) {
		super(conn, message);
	}
	
	public int getShardCount() {
		return this.getShardManager().getShardsTotal();
	}

}
