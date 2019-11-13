package com.IanSloat.noodlebot.gateway.events;

import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.sessions.Session;

import net.dv8tion.jda.api.sharding.ShardManager;

public abstract class Event {

	private ShardManager shardmgr;
	private JSONObject message;
	
	private Session conn;
	
	public Event(Session conn, JSONObject message) {
		this.conn = conn;
		this.shardmgr = conn.getShardManager();
		this.message = message;
	}
	
	public Session getSession() {
		return this.conn;
	}
	
	public ShardManager getShardManager() {
		return this.shardmgr;
	}
	
	public JSONObject getMessageJSON() {
		return message;
	}
	
}
