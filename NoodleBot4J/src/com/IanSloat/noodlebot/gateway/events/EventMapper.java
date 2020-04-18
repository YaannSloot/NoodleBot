package com.IanSloat.noodlebot.gateway.events;

import javax.annotation.Nonnull;

import com.IanSloat.noodlebot.gateway.events.guest.MotdRequestEvent;
import com.IanSloat.noodlebot.gateway.events.guest.ShardCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.ShardStatRequestEvent;
import com.IanSloat.noodlebot.gateway.events.guest.ThreadCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.TotalGuildCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.VersionEvent;

/**
 * An abstract class that contains a mapping of every server event to be
 * implemented by specific session event listeners.
 */
public class EventMapper implements SessionEventListener {

	public void broadcastEvent(@Nonnull Event e) {

		// Guest event mappings
		if (e instanceof ShardCountEvent)
			onShardCountEvent((ShardCountEvent) e);
		else if (e instanceof ThreadCountEvent)
			onThreadCountEvent((ThreadCountEvent) e);
		else if (e instanceof TotalGuildCountEvent)
			onTotalGuildCountEvent((TotalGuildCountEvent) e);
		else if (e instanceof VersionEvent)
			onVersionEvent((VersionEvent) e);
		else if (e instanceof MotdRequestEvent)
			onMotdRequestEvent((MotdRequestEvent) e);
		else if (e instanceof ShardStatRequestEvent)
			onShardStatRequestEvent((ShardStatRequestEvent) e);

		// Fire generic event if no matches for other events
		else
			onEvent(e);

	}

	// Generic event
	/**
	 * Called when the event broadcasted is a generic event
	 * 
	 * @param event The event that was broadcasted
	 */
	public void onEvent(Event event) {
	}

	// Guest session events
	/**
	 * Called when the event broadcasted is a {@linkplain ShardCountEvent}
	 * 
	 * @param event The event that was broadcasted
	 */
	public void onShardCountEvent(ShardCountEvent event) {
	}

	/**
	 * Called when the event broadcasted is a {@linkplain ThreadCountEvent}
	 * 
	 * @param event The event that was broadcasted
	 */
	public void onThreadCountEvent(ThreadCountEvent event) {
	}

	/**
	 * Called when the event broadcasted is a {@linkplain TotalGuildCountEvent}
	 * 
	 * @param event The event that was broadcasted
	 */
	public void onTotalGuildCountEvent(TotalGuildCountEvent event) {
	}

	/**
	 * Called when the event broadcasted is a {@linkplain VersionEvent}
	 * 
	 * @param event The event that was broadcasted
	 */
	public void onVersionEvent(VersionEvent event) {
	}

	/**
	 * Called when the event broadcasted is a {@linkplain MotdRequestEvent}
	 * 
	 * @param event The event that was broadcasted
	 */
	public void onMotdRequestEvent(MotdRequestEvent event) {
	}

	/**
	 * Called when the event broadcasted is a {@linkplain ShardStatRequestEvent}
	 * 
	 * @param event The event that was broadcasted
	 */
	public void onShardStatRequestEvent(ShardStatRequestEvent event) {
	}

}
