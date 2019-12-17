package com.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import com.IanSloat.noodlebot.NoodleBotMain;
import com.IanSloat.noodlebot.gateway.events.Event;
import com.IanSloat.noodlebot.gateway.sessions.Session;

/**
 * An event representing a request for the bot's current version number
 */
public class VersionEvent extends Event {

	/**
	 * Constructs a new {@linkplain VersionEvent}
	 * 
	 * @param conn    The {@linkplain Session} where this event was fired
	 * @param message The message that triggered this event
	 */
	public VersionEvent(Session conn, JSONObject message) {
		super(conn, message);
	}

	/**
	 * Retrieves the bot's current version
	 * 
	 * @return The bot's current version
	 */
	public String getBotVersion() {
		return NoodleBotMain.botVersion;
	}

}
