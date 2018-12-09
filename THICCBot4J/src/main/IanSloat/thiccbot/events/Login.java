package main.IanSloat.thiccbot.events;

import sx.blah.discord.handle.impl.events.shard.LoginEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.StatusType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;

public class Login {
	
	private final Logger logger = LoggerFactory.getLogger(Login.class);

	public void BotLoginEvent(LoginEvent event) {
		logger.info("Logged in.");
		event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, BotUtils.BOT_PREFIX + "help");
		AudioSourceManagers.registerRemoteSources(Events.playerManager);
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
	}
	
}
