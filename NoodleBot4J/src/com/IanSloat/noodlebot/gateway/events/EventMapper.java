package com.IanSloat.noodlebot.gateway.events;

import javax.annotation.Nonnull;

import com.IanSloat.noodlebot.gateway.events.guest.motdRequestEvent;
import com.IanSloat.noodlebot.gateway.events.guest.shardCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.shardStatRequestEvent;
import com.IanSloat.noodlebot.gateway.events.guest.threadCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.totalGuildCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.versionEvent;

public abstract class EventMapper implements SessionEventListener {

	public void broadcastEvent(@Nonnull Event e) {
		
		// Guest event mappings
		if(e instanceof shardCountEvent)
			onShardCountEvent((shardCountEvent) e);
		else if(e instanceof threadCountEvent)
			onThreadCountEvent((threadCountEvent) e);
		else if(e instanceof totalGuildCountEvent)
			onTotalGuildCountEvent((totalGuildCountEvent) e);
		else if(e instanceof versionEvent)
			onVersionEvent((versionEvent) e);
		else if(e instanceof motdRequestEvent)
			onMotdRequestEvent((motdRequestEvent) e);
		else if(e instanceof shardStatRequestEvent)
			onShardStatRequestEvent((shardStatRequestEvent) e);
		
		// Fire generic event if no matches for other events
		else
			onEvent(e);
			
	}
	
	// Generic event
	public void onEvent(Event event) {}
	
	// Guest session events
	public void onShardCountEvent(shardCountEvent event) {}
	public void onThreadCountEvent(threadCountEvent event) {}
	public void onTotalGuildCountEvent(totalGuildCountEvent event) {}
	public void onVersionEvent(versionEvent event) {}
	public void onMotdRequestEvent(motdRequestEvent event) {}
	public void onShardStatRequestEvent(shardStatRequestEvent event) {}
	
}
