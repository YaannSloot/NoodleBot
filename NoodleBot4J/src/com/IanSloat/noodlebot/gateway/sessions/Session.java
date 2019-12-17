package com.IanSloat.noodlebot.gateway.sessions;

import org.java_websocket.WebSocket;

import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * A generic gateway session
 */
public abstract class Session {

	private WebSocket conn;
	private ShardManager shardmgr;

	/**
	 * Constructs a new session
	 * 
	 * @param conn     The websocket connection associated with this session
	 * @param shardmgr The {@linkplain ShardManager} to use with this session
	 */
	public Session(WebSocket conn, ShardManager shardmgr) {
		this.conn = conn;
		this.shardmgr = shardmgr;
	}

	/**
	 * Fired when a client message has been received for this session
	 * 
	 * @param message The client message to process
	 */
	public abstract void onMessage(String message);

	/**
	 * Retrieves the websocket connection associated with this session
	 * 
	 * @return The websocket connection associated with this session
	 */
	public WebSocket getClientConnection() {
		return conn;
	}

	/**
	 * Retrieves the {@linkplain ShardManager} being used with this session
	 * 
	 * @return The {@linkplain ShardManager} being used with this session
	 */
	public ShardManager getShardManager() {
		return shardmgr;
	}

}
