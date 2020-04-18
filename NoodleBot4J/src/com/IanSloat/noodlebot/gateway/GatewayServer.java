package com.IanSloat.noodlebot.gateway;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.IanSloat.noodlebot.NoodleBotMain;
import com.IanSloat.noodlebot.gateway.sessions.GuestSession;
import com.IanSloat.noodlebot.gateway.sessions.Session;

import net.dv8tion.jda.api.sharding.ShardManager;

// TODO Clean this mess up
/**
 * The websocket gateway used for the noodlebot web dashboard and other
 * services. If the bot is ever deployed on a cluster server, this may
 * eventually be converted into a standalone server.
 */
public class GatewayServer extends WebSocketServer {

	private final static Logger logger = LoggerFactory.getLogger(GatewayServer.class);

	private final static Map<WebSocket, Session> sessionRegister = new HashMap<>();

	private String status = "DOWN";

	private static synchronized Session getGatewaySession(WebSocket connection, String lastMsg) {
		Session current = sessionRegister.get(connection);

		if (current == null) {
			JSONObject msgJSON = null;
			try {
				msgJSON = new JSONObject(lastMsg);
				if (msgJSON != null) {
					if (msgJSON.getString("sessiontype").equals("guest") && msgJSON.length() == 2) {
						sessionRegister.put(connection, new GuestSession(connection, NoodleBotMain.shardmgr));
						current = sessionRegister.get(connection);
						logger.info("Client:" + connection.getRemoteSocketAddress()
								+ " has registered a new guest session with the gateway");
					} else if (msgJSON.getString("sessiontype").equals("permview")) {

					}
				}
			} catch (JSONException e) {

			}
		}

		return current;
	}

	/**
	 * Constructs a new {@linkplain GatewayServer}
	 * 
	 * @param address  The address to bind to
	 * @param shardmgr The {@linkplain ShardManager} to use when handling requests
	 */
	public GatewayServer(InetSocketAddress address, ShardManager shardmgr) {
		super(address);
		status = "RUNNING";
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		conn.send("Connected to noodlebot gateway v1");
		logger.info("Started new gateway client connection. client-ip:" + conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		if (sessionRegister.get(conn) instanceof GuestSession) {
			logger.info("Guest session with client-ip:" + conn.getRemoteSocketAddress()
					+ " has been removed from the session registery");
		}
		sessionRegister.remove(conn);
		logger.info("Gateway connection with client-ip:" + conn.getRemoteSocketAddress() + " has closed. Reason: "
				+ reason);
		status = "DOWN";

	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		JSONObject msgJSON = null;
		try {
			msgJSON = new JSONObject(message);
		} catch (JSONException e) {
			conn.send(new JSONObject().put("response", "invalid_request").put("status", "disconnected").toString());
			conn.close(1007, "Invalid request caused server to close communication with the client");
		}
		if (msgJSON != null) {
			try {
				if (msgJSON.getString("request").equals("registernew")) {
					Session newSession = getGatewaySession(conn, message);
					if (newSession == null) {
						conn.send(new JSONObject().put("response", "invalid_request").put("status", "disconnected")
								.toString());
						conn.close(1007, "Invalid request caused server to close communication with the client");
					}
				} else {
					Session currentSession = getGatewaySession(conn, message);
					if (currentSession == null) {
						conn.send(new JSONObject().put("response", "invalid_request").put("status", "disconnected")
								.toString());
						conn.close(1007, "Invalid request caused server to close communication with the client");
					} else {
						currentSession.onMessage(message);
					}
				}
			} catch (JSONException e) {
				conn.send(new JSONObject().put("response", "invalid_request").put("status", "disconnected").toString());
				conn.close(1007, "Invalid request caused server to close communication with the client");
			}
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();

	}

	@Override
	public void onStart() {
		logger.info("NoodleBot Gateway server has started on address " + this.getAddress());
	}

	/**
	 * Retrieves the current status of the gateway
	 * 
	 * @return The gateway's current operating status
	 */
	public String getStatus() {
		return status;
	}

}
