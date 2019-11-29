package com.IanSloat.noodlebot.gateway.sessions;

import org.java_websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import com.IanSloat.noodlebot.gateway.events.SessionEventListener;
import com.IanSloat.noodlebot.gateway.events.guest.GuestSessionEventListener;
import com.IanSloat.noodlebot.gateway.events.guest.MotdRequestEvent;
import com.IanSloat.noodlebot.gateway.events.guest.ShardCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.ShardStatRequestEvent;
import com.IanSloat.noodlebot.gateway.events.guest.ThreadCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.TotalGuildCountEvent;
import com.IanSloat.noodlebot.gateway.events.guest.VersionEvent;

import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * A session that handles guest related requests
 */
public class GuestSession extends Session {

	private SessionEventListener listener;

	/**
	 * Constructs a new {@linkplain GuestSession}
	 * 
	 * @param conn     The websocket connection associated with this session
	 * @param shardmgr The {@linkplain ShardManager} to use with this session
	 */
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
				listener.broadcastEvent(new ShardCountEvent(this, msgJSON));
			else if (msgJSON.getString("request").equals("totalguildcount"))
				listener.broadcastEvent(new TotalGuildCountEvent(this, msgJSON));
			else if (msgJSON.getString("request").equals("gethreadcount"))
				listener.broadcastEvent(new ThreadCountEvent(this, msgJSON));
			else if (msgJSON.getString("request").equals("getversion"))
				listener.broadcastEvent(new VersionEvent(this, msgJSON));
			else if (msgJSON.getString("request").equals("getstatmotd"))
				listener.broadcastEvent(new MotdRequestEvent(this, msgJSON));
			else if (msgJSON.getString("request").equals("getshardstat") && msgJSON.has("shardid")) {
				if (msgJSON.getInt("shardid") >= 0
						&& msgJSON.getInt("shardid") < this.getShardManager().getShardsTotal())
					listener.broadcastEvent(new ShardStatRequestEvent(this, msgJSON));
				else
					this.getClientConnection().send(
							new JSONObject().put("response", "error").put("reason", "shardid_out_of_range").toString());
			} else
				this.getClientConnection()
						.send(new JSONObject().put("response", "error").put("reason", "invalid_request").toString());
		} catch (JSONException e) {
			this.getClientConnection().send(
					new JSONObject().put("response", "error").put("reason", "malformed_request_object").toString());
		}
	}

}
