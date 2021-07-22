package com.IanSloat.noodlebot.gateway.sessions;

import org.java_websocket.WebSocket;

import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * A session that handles requests to view guild permissions
 */
public class PermissionViewerSession extends Session {

	/**
	 * Constructs a new {@linkplain PermissionViewerSession}
	 * 
	 * @param conn     The websocket connection associated with this session
	 * @param shardmgr The {@linkplain ShardManager} to use with this session
	 */
	public PermissionViewerSession(WebSocket conn, ShardManager shardmgr) {
		super(conn, shardmgr);
	}

	@Override
	public void onMessage(String message) {
		// TODO Implement permission viewer session in gateway

	}

}
