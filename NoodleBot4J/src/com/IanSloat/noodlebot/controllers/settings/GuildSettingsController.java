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

// TODO Implement this class
//TODO Document class
public class GuildSettingsController {

	private static Consumer<GuildSettingsController> initBehavior;
	private static final File masterSettingsDirectory = new File("guilds");
	private final File settingsDirectory;
	private final File settingsFile;
	private JSONObject settingsRaw;

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

	public static void setInitBehavior(Consumer<GuildSettingsController> initBehavior) {
		GuildSettingsController.initBehavior = initBehavior;
	}

	public static void initGuildSettingsFiles(Guild guild) throws IOException {
		new GuildSettingsController(guild);
	}

	public static void deleteGuildSettings(Guild guild) throws IOException {
		if (!masterSettingsDirectory.exists())
			FileUtils.forceMkdir(masterSettingsDirectory);
		FileUtils.forceDelete(new File(masterSettingsDirectory + "/" + guild.getId()));
	}

	public GuildSettingsController setSetting(GuildSetting setting) {
		if (setting != null)
			settingsRaw.put(setting.getKey(), setting.getObjectEntry());
		return this;
	}

	public GuildSetting getSetting(String key) {
		GuildSetting result = null;
		try {
			JSONObject settings = new JSONObject(FileUtils.readFileToString(settingsFile, "UTF-8"));
			if (settings.has(key))
				if (settings.get(key) instanceof JSONObject)
					result = new GuildSetting(key, settings.getJSONObject(key).getString("value"),
							settings.getJSONObject(key).getString("title"),
							settings.getJSONObject(key).getString("category"),
							getValuesFromArray(settings.optJSONArray("accepted")));
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}

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

	public void writeSettings() throws IOException {
		FileUtils.write(settingsFile, settingsRaw.toString(), "UTF-8");
	}

	private String[] getValuesFromArray(JSONArray a) {
		List<String> pile = new ArrayList<String>();
		for (Object o : a) {
			if (o instanceof String)
				pile.add((String) o);
		}
		String[] result = new String[pile.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = pile.get(i);
		}
		return result;
	}

}
