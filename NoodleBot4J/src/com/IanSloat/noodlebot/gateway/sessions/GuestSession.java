package com.IanSloat.noodlebot.gateway.sessions;

import org.java_websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.events.SessionEventListener;
import com.IanSloat.noodlebot.gateway.events.guest.GuestSessionEventListener;
import com.IanSloat.noodlebot.gateway.events.guest.motdRequestEvent;
import com.IanSloat.noodlebot.gateway.events.guest.shardCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.shardStatRequestEvent;
import com.IanSloat.noodlebot.gateway.events.guest.threadCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.totalGuildCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.versionEvent;

import net.dv8tion.jda.api.sharding.ShardManager;

// TODO Document this class
public class GuestSession extends Session {

	private SessionEventListener listener;

	public GuestSession(WebSocket conn, ShardManager shardmgr) {
		super(conn, shardmgr);
		this.listener = new GuestSessionEventListener();
		JSONObject msgJSON = new JSONObject();
		msgJSON.put("response", "success");
		msgJSON.put("sessiontype", "guest");
		conn.send(msgJSON.toString());
	}

	@Override
	public void onMessage(String message) {
		JSONObject msgJSON = null;
		try {
			msgJSON = new JSONObject(message);
			if (msgJSON.getString("request").equals("getshardcount"))
				listener.broadcastEvent(new shardCountEvent(this, msgJSON));
			else if (msgJSON.getString("request").equals("totalguildcount"))
				listener.broadcastEvent(new totalGuildCountEvent(this, msgJSON));
			else if (msgJSON.getString("request").equals("gethreadcount"))
				listener.broadcastEvent(new threadCountEvent(this, msgJSON));
			else if (msgJSON.getString("request").equals("getversion"))
				listener.broadcastEvent(new versionEvent(this, msgJSON));
			else if (msgJSON.getString("request").equals("getstatmotd"))
				listener.broadcastEvent(new motdRequestEvent(this, msgJSON));
			else if (msgJSON.getString("request").equals("getshardstat") && msgJSON.has("shardid")) {
				if (msgJSON.getInt("shardid") >= 0
						&& msgJSON.getInt("shardid") < this.getShardManager().getShardsTotal())
					listener.broadcastEvent(new shardStatRequestEvent(this, msgJSON));
				else
					this.getClientConnection().send(
							new JSONObject().put("response", "error").put("reason", "shardid_out_of_range").toString());
			} else
				this.getClientConnection().send(new JSONObject().put("response", "error").put("reason", "invalid_request").toString());
		} catch (JSONException e) {
			this.getClientConnection().send(new JSONObject().put("response", "error").put("reason", "malformed_request_object").toString());
		}
	}

}
