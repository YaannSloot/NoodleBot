package com.IanSloat.noodlebot.gateway.events;

import javax.annotation.Nonnull;

public interface SessionEventListener {

	/**
	 * Broadcasts an event to the event listener
	 * @param e
	 */
	public void broadcastEvent(@Nonnull Event e);
	
}
