package com.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.events.Event;
import com.IanSloat.noodlebot.gateway.sessions.Session;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;

/**
 * An event representing a request for the status of a specific shard
 */
public class ShardStatRequestEvent extends Event {

	private JDA shard;

	/**
	 * Constructs a new {@linkplain ShardStatRequestEvent}
	 * 
	 * @param conn    The {@linkplain Session} where this event was fired
	 * @param message The message that triggered this event
	 */
	public ShardStatRequestEvent(Session conn, JSONObject message) {
		super(conn, message);
		this.shard = conn.getShardManager().getShardById(message.getInt("shardid"));
	}

	/**
	 * Retrieves the shard that this event is targeting
	 * 
	 * @return The shard that this event is targeting
	 */
	public JDA getShard() {
		return this.shard;
	}

	/**
	 * Checks whether the targeted shard is online
	 * 
	 * @return True if the shard is online
	 */
	public boolean isShardOnline() {
		return !shard.getStatus().equals(Status.DISCONNECTED);
	}

	/**
	 * Retrieves the amount of guilds that the targeted shard is connected to
	 * 
	 * @return The amount of guilds that the targeted shard is connected to
	 */
	public int getShardGuildCount() {
		return shard.getGuilds().size();
	}

}
