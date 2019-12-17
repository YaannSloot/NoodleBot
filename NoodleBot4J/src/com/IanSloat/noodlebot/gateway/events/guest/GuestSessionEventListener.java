package com.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.events.EventMapper;

/**
 * The event listener that handles events for a
 * {@linkplain com.IanSloat.noodlebot.gateway.sessions.GuestSession
 * GuestSession}
 */
public class GuestSessionEventListener extends EventMapper {

	@Override
	public void onShardCountEvent(ShardCountEvent event) {
		event.getSession().getClientConnection()
				.send(new JSONObject().put("response", "shardcount").put("value", event.getShardCount()).toString());
	}

	@Override
	public void onThreadCountEvent(ThreadCountEvent event) {
		event.getSession().getClientConnection()
				.send(new JSONObject().put("response", "threadcount").put("value", event.getThreadCount()).toString());
	}

	@Override
	public void onTotalGuildCountEvent(TotalGuildCountEvent event) {
		event.getSession().getClientConnection()
				.send(new JSONObject().put("response", "guildcount").put("value", event.getGuildCount()).toString());
	}

	@Override
	public void onVersionEvent(VersionEvent event) {
		event.getSession().getClientConnection()
				.send(new JSONObject().put("response", "botversion").put("value", event.getBotVersion()).toString());
	}

	@Override
	public void onMotdRequestEvent(MotdRequestEvent event) {
		event.getSession().getClientConnection()
				.send(new JSONObject().put("response", "botmotd").put("value", event.getMOTD()).toString());
	}

	@Override
	public void onShardStatRequestEvent(ShardStatRequestEvent event) {
		event.getSession().getClientConnection().send(new JSONObject().put("response", "shardstat")
				.put("shard_id", event.getShard().getShardInfo().getShardId()).put("is_online", event.isShardOnline())
				.put("guild_count", event.getShardGuildCount()).toString());
	}

}
