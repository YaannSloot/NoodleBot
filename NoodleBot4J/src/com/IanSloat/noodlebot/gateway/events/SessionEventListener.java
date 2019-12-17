package com.IanSloat.noodlebot.gateway.events;

import javax.annotation.Nonnull;

public interface SessionEventListener {

	/**
	 * Broadcasts an event to the event listener
	 * @param e The event to broadcast
	 */
	public void broadcastEvent(@Nonnull Event e);
	
}
