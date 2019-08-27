package main.IanSloat.noodlebot.gateway.sessions;

import org.java_websocket.WebSocket;

import net.dv8tion.jda.api.sharding.ShardManager;

public abstract class Session {
	
	private WebSocket conn;
	private ShardManager shardmgr;
	
	public Session(WebSocket conn, ShardManager shardmgr) {
		this.conn = conn;
		this.shardmgr = shardmgr;
	}
	
	public abstract void onMessage(String message);
	
	public WebSocket getClientConnection() {
		return conn;
	}
	
	public ShardManager getShardManager() {
		return shardmgr;
	}
	
}
