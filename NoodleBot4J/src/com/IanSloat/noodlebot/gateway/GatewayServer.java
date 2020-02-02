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

	//private final static Map<WebSocket, File> sessionTempdir = new HashMap<>();

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
		// try {
		// FileUtils.deleteDirectory(sessionTempdir.get(conn));
		if (sessionRegister.get(conn) instanceof GuestSession) {
			logger.info("Guest session with client-ip:" + conn.getRemoteSocketAddress()
					+ " has been removed from the session registery");
		}
		sessionRegister.remove(conn);
		// sessionTempdir.remove(conn);
		logger.info("Gateway connection with client-ip:" + conn.getRemoteSocketAddress() + " has closed. Reason: "
				+ reason);
		status = "DOWN";
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

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
		/*
		 * JSONObject msgJSON = null; try { msgJSON = new JSONObject(message); } catch
		 * (JSONException e) {
		 * 
		 * } if (msgJSON != null) { if (message.contains("guildid=") &&
		 * message.contains("passwd=")) { message = BotUtils.normalizeSentence(message);
		 * String[] words = message.split(" "); long guildID = 0; String passwd = "";
		 * for (String element : words) { if (element.contains("guildid=")) { guildID =
		 * Long.parseLong(element.replace("guildid=", "")); } else if
		 * (element.contains("passwd=")) { passwd = element.replace("passwd=", ""); } }
		 * if (guildID != 0) { Guild guild = shardmgr.getGuildById(guildID); if (guild
		 * != null) { GuildSettingsManager setMgr = new GuildSettingsManager(guild);
		 * NBMLSettingsParser setParser = setMgr.getTBMLParser();
		 * setParser.setScope(NBMLSettingsParser.DOCROOT);
		 * setParser.addObj("GuiSettings"); setParser.setScope("GuiSettings"); if
		 * (setParser.getFirstInValGroup("guipasswd").equals(passwd)) {
		 * conn.send("Success!"); } else { conn.send("No!"); } } else {
		 * conn.send("No!"); } } else { conn.send("No!"); }
		 * 
		 * } else if (message.equals("requestnewstatstream")) {
		 * conn.send("requestapproved"); } else if (message.equals("updaterq")) {
		 * conn.send("srdcnt:" + shardmgr.getShardsTotal()); conn.send("vsninf:" +
		 * NoodleBotMain.botVersion); conn.send("trdcnt:" + Thread.activeCount());
		 * conn.send("dvmsg:" + NoodleBotMain.devMsg); } else if (msgJSON.opt("request")
		 * != null) { String request = msgJSON.optString("request"); JSONObject resJSON
		 * = new JSONObject();
		 * 
		 * if (request.equals("registernewsession")) { if (msgJSON.opt("guildid") !=
		 * null) { Guild guild = shardmgr.getGuildById(msgJSON.optLong("guildid")); if
		 * (guild != null) { if (sessionRegister.get(conn) == null) {
		 * sessionRegister.put(conn, guild); } resJSON.put("response",
		 * "session-event=registered"); conn.send(resJSON.toString()); } else {
		 * resJSON.put("response", "session-error=invalid-guild");
		 * conn.send(resJSON.toString()); } } } else { if (sessionRegister.get(conn) !=
		 * null) { if (request.equals("permmgr-init")) { File tempDir =
		 * getTemporaryFileDir(conn); File settingsFile = new
		 * File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR +
		 * "GuildSettings" + BotUtils.PATH_SEPARATOR + sessionRegister.get(conn).getId()
		 * + BotUtils.PATH_SEPARATOR + "settings.guild"); if(settingsFile.exists()) {
		 * try { FileUtils.copyFileToDirectory(settingsFile, tempDir); File tempSettings
		 * = new File(tempDir.getAbsolutePath() + BotUtils.PATH_SEPARATOR +
		 * "settings.guild"); PermissionPayloadLoader payload = new
		 * PermissionPayloadLoader(sessionRegister.get(conn), tempSettings);
		 * resJSON.put("payload-id", "permmgr-pre-init"); resJSON.put("content", new
		 * JSONObject().put("perm-registry-details", payload.queryObjects()));
		 * conn.send(resJSON.toString()); } catch (IOException e) { e.printStackTrace();
		 * } } } else if (request.equals("getpermids")) { File tempDir =
		 * getTemporaryFileDir(conn); File settingsFile = new
		 * File(tempDir.getAbsolutePath() + BotUtils.PATH_SEPARATOR + "settings.guild");
		 * if(settingsFile.exists()) { PermissionPayloadLoader payload = new
		 * PermissionPayloadLoader(sessionRegister.get(conn), settingsFile);
		 * resJSON.put("payload-id", "permmgr-id-list"); resJSON.put("content", new
		 * JSONObject().put("registry-ids", payload.getIDs()));
		 * conn.send(resJSON.toString()); } else { resJSON.put("response",
		 * "session-error=session_not_initialized"); conn.send(resJSON.toString()); } }
		 * } else { resJSON.put("response", "session-error=noguild");
		 * conn.send(resJSON.toString()); } } } } else { conn.close(1,
		 * "ERROR: Invalid arguement. Session has closed"); }
		 */
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
