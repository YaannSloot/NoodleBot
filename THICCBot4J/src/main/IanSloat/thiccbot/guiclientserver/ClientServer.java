package main.IanSloat.thiccbot.guiclientserver;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.ThiccBotMain;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;

public class ClientServer extends WebSocketServer{

	private final Logger logger = LoggerFactory.getLogger(ClientServer.class);
	
	private IDiscordClient client;
	
	public ClientServer(InetSocketAddress address, IDiscordClient client) {
		super(address);
		this.client = client;
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		conn.send("Connected to thiccbot gateway v1");
		logger.info("Started new gui client connection. client-ip:" + conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		if(message.contains("guildid=") && message.contains("passwd=")) {
			message = BotUtils.normalizeSentence(message);
			String[] words = message.split(" ");
			long guildID = 0;
			String passwd = "";
			for(String element : words) {
				if(element.contains("guildid=")) {
					guildID = Long.parseLong(element.replace("guildid=", ""));
				} else if(element.contains("passwd=")) {
					passwd = element.replace("passwd=", "");
				}
			}
			if(guildID != 0) {
				IGuild guild = client.getGuildByID(guildID);
				if(guild != null) {
					GuildSettingsManager setMgr = new GuildSettingsManager(guild);
					if(setMgr.GetSetting("guipasswd").equals(passwd)) {
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
			
		} else if(message.equals("requestnewstatstream")) {
			conn.send("requestapproved");
		} else if(message.equals("updaterq")) {
			conn.send("srdcnt:" + client.getShardCount());
			conn.send("vsninf:" + ThiccBotMain.botVersion);
			conn.send("trdcnt:" + Thread.activeCount());
			conn.send("dvmsg:" + ThiccBotMain.devMsg);
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
