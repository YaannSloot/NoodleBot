package com.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.events.Event;
import com.IanSloat.noodlebot.gateway.sessions.Session;

/**
 * An event representing a request for the total amount of JVM threads being
 * used to run the bot
 */
public class ThreadCountEvent extends Event {

	/**
	 * Constructs a new {@linkplain ThreadCountEvent}
	 * 
	 * @param conn    The {@linkplain Session} where this event was fired
	 * @param message The message that triggered this event
	 */
	public ThreadCountEvent(Session conn, JSONObject message) {
		super(conn, message);
	}

	/**
	 * Retrieves the total amount of JVM threads being used by the bot to run
	 * 
	 * @return The total amount of JVM threads being used by the bot to run
	 */
	public int getThreadCount() {
		return Thread.activeCount();
	}

}
