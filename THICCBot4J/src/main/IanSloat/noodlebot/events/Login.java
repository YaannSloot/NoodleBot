package main.IanSloat.noodlebot.events;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.commands.Command;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
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

			class commandReader implements Runnable {

				@Override
				public void run() {
					String command = "";
					while (!(command.equals("shutdown"))) {
						command = NoodleBotMain.lineReader.readLine(">");
						List<String> words = Arrays.asList(command.trim().split(" "));
						switch (words.get(0)) {
						case "status":
							System.out.println("\nCurrent version: " + NoodleBotMain.botVersion + "\n"
									+ "\nBot Stats\n---------------\nShards: "
									+ event.getJDA().getShardManager().getShardsTotal() + "\n" + "Guilds: "
									+ event.getJDA().getShardManager().getGuilds().size() + "\n" + "\nResource usage\n---------------\n"
									+ "Threads: " + Thread.activeCount() + "\n" + "Memory Usage: "
									+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000
									+ "/" + Runtime.getRuntime().maxMemory() / 1000000 + " MB\n");
							break;

						case "message": {
							if (words.get(1).equals("help")) {
								System.out.println("message <guildid> <message>");
							} else {
								TextChannel channel = event.getJDA().getTextChannelById(Long.parseLong(words.get(1)));
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
								System.out.println("The specified guild goes by the name \"" + guild.getName() + "\"");
							} else {
								System.out.println("The bot is not connected to that guild");
							}
							break;
						default:
							System.out.println("ERROR: Command not recognized");
							break;
						}

					}

					System.out.println("Bot is shutting down...");
					NoodleBotMain.shardmgr.shutdown();
					System.exit(0);
				}

			}

			Thread commandHandler = new Thread(new commandReader());
			commandHandler.start();
		}
	}

}
