package com.IanSloat.noodlebot.controllers.settings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Guild;

/**
 * Used to create and modify settings files for Discord guilds. File data is
 * stored in JSON format
 */
public class GuildSettingsController {

	private static Consumer<GuildSettingsController> initBehavior;
	private static final File masterSettingsDirectory = new File("guilds");
	private final File settingsDirectory;
	private final File settingsFile;
	private JSONObject settingsRaw;

	/**
	 * Creates a new instance of {@linkplain GuildSettingsController}. Running this
	 * constructor will create a new folder in the "guilds" directory, usually found
	 * in the bot's working directory. The folder will be named after the guild's
	 * snowflake id and will have a new file created inside it called
	 * "settings.json". Anything else set in the static init behavior consumer will
	 * also be run.
	 * 
	 * @param guild The guild to create a new instance of
	 *              {@linkplain GuildSettingsController} for
	 * @throws IOException If something goes wrong when doing any file io
	 */
	public GuildSettingsController(Guild guild) throws IOException {
		if (!masterSettingsDirectory.exists())
			FileUtils.forceMkdir(masterSettingsDirectory);
		settingsDirectory = new File(masterSettingsDirectory + "/" + guild.getId());
		settingsFile = new File(settingsDirectory + "/settings.json");
		if (!settingsDirectory.exists())
			FileUtils.forceMkdir(settingsDirectory);
		if (!settingsFile.exists())
			settingsFile.createNewFile();
		try {
			settingsRaw = new JSONObject(FileUtils.readFileToString(settingsFile, "UTF-8"));
		} catch (JSONException e) {
			settingsRaw = new JSONObject();
			FileUtils.write(settingsFile, settingsRaw.toString(), "UTF-8");
		}
		initBehavior.accept(this);
	}

	/**
	 * Sets the global consumer to whatever is passed in the initBehavior parameter.
	 * This can be useful for adding initial values to each instance that is
	 * created.
	 * 
	 * @param initBehavior A consumer to be used each time a new instance of
	 *                     {@linkplain GuildSettingsController} is created.
	 */
	public static void setInitBehavior(Consumer<GuildSettingsController> initBehavior) {
		GuildSettingsController.initBehavior = initBehavior;
	}

	/**
	 * Constructs a new instance of {@linkplain GuildSettingsController} for the
	 * sole purpose of initializing the files. Nothing is returned, and the instance
	 * is discarded once the files and directories have been created.
	 * 
	 * @param guild The guild to create new settings files for
	 * @throws IOException If something goes wrong when doing any file io
	 */
	public static void initGuildSettingsFiles(Guild guild) throws IOException {
		new GuildSettingsController(guild);
	}

	/**
	 * Deletes the settings directory for a specific guild. This should only be used
	 * once no other instance of {@linkplain GuildSettingsController} is modifying
	 * settings for this guild. Otherwise, you may encounter random exceptions.
	 * 
	 * @param guild The guild to delete settings for
	 * @throws IOException If something goes wrong when doing any file io
	 */
	public static void deleteGuildSettings(Guild guild) throws IOException {
		if (!masterSettingsDirectory.exists())
			FileUtils.forceMkdir(masterSettingsDirectory);
		FileUtils.forceDelete(new File(masterSettingsDirectory + "/" + guild.getId()));
	}

	/**
	 * Adds the specified {@linkplain GuildSetting} object to the settings cache.
	 * This does not modify the file. Instead this is a safe use method that has no
	 * effect on any retrieved settings as long as the write method hasn't been
	 * called.
	 * 
	 * @param setting The setting to apply to the settings cache. Note that only one
	 *                setting can occupy a given key at any point in time so if a
	 *                pre-existing setting has the same key as this one it will be
	 *                overwritten.
	 * @return This {@linkplain GuildSettingsController} instance. Useful for
	 *         chaining.
	 */
	public GuildSettingsController setSetting(GuildSetting setting) {
		if (setting != null)
			settingsRaw.put(setting.getKey(), setting.getObjectEntry());
		return this;
	}

