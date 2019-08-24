package main.IanSloat.noodlebot.guiclientserver;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.events.CommandHandler;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;

public class ClientServer extends WebSocketServer {

	private final Logger logger = LoggerFactory.getLogger(ClientServer.class);

	private ShardManager shardmgr;

	private final static Map<WebSocket, Guild> sessionRegister = new HashMap<>();

	private final static Map<WebSocket, File> sessionTempdir = new HashMap<>();

	private File getTemporaryFileDir(WebSocket session) {
		File output = null;
		if (sessionRegister.get(session) != null) {
			File tempDirectory = new File(
					System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "GuildSettings" + BotUtils.PATH_SEPARATOR
							+ sessionRegister.get(session).getId() + BotUtils.PATH_SEPARATOR + "temp");
			if (tempDirectory.exists() == false) {
				try {
					FileUtils.forceMkdir(tempDirectory);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				if (sessionTempdir.get(session) == null) {
					File tempFile = Files.createTempDirectory(tempDirectory.toPath(), "session").toFile();
					FileUtils.forceDeleteOnExit(tempFile);
					sessionTempdir.put(session, tempFile);
				}
				output = sessionTempdir.get(session);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return output;
	}

	public ClientServer(InetSocketAddress address, ShardManager shardmgr) {
		super(address);
		this.shardmgr = shardmgr;
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		conn.send("Connected to noodlebot gateway v1");
		logger.info("Started new gui client connection. client-ip:" + conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		try {
			FileUtils.deleteDirectory(sessionTempdir.get(conn));
			sessionRegister.remove(conn);
			sessionTempdir.remove(conn);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		JSONObject msgJSON = null;
		try {
			msgJSON = new JSONObject(message);
		} catch (JSONException e) {

		}
		if (msgJSON != null) {
			if (message.contains("guildid=") && message.contains("passwd=")) {
				message = BotUtils.normalizeSentence(message);
				String[] words = message.split(" ");
				long guildID = 0;
				String passwd = "";
				for (String element : words) {
					if (element.contains("guildid=")) {
						guildID = Long.parseLong(element.replace("guildid=", ""));
					} else if (element.contains("passwd=")) {
						passwd = element.replace("passwd=", "");
					}
				}
				if (guildID != 0) {
					Guild guild = shardmgr.getGuildById(guildID);
					if (guild != null) {
						GuildSettingsManager setMgr = new GuildSettingsManager(guild);
						NBMLSettingsParser setParser = setMgr.getTBMLParser();
						setParser.setScope(NBMLSettingsParser.DOCROOT);
						setParser.addObj("GuiSettings");
						setParser.setScope("GuiSettings");
						if (setParser.getFirstInValGroup("guipasswd").equals(passwd)) {
							conn.send("Success!");
						} else {
							conn.send("No!");
						}
					} else {
						conn.send("No!");
					}
				} else {
					conn.send("No!");
				}

			} else if (message.equals("requestnewstatstream")) {
				conn.send("requestapproved");
			} else if (message.equals("updaterq")) {
				conn.send("srdcnt:" + shardmgr.getShardsTotal());
				conn.send("vsninf:" + NoodleBotMain.botVersion);
				conn.send("trdcnt:" + Thread.activeCount());
				conn.send("dvmsg:" + NoodleBotMain.devMsg);
			} else if (msgJSON.opt("request") != null) {
				String request = msgJSON.optString("request");
				JSONObject resJSON = new JSONObject();

				if (request.equals("registernewsession")) {
					if (msgJSON.opt("guildid") != null) {
						Guild guild = shardmgr.getGuildById(msgJSON.optLong("guildid"));
						if (guild != null) {
							if (sessionRegister.get(conn) == null) {
								sessionRegister.put(conn, guild);
							}
							resJSON.put("response", "session-event=registered");
							conn.send(resJSON.toString());
						} else {
							resJSON.put("response", "session-error=invalid-guild");
							conn.send(resJSON.toString());
						}
					}
				} else {
					if (sessionRegister.get(conn) != null) {
						if (request.equals("permmgr-init")) {
							File tempDir = getTemporaryFileDir(conn);
							File settingsFile = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR
									+ "GuildSettings" + BotUtils.PATH_SEPARATOR + sessionRegister.get(conn).getId()
									+ BotUtils.PATH_SEPARATOR + "settings.guild");
							if(settingsFile.exists()) {
								try {
									FileUtils.copyFileToDirectory(settingsFile, tempDir);
									File tempSettings = new File(tempDir.getAbsolutePath() + BotUtils.PATH_SEPARATOR + "settings.guild");
									PermissionPayloadLoader payload = new PermissionPayloadLoader(sessionRegister.get(conn), tempSettings);
									resJSON.put("payload-id", "permmgr-pre-init");
									resJSON.put("content", new JSONObject().put("perm-registry-details", payload.queryObjects()));
									conn.send(resJSON.toString());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} else if (request.equals("getpermids")) {
							File tempDir = getTemporaryFileDir(conn);
							File settingsFile = new File(tempDir.getAbsolutePath() + BotUtils.PATH_SEPARATOR + "settings.guild");
							if(settingsFile.exists()) {
								PermissionPayloadLoader payload = new PermissionPayloadLoader(sessionRegister.get(conn), settingsFile);
								resJSON.put("payload-id", "permmgr-id-list");
								resJSON.put("content", new JSONObject().put("registry-ids", payload.getIDs()));
								conn.send(resJSON.toString());
							} else {
								resJSON.put("response", "session-error=session_not_initialized");
								conn.send(resJSON.toString());
							}
						}
					} else {
						resJSON.put("response", "session-error=noguild");
						conn.send(resJSON.toString());
					}
				}
			}
		} else {
			conn.send("ERROR: Invalid JSON Object");
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		logger.error(ex.getMessage());

	}

	@Override
	public void onStart() {
		logger.info("GUI Client server has started on address " + this.getAddress());
	}

}
