package main.IanSloat.thiccbot.tools;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import main.IanSloat.thiccbot.BotUtils;
import sx.blah.discord.handle.obj.IGuild;

public class GuildSettingsManager {

	private IGuild guild;
	private static final Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);
	private File settingsDirectory;
	private File settingsFile;

	public GuildSettingsManager(IGuild guild) {
		this.guild = guild;
		settingsDirectory = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "GuildSettings"
				+ BotUtils.PATH_SEPARATOR + guild.getStringID());
		settingsFile = new File(settingsDirectory.getAbsolutePath() + BotUtils.PATH_SEPARATOR + "settings.guild");
		logger.info(
				"Started guild settings manager for guild: " + guild.getName() + "(id:" + guild.getStringID() + ')');
	}

	public static void CreateSettingsDirectoriesForGuilds(List<IGuild> Guilds) {
		for (IGuild guild : Guilds) {
			File SettingDirectory = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "GuildSettings"
					+ BotUtils.PATH_SEPARATOR + guild.getStringID());
			if (!(SettingDirectory.exists())) {
				SettingDirectory.mkdirs();
				logger.info("Settings directory added for guild:" + guild.getName() + "(id:" + guild.getStringID()
						+ ") at path " + SettingDirectory.getAbsolutePath());
			}
		}
	}

	public void RemoveSettings() {
		if (settingsDirectory.exists()) {
			FileSystemUtils.deleteRecursively(settingsDirectory);
			logger.info("Guild settings for guild: " + guild.getName() + "(id:" + guild.getStringID()
					+ ") removed successfully");
		} else {
			logger.info("Guild: " + guild.getName() + "(id:" + guild.getStringID()
					+ ") had no settings so no files were deleted");
		}
	}

	public void CreateSettings() {
		if (settingsDirectory.exists()) {
			settingsDirectory.delete();
			settingsDirectory.mkdirs();
		} else {
			settingsDirectory.mkdirs();
		}
	}
	
	public TBMLSettingsParser getTBMLParser() {
		return new TBMLSettingsParser(settingsFile);
	}

}