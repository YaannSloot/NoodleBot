package com.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import com.IanSloat.noodlebot.NoodleBotMain;
import com.IanSloat.noodlebot.gateway.events.Event;
import com.IanSloat.noodlebot.gateway.sessions.Session;

/**
 * An event representing a request for the bot's MOTD message
 */
public class MotdRequestEvent extends Event {

	/**
	 * Constructs a new {@linkplain MotdRequestEvent}
	 * 
	 * @param conn    The {@linkplain Session} where this event was fired
	 * @param message The message that triggered this event
	 */
	public MotdRequestEvent(Session conn, JSONObject message) {
		super(conn, message);
	}

	/**
	 * Retrieves the bot's MOTD message
	 * 
	 * @return The bot's MOTD message
	 */
	public String getMOTD() {
		return NoodleBotMain.devMsg;
	}

}
