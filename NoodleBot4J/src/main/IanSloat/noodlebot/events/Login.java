package main.IanSloat.noodlebot.events;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.discordbots.api.client.DiscordBotListAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.commands.Command;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;

public class Login {

	private static int loginCounter = 0;
	private boolean disableInitialLoad = false;

	private final Logger logger = LoggerFactory.getLogger(Login.class);

	public void BotLoginEvent(ReadyEvent event) {
		logger.info("Shard " + event.getJDA().getShardInfo().getShardId() + " has logged in.");
		event.getJDA().getPresence().setPresence(OnlineStatus.ONLINE,
				Activity.playing("Please wait... Bot is starting up"));

		loginCounter++;

		if (loginCounter == NoodleBotMain.shardmgr.getShardsTotal() && disableInitialLoad == false) {
			event.getJDA().getShardManager().setPresence(OnlineStatus.ONLINE,
					Activity.playing(BotUtils.BOT_PREFIX + "help | Beta v" + NoodleBotMain.versionNumber));
			AudioSourceManagers.registerRemoteSources(Command.playerManager);
			disableInitialLoad = true;
			logger.info("All shards have logged in successfully");
			class loadSettings extends Thread {
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					GuildSettingsManager
							.CreateSettingsDirectoriesForGuilds(event.getJDA().getShardManager().getGuilds());
					logger.info("Settings files loaded successfully. Settings files located in "
							+ System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "GuildSettings");
					for (Guild guild : event.getJDA().getShardManager().getGuilds()) {
						Events.knownGuildIds.add(guild.getId());
					}
				}
			}
			logger.info("Loading guild settings...");
			new loadSettings().run();
			
			NBMLSettingsParser setMgr = new NBMLSettingsParser(NoodleBotMain.configFile);
			
			if(setMgr.getFirstInValGroup("DBLTOKEN").length() != 0) {
				NoodleBotMain.dblEndpoint = new DiscordBotListAPI.Builder()
						.token(setMgr.getFirstInValGroup("DBLTOKEN"))
						.botId(NoodleBotMain.shardmgr.getShards().get(0).getSelfUser().getId())
						.build();
				ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
				final Runnable UpdateTask = new Runnable() {

					@Override
					public void run() {
						NoodleBotMain.dblEndpoint.setStats(NoodleBotMain.shardmgr.getGuilds().size());
					}
					
				};
				scheduler.scheduleAtFixedRate(UpdateTask, 5, 5, TimeUnit.MINUTES);
			}

			class commandReader implements Runnable {

				@Override
				public void run() {
					String command = "";
					while (!(command.equals("shutdown"))) {
						try {
							command = NoodleBotMain.lineReader.readLine(">");
							List<String> words = Arrays.asList(command.trim().split(" "));
							switch (words.get(0)) {
							case "status":
								System.out.println("\nCurrent version: " + NoodleBotMain.botVersion + "\n"
										+ "\nBot Stats\n---------------\nShards: "
										+ event.getJDA().getShardManager().getShardsTotal() + "\n" + "Guilds: "
										+ event.getJDA().getShardManager().getGuilds().size() + "\n"
										+ "\nResource usage\n---------------\n" + "Threads: " + Thread.activeCount()
										+ "\n" + "Memory Usage: "
										+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
												/ 1000000
										+ "/" + Runtime.getRuntime().maxMemory() / 1000000 + " MB\n");
								break;

							case "message": {
								if (words.get(1).equals("help")) {
									System.out.println("message <guildid> <message>");
								} else {
									TextChannel channel = event.getJDA()
											.getTextChannelById(Long.parseLong(words.get(1)));
									String message = command.replace("message " + words.get(1), "");
									channel.sendMessage(message).queue();
								}
								break;
							}
							case "shutdown":
								System.out.println("Shutdown requested. Halting threads...");
								break;
							case "whois":
								Guild guild = event.getJDA().getGuildById(words.get(1));
								if (guild != null) {
									System.out.println(
											"The specified guild goes by the name \"" + guild.getName() + "\"");
								} else {
									System.out.println("The bot is not connected to that guild");
								}
								break;
							default:
								System.out.println("ERROR: Command not recognized");
								break;
							}
						} catch (Exception e) {
							System.out.println("ERROR: Command parse error");
						}
					}

					NoodleBotMain.shardmgr.shutdown();
					try {
						NoodleBotMain.server.stop();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}

					System.out.println("Bot is shutting down...");

					System.exit(0);
				}

			}

			Thread commandHandler = new Thread(new commandReader());
			commandHandler.start();
		}
	}

}
