package com.IanSloat.noodlebot.controllers.settings;

import org.json.JSONObject;

// TODO Document this class
public class GuildSetting {

	private String key;
	private String value;
	private String title;
	private String category;
	private String[] acceptedValues;

	public GuildSetting(String key, String value, String title, String category, String... acceptedValues) {
		this.key = key;
		this.value = value;
		this.title = title;
		this.category = category;
		this.acceptedValues = acceptedValues;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public String getTitle() {
		return title;
	}

	public String getCategory() {
		return category;
	}

	public String[] getAcceptedValues() {
		return acceptedValues;
	}

	public GuildSetting setValue(String value) {
		this.value = value;
		return this;
	}

	public GuildSetting setTitle(String title) {
		this.title = title;
		return this;
	}

	public GuildSetting setCategory(String category) {
		this.category = category;
		return this;
	}

	public GuildSetting setAcceptedValues(String... acceptedValues) {
		this.acceptedValues = acceptedValues;
		return this;
	}

	public JSONObject getObjectEntry() {
		return new JSONObject().put("title", title).put("value", value).put("category", category).accumulate("accepted",
				acceptedValues);
	}

}
