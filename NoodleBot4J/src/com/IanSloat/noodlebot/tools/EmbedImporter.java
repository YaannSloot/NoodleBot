package com.IanSloat.noodlebot.tools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class EmbedImporter {

	private File rawJSONFile;

	public EmbedImporter(File rawFile) {
		rawJSONFile = rawFile;
	}

	public EmbedBuilder getEditableEmbed() {
		EmbedBuilder embed = new EmbedBuilder();
		try {
			JSONObject embedRaw = new JSONObject(FileUtils.readFileToString(rawJSONFile, "UTF-8"));
			embedRaw = embedRaw.getJSONObject("embed");
			if (embedRaw.has("title")) {
				if (embedRaw.has("url"))
					embed.setTitle(embedRaw.getString("title"), embedRaw.getString("url"));
				else
					embed.setTitle(embedRaw.getString("title"));
			}
			if (embedRaw.has("description"))
				embed.appendDescription(embedRaw.getString("description"));
			if (embedRaw.has("color"))
				embed.setColor(embedRaw.getInt("color"));
			if (embedRaw.has("footer")) {
				if (embedRaw.getJSONObject("footer").has("icon_url"))
					embed.setFooter(embedRaw.getJSONObject("footer").getString("text"),
							embedRaw.getJSONObject("footer").getString("icon_url"));
				else
					embed.setFooter(embedRaw.getJSONObject("footer").getString("text"));
			}
			if (embedRaw.has("thumbnail"))
				embed.setThumbnail(embedRaw.getJSONObject("thumbnail").getString("url"));
			if (embedRaw.has("image"))
				embed.setImage(embedRaw.getJSONObject("image").getString("url"));
			if (embedRaw.has("author")) {
				if (embedRaw.getJSONObject("author").has("url") && embedRaw.getJSONObject("author").has("icon_url"))
					embed.setAuthor(embedRaw.getJSONObject("author").getString("name"),
							embedRaw.getJSONObject("author").getString("url"),
							embedRaw.getJSONObject("author").getString("icon_url"));
				else if (embedRaw.getJSONObject("author").has("url"))
					embed.setAuthor(embedRaw.getJSONObject("author").getString("name"),
							embedRaw.getJSONObject("author").getString("url"));
				else
					embed.setAuthor(embedRaw.getJSONObject("author").getString("name"));
			}
			if (embedRaw.has("fields")) {
				embedRaw.getJSONArray("fields").forEach(field -> {
					JSONObject fieldObj = (JSONObject) field;
					if (fieldObj.has("inline"))
						embed.addField(fieldObj.getString("name"), fieldObj.getString("value"),
								fieldObj.getBoolean("inline"));
					else
						embed.addField(fieldObj.getString("name"), fieldObj.getString("value"), false);
				});
			}
		} catch (JSONException | IOException e) {
			throw new RuntimeException(e);
		}
		return embed;
	}

	public MessageEmbed getEmbed() {
		return getEditableEmbed().build();
	}

}
