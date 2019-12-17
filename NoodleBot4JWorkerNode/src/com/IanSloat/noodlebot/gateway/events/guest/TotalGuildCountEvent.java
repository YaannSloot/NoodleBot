package com.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.events.Event;
import com.IanSloat.noodlebot.gateway.sessions.Session;

/**
 * An event representing a request for the total amount of guilds the bot is
 * connected to
 */
public class TotalGuildCountEvent extends Event {

	/**
	 * Constructs a new {@linkplain TotalGuildCountEvent}
	 * 
	 * @param conn    The {@linkplain Session} where this event was fired
	 * @param message The message that triggered this event
	 */
	public TotalGuildCountEvent(Session conn, JSONObject message) {
		super(conn, message);
	}

	/**
	 * Retrieves the total amount of guilds that the bot is currently connected to
	 * 
	 * @return The total amount of guilds that the bot is currently connected to
	 */
	public int getGuildCount() {
		return this.getShardManager().getGuilds().size();
	}

}
