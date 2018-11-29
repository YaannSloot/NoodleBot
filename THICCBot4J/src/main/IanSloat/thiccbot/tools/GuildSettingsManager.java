package main.IanSloat.thiccbot.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

	private ArrayList<String> getConfigFileLines() {
		ArrayList<String> lines = new ArrayList<String>();
		try {
			FileReader fileReader = new FileReader(settingsFile);
			int ch;
			String line = "";
			while ((ch = fileReader.read()) != -1) {
				if ((char) ch == '\n' || (char) ch == '\r') {
					if (!(line.equals(""))) {
						lines.add(line);
						line = "";
					}
				} else {
					line += (char) ch;
				}
			}
			if (line.length() > 0) {
				lines.add(line);
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			logger.error("Could find settings file for guild: " + guild.getName() + "(id:" + guild.getStringID() + ")");
		} catch (IOException e) {
			logger.error("Could not read settings from file for guild: " + guild.getName() + "(id:"
					+ guild.getStringID() + ")");
		}
		return lines;
	}

	private void writeSettings(ArrayList<String> settings) {
		try {
			settingsFile.delete();
			settingsFile.createNewFile();
			FileWriter fileWriter = new FileWriter(settingsFile);
			for (String line : settings) {
				fileWriter.write(line + "\r\n");
			}
			fileWriter.close();
		} catch (IOException e) {
			logger.error("Unable to write to settings file for guild: " + guild.getName() + "(id:" + guild.getStringID()
					+ ")");
		}
	}

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

	public void SetSetting(String key, String value) {
		ArrayList<String> lines;
		boolean someFailure = false;
		if (settingsFile.exists()) {
			lines = getConfigFileLines();
		} else {
			lines = new ArrayList<String>();
			try {
				settingsFile.createNewFile();
			} catch (IOException e) {
				logger.error("Could not create guild settings file");
				someFailure = true;
			}
		}
		if (someFailure == true) {
			logger.error("Error modifying settings for guild: " + guild.getName() + "(id:" + guild.getStringID() + ")");
		} else {
			boolean foundMatch = false;
			for (int i = 0; i < lines.size(); i++) {
				if (lines.get(i).startsWith(key + '=')) {
					lines.remove(i);
					lines.add(i, key + '=' + value);
					foundMatch = true;
					break;
				}
			}
			if (foundMatch == false) {
				lines.add(key + '=' + value);
			}
			logger.info("Set setting: " + key + " to the value: " + value + " for the guild: " + guild.getName() + "(id:" + guild.getStringID() + ')');
			writeSettings(lines);
		}
	}
	
	public String GetSetting(String key) {
		String result = "";
		if(settingsFile.exists()) {
			for(String line : getConfigFileLines()) {
				if(line.startsWith(key + '=')) {
					result = line.replace(key + '=', "");
					break;
				}
			}
		}
		return result;
	}

}
