package com.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.events.Event;
import com.IanSloat.noodlebot.gateway.sessions.Session;

/**
 * An event representing a request for the bot's total shard count
 */
public class ShardCountEvent extends Event {

	/**
	 * Constructs a new {@linkplain ShardCountEvent}
	 * 
	 * @param conn    The {@linkplain Session} where this event was fired
	 * @param message The message that triggered this event
	 */
	public ShardCountEvent(Session conn, JSONObject message) {
		super(conn, message);
	}

	/**
	 * Retrieves the bot's total amount of shards currently running
	 * 
	 * @return The bot's total amount of shards currently running
	 */
	public int getShardCount() {
		return this.getShardManager().getShardsTotal();
	}

}
