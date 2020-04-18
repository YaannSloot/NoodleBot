package com.IanSloat.noodlebot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.FileUtils;
import org.discordbots.api.client.DiscordBotListAPI;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.IanSloat.noodlebot.commands.Command;
import com.IanSloat.noodlebot.commands.Command.CommandCategory;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermission;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermission.PermissionValue;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissions;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;
import com.IanSloat.noodlebot.controllers.settings.GuildSetting;
import com.IanSloat.noodlebot.controllers.settings.GuildSettings;
import com.IanSloat.noodlebot.controllers.settings.GuildSettingsController;
import com.IanSloat.noodlebot.events.CommandController;
import com.IanSloat.noodlebot.events.Events;
import com.IanSloat.noodlebot.gateway.GatewayServer;

import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class NoodleBotMain {

	public static String questionIDs[] = { "what", "how", "why", "when", "who", "where", "simplify" };
	private static final Logger logger = LoggerFactory.getLogger(NoodleBotMain.class);
	public static final Set<String> badWords = new HashSet<>();
	public static GatewayServer server;
	public static String versionNumber = "2";
	public static String botVersion = "NoodleBot v" + versionNumber + " Pre-release Beta";
	public static String devMsg = "Working as expected";
	public static File botSettings = new File("settings/settings.json");
	public static User botOwner;
	public static ShardManager shardmgr;
	public static DiscordBotListAPI dblEndpoint = null;
	public static JdaLavalink lavalink;
	public static EventListener eventListener = new Events();
	public static JSONObject settings;
	public static MessageEmbed announcementEmbed = null;

	// Value Overrides
	public static final int playerVolumeLimit = 2147483647;

	// Console
	public static Terminal terminal;

	// Line Reader
	public static LineReader lineReader;

	public static void main(String[] args) {

		System.out.println("\n   _  ______  ____  ___  __   _______  ____  ______\n"
				+ "  / |/ / __ \\/ __ \\/ _ \\/ /  / __/ _ )/ __ \\/_  __/\n"
				+ " /    / /_/ / /_/ / // / /__/ _// _  / /_/ / / /   \n"
				+ "/_/|_/\\____/\\____/____/____/___/____/\\____/ /_/ \n\n"
				+ "    ____  _______________            ___ \n" + "   / __ )/ ____/_  __/   |     _   _|__ \\\n"
				+ "  / __  / __/   / / / /| |    | | / /_/ /\n" + " / /_/ / /___  / / / ___ |    | |/ / __/ \n"
				+ "/_____/_____/ /_/ /_/  |_|    |___/____/ \n" + "                                         ");

		try {

			terminal = TerminalBuilder.terminal();

			lineReader = LineReaderBuilder.builder().terminal(terminal).build();

			class ModifiedPrintStream extends PrintStream {

				public ModifiedPrintStream(OutputStream out) {
					super(out, true);
				}

				@Override
				public void write(int b) {
					lineReader.printAbove("" + (char) b);
				}

				@Override
				public void write(byte[] b, int off, int len) {
					if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
						throw new IndexOutOfBoundsException();

					String output = "";

					for (int i = 0; i < len; i++) {
						output += (char) b[off + i];
					}

					lineReader.printAbove(output);
				}

				@Override
				public void write(byte[] b) throws IOException {
					String output = "";
					for (byte bt : b) {
						output += (char) bt;
					}
					lineReader.printAbove(output);
				}

			}

			PrintStream originalStream = System.out;

			System.setOut(new ModifiedPrintStream(originalStream));

			System.out.print("Starting bot");

			System.out.print("Running core file check...");

			File badWordDir = new File("filters/words");
			FileUtils.forceMkdir(badWordDir);
			Collection<File> badWordFiles = FileUtils.listFiles(badWordDir, null, false);
			badWordFiles.forEach(f -> {
				try {
					FileUtils.readLines(f, "UTF-8").forEach(l -> {
						if (!l.equals(""))
							badWords.add(l.trim().toLowerCase());
					});
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});

			logger.info("Loading settings file...");
			if (!new File("settings").exists()) {
				logger.info("Settings directory does not exist. Creating new settings directory...");
				try {
					FileUtils.forceMkdir(new File("settings"));
				} catch (IOException e) {
					logger.error("Could not create settings directory");
					System.exit(1);
				}
			}

			if (!botSettings.exists()) {
				logger.info("Settings file does not exist. Creating new settings file...");
				try {
					botSettings.createNewFile();
					FileUtils.write(botSettings, new JSONObject().toString(), "UTF-8");
				} catch (IOException e) {
					logger.error("Could not create settings file");
					System.exit(1);
				}
			}

			settings = new JSONObject(FileUtils.readFileToString(botSettings, "UTF-8"));

			if (!settings.has("token")) {
				System.out.print("Could not find a token in the bot settings file. Please enter bots token:");
				settings.put("token", lineReader.readLine(">"));
			}

			if (!settings.has("clientid")) {
				System.out.print("Could not find a client id in the bot settings file. Please enter bots client id:");
				settings.put("clientid", lineReader.readLine(">"));
			}

			if (!settings.has("linknodes")) {
				JSONArray nodeArray = new JSONArray();
				JSONObject nodeObj = new JSONObject();
				System.out.print(
						"Could not find a lavalink node address in the bot settings file. Please enter a lavalink node address:");
				String nodeAddr = lineReader.readLine(">");
				System.out.print("Please enter the lavalink node password:");
				String nodePass = lineReader.readLine(">");
				nodeObj.put("nodeaddr", nodeAddr);
				nodeObj.put("nodepass", nodePass);
				nodeArray.put(nodeObj);
				settings.put("linknodes", nodeArray);
			}

			server = new GatewayServer(new InetSocketAddress("0.0.0.0", 8000), shardmgr);

			int minShard = 0;
			int maxShard = 0;

			if (args.length > 0) {
				if (Arrays.asList(args).contains("useSSL")) {
					String sp;
					String kp;
					if (!settings.has("sslkeystore")) {
						System.out.print(
								"Gateway is set to use SSL but no keystore passwords were found. Please input required passwords.\nInput the store password:");
						sp = lineReader.readLine(">", '*');
						System.out.println("Input the key password");
						kp = lineReader.readLine(">", '*');
						JSONObject store = new JSONObject();
						store.put("storepass", sp);
						store.put("keypass", kp);
						settings.put("sslkeystore", store);
					} else if (!(settings.get("sslkeystore") instanceof JSONObject)) {
						settings.remove("sslkeystore");
						System.out.print(
								"Gateway is set to use SSL but no keystore passwords were found. Please input required passwords.\nInput the store password:");
						sp = lineReader.readLine(">", '*');
						System.out.println("Input the key password");
						kp = lineReader.readLine(">", '*');
						JSONObject store = new JSONObject();
						store.put("storepass", sp);
						store.put("keypass", kp);
						settings.put("sslkeystore", store);
					} else if (settings.getJSONObject("sslkeystore").keySet().size() != 2) {
						settings.remove("sslkeystore");
						System.out.print(
								"Gateway is set to use SSL but no keystore passwords were found. Please input required passwords.\nInput the store password:");
						sp = lineReader.readLine(">", '*');
						System.out.println("Input the key password");
						kp = lineReader.readLine(">", '*');
						JSONObject store = new JSONObject();
						store.put("storepass", sp);
						store.put("keypass", kp);
						settings.put("sslkeystore", store);
					} else {
						sp = settings.getJSONObject("sslkeystore").getString("storepass");
						kp = settings.getJSONObject("sslkeystore").getString("keypass");
					}
					try {
						KeyStore ks = KeyStore.getInstance("JKS");
						File kf = new File("keystore.jks");
						if (!kf.exists()) {
							logger.error("Keystore file does not exist. Bot is shutting down...");
							System.exit(0);
						} else {
							try {
								ks.load(new FileInputStream(kf), sp.toCharArray());
								KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
								kmf.init(ks, kp.toCharArray());
								TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
								tmf.init(ks);
								SSLContext sslContext = SSLContext.getInstance("TLS");
								sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
								server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
							} catch (NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException
									| KeyManagementException e) {
								e.printStackTrace();
								System.exit(0);
							}
						}
					} catch (KeyStoreException e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
				if (Arrays.asList(args).contains("useDBL")) {
					if (!settings.has("dbltoken")) {
						System.out
								.print("Bot is set to connect to a DBL endpoint. Please input a valid DBL api token.");
						settings.put("dbltoken", lineReader.readLine(">"));
						NoodleBotMain.dblEndpoint = new DiscordBotListAPI.Builder()
								.botId(settings.getString("clientid")).token(settings.getString("dbltoken")).build();
					}
				}
				if (Arrays.asList(args).contains("shardIds=")) {
					String arg = "";
					for (String a : args) {
						if (a.startsWith("shardIds=")) {
							arg = a;
							break;
						}
					}
					if (!arg.equals("")) {
						arg = arg.replaceFirst("shardIds=", "");
						String[] words = arg.split("-");
						if (words.length == 2) {
							try {
								int s1 = Integer.parseInt(words[0]);
								int s2 = Integer.parseInt(words[1]);
								if (s1 > s2) {
									minShard = s2;
									maxShard = s1;
								} else {
									minShard = s1;
									maxShard = s2;
								}
							} catch (NumberFormatException e) {
							}
						}
					}
				}
			}

			logger.info("Core file check complete. Loading bot...");

			logger.info("Starting shards...");

			GuildSettingsController.setInitBehavior(s -> {
				GuildSettings sList = s.getSettings();
				if (!sList.contains("volume"))
					s.setSetting(new GuildSetting("volume", "100", "Default volume", "music", "range!0-200"));
				if (!sList.contains("autoplay"))
					s.setSetting(new GuildSetting("autoplay", "on", "AutoPlay", "music", "off", "on"));
				if (!sList.contains("logchannel"))
					s.setSetting(new GuildSetting("logchannel", "disabled", "Logger channel", "logging",
							"type!TextChannel"));
				if (!sList.contains("logmentions"))
					s.setSetting(new GuildSetting("logmentions", "false", "Use @ mentions on entries", "logging",
							"false", "true"));
				try {
					s.writeSettings();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			GuildPermissionsController.setInitBehavior(per -> {
				GuildPermissions pList = per.getPermissions();
				List<String> pKeys = pList.getKeys();
				for (Command c : CommandController.commandList) {
					pKeys.remove(c.getCommandId());
				}
				for (CommandCategory ct : CommandCategory.values()) {
					pKeys.remove(ct.toString());
				}
				for (String extras : pKeys) {
					per.removePermission(extras);
				}
				if (!pList.contains(CommandCategory.MANAGEMENT.toString())) {
					GuildPermission perm = new GuildPermission(CommandCategory.MANAGEMENT.toString(), new HashMap<>(),
							new HashMap<>());
					perm.setRoleEntry(per.getGuild().getPublicRole(), PermissionValue.DENY);
					List<Role> admins = per.getGuild().getRoles().stream()
							.filter(r -> r.hasPermission(Permission.ADMINISTRATOR)).collect(Collectors.toList());
					for (Role admin : admins) {
						perm.setRoleEntry(admin, PermissionValue.ALLOW);
					}
					List<Role> nonAdmins = per.getGuild().getRoles().stream()
							.filter(r -> !r.hasPermission(Permission.ADMINISTRATOR)).collect(Collectors.toList());
					for (Role nonAdmin : nonAdmins) {
						perm.setRoleEntry(nonAdmin, PermissionValue.DENY);
					}
					per.setPermission(perm);
				}
				try {
					per.writePermissions();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			lavalink = new JdaLavalink(settings.getString("clientid"), maxShard - minShard + 1,
					shardId -> shardmgr.getShardById(shardId));

			JSONArray nodeArray = settings.getJSONArray("linknodes");

			for (Object node : nodeArray) {
				lavalink.addNode(new URI("ws://" + ((JSONObject) node).getString("nodeaddr")),
						((JSONObject) node).getString("nodepass"));
			}

			File announcementsDir = new File("announcements");

			FileUtils.forceMkdir(announcementsDir);

			server.start();

			FileUtils.write(botSettings, settings.toString(), "UTF-8");
			
			shardmgr = new DefaultShardManagerBuilder(settings.getString("token"))
					.setShardsTotal(maxShard - minShard + 1).setShards(minShard, maxShard)
					.addEventListeners(eventListener, lavalink)
					.setVoiceDispatchInterceptor(lavalink.getVoiceInterceptor()).build();
			botOwner = shardmgr.getShards().get(0).retrieveApplicationInfo().complete().getOwner();

		} catch (IOException | LoginException | IllegalArgumentException | JSONException | URISyntaxException e) {
			e.printStackTrace();
		}

	}

}
