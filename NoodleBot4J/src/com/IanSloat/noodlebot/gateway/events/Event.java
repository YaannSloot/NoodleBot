package com.IanSloat.noodlebot.gateway.events;

import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.sessions.Session;

import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * A generic gateway event
 */
public class Event {

	private ShardManager shardmgr;
	private JSONObject message;

	private Session conn;

	/**
	 * Constructs a new generic event
	 * 
	 * @param conn    The {@linkplain Session} where this event was fired
	 * @param message The message that triggered this event
	 */
	public Event(Session conn, JSONObject message) {
		this.conn = conn;
		this.shardmgr = conn.getShardManager();
		this.message = message;
	}

	/**
	 * Retrieves the {@linkplain Session} where this event was fired
	 * 
	 * @return The {@linkplain Session} associated with this event
	 */
	public Session getSession() {
		return this.conn;
	}

	/**
	 * Retrieves the {@linkplain ShardManager} associated with this event
	 * 
	 * @return The {@linkplain ShardManager} associated with this event
	 */
	public ShardManager getShardManager() {
		return this.shardmgr;
	}

	/**
	 * Retrieves the message that triggered this event
	 * 
	 * @return A {@linkplain} JSONObject representing the message that fired this
	 *         event
	 */
	public JSONObject getMessageJSON() {
		return message;
	}

}
