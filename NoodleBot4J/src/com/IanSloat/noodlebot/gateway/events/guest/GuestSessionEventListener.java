package com.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.events.EventMapper;

// TODO Document class
public class GuestSessionEventListener extends EventMapper {

	@Override
	public void onShardCountEvent(shardCountEvent event) {
		event.getSession().getClientConnection()
				.send(new JSONObject().put("response", "shardcount").put("value", event.getShardCount()).toString());
	}

	@Override
	public void onThreadCountEvent(threadCountEvent event) {
		event.getSession().getClientConnection()
			.send(new JSONObject().put("response", "threadcount").put("value", event.getThreadCount()).toString());
	}

	@Override
	public void onTotalGuildCountEvent(totalGuildCountEvent event) {
		event.getSession().getClientConnection()
			.send(new JSONObject().put("response", "guildcount").put("value", event.getGuildCount()).toString());
	}

	@Override
	public void onVersionEvent(versionEvent event) {
		event.getSession().getClientConnection()
			.send(new JSONObject().put("response", "botversion").put("value", event.getBotVersion()).toString());
	}

	@Override
	public void onMotdRequestEvent(motdRequestEvent event) {
		event.getSession().getClientConnection()
			.send(new JSONObject().put("response", "botmotd").put("value", event.getMOTD()).toString());
	}

	@Override
	public void onShardStatRequestEvent(shardStatRequestEvent event) {
		event.getSession().getClientConnection().send(new JSONObject()
				.put("response", "shardstat")
				.put("shard_id", event.getShard().getShardInfo().getShardId())
				.put("is_online", event.isShardOnline())
				.put("guild_count", event.getShardGuildCount())
				.toString());
	}

}