	/**
	 * Removes the specified {@linkplain GuildSetting} object from the settings
	 * cache. This does not modify the file. Instead this is a safe use method that
	 * has no effect on any retrieved settings as long as the write method hasn't
	 * been called.
	 * 
	 * @param key The key that points to an existing {@linkplain GuildSetting}
	 * @return This {@linkplain GuildSettingsController} instance. Useful for
	 *         chaining.
	 */
	public GuildSettingsController removeSetting(String key) {
		settingsRaw.remove(key);
		return this;
	}

	/**
	 * Removes the specified {@linkplain GuildSetting} object from the settings
	 * cache. This does not modify the file. Instead this is a safe use method that
	 * has no effect on any retrieved settings as long as the write method hasn't
	 * been called.
	 * 
	 * @param setting The setting that contains a pre-existing key desired to be
	 *                removed
	 * @return This {@linkplain GuildSettingsController} instance. Useful for
	 *         chaining.
	 */
	public GuildSettingsController removeSetting(GuildSetting setting) {
		settingsRaw.remove(setting.getKey());
		return this;
	}

	/**
	 * Performs a fresh read on the settings file and attempts to retrieve the
	 * desired setting that the specified key points to. This has no effect on the
	 * current state of the settings cache.
	 * 
	 * @param key The key that points to the desired setting.
	 * @return An instance of {@linkplain GuildSetting} that represents a setting
	 *         entry, or null if either no setting could be found that matches that
	 *         key or if a file io error occurred.
	 */
	public GuildSetting getSetting(String key) {
		GuildSetting result = null;
		try {
			JSONObject settings = new JSONObject(FileUtils.readFileToString(settingsFile, "UTF-8"));
			if (settings.has(key))
				if (settings.get(key) instanceof JSONObject)
					result = new GuildSetting(key, settings.getJSONObject(key).getString("value"),
							settings.getJSONObject(key).getString("title"),
							settings.getJSONObject(key).getString("category"),
							getValuesFromArray(settings.getJSONObject(key).optJSONArray("accepted")));
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Performs a fresh read on the settings file and attempts to retrieve every
	 * valid setting entry. The settings are then placed into a
	 * {@linkplain GuildSettings} list, which is a modified {@linkplain ArrayList}.
	 * This has no effect on the current state of the settings cache.
	 * 
	 * @return A {@linkplain GuildSettings} list containing all valid settings
	 *         entries found in the settings file.
	 */
	public GuildSettings getSettings() {
		GuildSettings result = new GuildSettings();
		try {
			JSONObject settings = new JSONObject(FileUtils.readFileToString(settingsFile, "UTF-8"));
			for (String k : settings.keySet()) {
				if (settings.get(k) instanceof JSONObject)
					if (((JSONObject) settings.get(k)).has("title") && ((JSONObject) settings.get(k)).has("value")
							&& ((JSONObject) settings.get(k)).has("category")
							&& ((JSONObject) settings.get(k)).has("accepted"))
						result.add(new GuildSetting(k, ((JSONObject) settings.get(k)).getString("value"),
								((JSONObject) settings.get(k)).getString("title"),
								((JSONObject) settings.get(k)).getString("category"),
								getValuesFromArray(((JSONObject) settings.get(k)).optJSONArray("accepted"))));
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Writes what is currently contained in the settings cache to the settings
	 * file. The settings are written in JSON format.
	 * 
	 * @throws IOException If something goes wrong when doing any file io
	 */
	public void writeSettings() throws IOException {
		FileUtils.write(settingsFile, settingsRaw.toString(), "UTF-8");
	}

	private String[] getValuesFromArray(JSONArray a) {
		List<String> pile = new ArrayList<String>();
		if (a != null) {
			for (Object o : a) {
				if (o instanceof String)
					pile.add((String) o);
			}
		}
		String[] result = pile.toArray(new String[pile.size()]);
		return result;
	}

}
