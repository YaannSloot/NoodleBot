package com.IanSloat.noodlebot.events;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.NoodleBotMain;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;
import com.IanSloat.noodlebot.controllers.settings.GuildSettingsController;
import com.IanSloat.noodlebot.tools.EmbedImporter;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;

/**
 * Handles {@linkplain ReadyEvent}s fired when a shard logs in. Makes sure all
 * shards have a synchronous start once the bot has completely logged in. All
 * guild setting directories will be initialized as well during synchronization.
 */
public class Login {

	private final Logger logger = LoggerFactory.getLogger(Login.class);

	public void BotLoginEvent(ReadyEvent event) {
		logger.info("Shard " + event.getJDA().getShardInfo().getShardId() + " has started.");

		if (event.getJDA().getShardInfo().getShardId() < event.getJDA().getShardManager().getShardsTotal() - 1) {
			try {
				synchronized (event.getJDA()) {
					logger.info("Shard " + event.getJDA().getShardInfo().getShardId()
							+ " is waiting for other shards to start...");
					event.getJDA().getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB,
							Activity.playing("Bot is starting..."));
					event.getJDA().wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("All shards have logged in. Performing guild settings file check...");
			event.getJDA().getShardManager().getGuilds().forEach(g -> {
				try {
					GuildSettingsController.initGuildSettingsFiles(g);
					GuildPermissionsController.initGuildPermissionsFiles(g);
				} catch (IOException e) {
					logger.error(
							"Failed to init settings directory for guild " + g.getName() + "(id:" + g.getId() + ")");
					e.printStackTrace();
					System.exit(1);
				}
			});
			logger.info("Guild settings file check complete.");
			File announcement = new File("announcements/announcement.json");

			if (announcement.exists())
				NoodleBotMain.botOwner.openPrivateChannel()
						.queue(pm -> pm.sendMessage(new EmbedImporter(announcement).getEmbed()).queue());

			if (NoodleBotMain.dblEndpoint != null) {
				ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
				final Runnable UpdateTask = new Runnable() {

					@Override
					public void run() {
						NoodleBotMain.dblEndpoint.setStats(NoodleBotMain.shardmgr.getGuilds().size());
					}

				};
				scheduler.scheduleAtFixedRate(UpdateTask, 5, 5, TimeUnit.MINUTES);
			}

			ForkJoinPool.commonPool().execute(() -> {
				String command = "";
				while (!(command.equals("shutdown"))) {
					try {
						command = NoodleBotMain.lineReader.readLine(">");
						List<String> words = Arrays.asList(command.trim().split(" "));
						switch (words.get(0)) {
						case "status":
							System.out.print("Current version: " + NoodleBotMain.botVersion + "\n"
									+ "\nBot Stats\n---------------\nShards: "
									+ event.getJDA().getShardManager().getShardsTotal() + "\n" + "Guilds: "
									+ event.getJDA().getShardManager().getGuilds().size() + "\n"
									+ "\nResource usage\n---------------\n" + "Threads: " + Thread.activeCount() + "\n"
									+ "Memory Usage: "
									+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000
									+ "/" + Runtime.getRuntime().maxMemory() / 1000000 + " MB\n");
							break;

						case "message": {
							if (words.get(1).equals("help")) {
								System.out.print("message <guildid> <message>");
							} else {
								TextChannel channel = event.getJDA().getTextChannelById(Long.parseLong(words.get(1)));
								String message = command.replace("message " + words.get(1), "");
								channel.sendMessage(message).queue();
							}
							break;
						}
						case "shutdown":
							System.out.print("Shutdown requested. Halting threads...");
							break;
						case "whois":
							Guild guild = event.getJDA().getGuildById(words.get(1));
							if (guild != null) {
								System.out.print("The specified guild goes by the name \"" + guild.getName() + "\"");
							} else {
								System.out.print("The bot is not connected to that guild");
							}
							break;
						case "addnode":
							JSONArray currentNodeArray = NoodleBotMain.settings.getJSONArray("linknodes");
							System.out.print("Please input the new node's address");
							String nodeAddr = NoodleBotMain.lineReader.readLine(">");
							System.out.print("Please input the new node's password");
							String nodePass = NoodleBotMain.lineReader.readLine(">");
							Map<String, String> nodeMap = new HashMap<>();
							currentNodeArray.forEach(node -> nodeMap.put(((JSONObject) node).getString("nodeaddr"),
									((JSONObject) node).getString("nodepass")));
							if (nodeMap.containsKey(nodeAddr))
								System.out.print("ERROR: Node already present in settings");
							else {
								JSONObject newNode = new JSONObject();
								newNode.put("nodeaddr", nodeAddr);
								newNode.put("nodepass", nodePass);
								currentNodeArray.put(newNode);
								NoodleBotMain.settings.put("linknodes", currentNodeArray);
								FileUtils.write(NoodleBotMain.botSettings, NoodleBotMain.settings.toString(), "UTF-8");
								NoodleBotMain.lavalink.addNode(new URI("ws://" + nodeAddr), nodePass);
							}
							break;
						case "broadcast":
							if (announcement.exists()) {
								MessageEmbed payload = new EmbedImporter(announcement).getEmbed();
								File backupDir = new File("announcements/previous");
								FileUtils.forceMkdir(backupDir);
								FileUtils.moveFile(announcement, new File("announcements/previous/"
										+ new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss").format(Date.from(Instant.now()))
										+ ".json"));
								List<Guild> guilds = NoodleBotMain.shardmgr.getGuilds();
								Set<User> targetUsers = new HashSet<User>();
								for (Guild targetGuild : guilds) {
									targetUsers.add(targetGuild.getOwner().getUser());
								}
								for (User targetUser : targetUsers) {
									targetUser.openPrivateChannel().queue(pm -> pm.sendMessage(payload).queue(null,
											error -> error.printStackTrace()));
								}
								System.out.print("Broadcast sent successfully");
							} else
								System.out.print("ERROR: Announcement file not found");
							break;
						default:
							System.out.print("ERROR: Command not recognized");
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.out.print("ERROR: Command parse error");
					}
				}
				NoodleBotMain.shardmgr.shutdown();
				try {
					NoodleBotMain.server.stop();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
				System.out.print("Bot is shutting down...");
				System.exit(0);
			});
			for (JDA shard : event.getJDA().getShardManager().getShards()) {
				synchronized (shard) {
					shard.notify();
				}
			}
		}
		event.getJDA().getPresence().setPresence(OnlineStatus.ONLINE,
				Activity.playing(BotUtils.BOT_PREFIX + "help | v" + NoodleBotMain.versionNumber + " snapshot"));
		logger.info("Shard " + event.getJDA().getShardInfo().getShardId() + " is ready.");

	}
}
