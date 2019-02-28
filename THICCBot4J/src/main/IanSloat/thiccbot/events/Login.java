package main.IanSloat.thiccbot.events;

import sx.blah.discord.handle.impl.events.shard.LoginEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.RequestBuffer;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import main.IanSloat.thiccbot.ThiccBotMain;
import main.IanSloat.thiccbot.commands.Command;
import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;

public class Login {
	
	private final Logger logger = LoggerFactory.getLogger(Login.class);

	public void BotLoginEvent(LoginEvent event) {
		logger.info("Logged in.");
		event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, BotUtils.BOT_PREFIX + "help");
		AudioSourceManagers.registerRemoteSources(Command.playerManager);
		class loadSettings extends Thread {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				GuildSettingsManager.CreateSettingsDirectoriesForGuilds(event.getClient().getGuilds());
				logger.info("Settings files loaded successfully. Settings files located in "
						+ System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "GuildSettings");
				for (IGuild guild : event.getClient().getGuilds()) {
					Events.knownGuildIds.add(guild.getStringID());
				}
			}
		}
		logger.info("Loading guild settings...");
		new loadSettings().run();
		
		class commandReader implements Runnable {

			@Override
			public void run() {
				String command = "";
				while(!(command.equals("shutdown"))) {
					command = ThiccBotMain.lineReader.readLine(">");
					List<String> words = Arrays.asList(command.trim().split(" "));
					switch (words.get(0)) {
						case "status": System.out.println("\nCurrent version: " + ThiccBotMain.botVersion + "\n"
								+ "\nBot Stats\n---------------\nShards: " + event.getClient().getShardCount() + "\n"
								+ "Guilds: " + event.getClient().getGuilds().size() + "\n"
								+ "\nResource usage\n---------------\n"
								+ "Threads: " + Thread.activeCount() + "\n"
								+ "Memory Usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000 + "/" + Runtime.getRuntime().maxMemory() / 1000000 + " MB\n");
								 break;
						
						case "message": {
							if(words.get(1).equals("help")) {
								System.out.println("message <guildid> <message>");
							} else {
								IChannel channel = event.getClient().getChannelByID(Long.parseLong(words.get(1)));
								String message = command.replace("message " + words.get(1), "");
								RequestBuffer.request(() -> channel.sendMessage(message));
							}
							break;
						}
								 
						default: System.out.println("ERROR: Command not recognized");
								 break;
					}
					
				}
				
				System.out.println("Bot is shutting down...");
				System.exit(0);
			}
			
		}
		
		Thread commandHandler = new Thread(new commandReader());
		commandHandler.start();
		
	}
	
}
