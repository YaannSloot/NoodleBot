package main.IanSloat.noodlebot.gateway.sessions;

import org.java_websocket.WebSocket;

import net.dv8tion.jda.api.sharding.ShardManager;

public class PermissionViewerSession extends Session {

	public PermissionViewerSession(WebSocket conn, ShardManager shardmgr) {
		super(conn, shardmgr);
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
		
	}

}
