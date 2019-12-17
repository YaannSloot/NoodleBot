package com.IanSloat.noodlebot.controllers.settings;

import org.json.JSONObject;

/**
 * Represents a settings entry found in a guild settings file. This acts as a
 * wrapper for a specially formatted JSON object.
 */
public class GuildSetting {

	private String key;
	private String value;
	private String title;
	private String category;
	private String[] acceptedValues;

	/**
	 * Constructs a new instance of {@linkplain GuildSetting}.
	 * 
	 * @param key            The key to associate with this entry
	 * @param value          The value this setting currently has
	 * @param title          The title or short description of this setting
	 * @param category       The category of this setting
	 * @param acceptedValues All valid values this entry can be set to
	 */
	public GuildSetting(String key, String value, String title, String category, String... acceptedValues) {
		this.key = key;
		this.value = value;
		this.title = title;
		this.category = category;
		this.acceptedValues = acceptedValues;
	}

	/**
	 * Retrieves the key associated with this setting entry
	 * 
	 * @return The key for this setting
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Retrieves the current value for this setting
	 * 
	 * @return The entry's current value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Retrieves the title or short description of this setting
	 * 
	 * @return The title or short description of this entry
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Retrieves this entry's category
	 * 
	 * @return The category this setting belongs to
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Retrieves the values that this entry should be changed to
	 * 
	 * @return An array of recommended values that this entry should be set to
	 */
	public String[] getAcceptedValues() {
		return acceptedValues;
	}

	/**
	 * Sets the value of this entry to the one specified
	 * 
	 * @param value The value to change to
	 * @return This {@linkplain GuildSetting} entry. Useful for chaining
	 */
	public GuildSetting setValue(String value) {
		this.value = value;
		return this;
	}

	/**
	 * Sets the title or short description of this setting to a new value
	 * 
	 * @param title The title or short description to change to
	 * @return This {@linkplain GuildSetting} entry. Useful for chaining
	 */
	public GuildSetting setTitle(String title) {
		this.title = title;
		return this;
	}

	/**
	 * Sets the category of this setting to a new value
	 * 
	 * @param category The category to change to
	 * @return This {@linkplain GuildSetting} entry. Useful for chaining
	 */
	public GuildSetting setCategory(String category) {
		this.category = category;
		return this;
	}

	/**
	 * Sets the array of recommended values for this entry to a different array
	 * 
	 * @param acceptedValues The array of accepted values to change to
	 * @return This {@linkplain GuildSetting} entry. Useful for chaining
	 */
	public GuildSetting setAcceptedValues(String... acceptedValues) {
		this.acceptedValues = acceptedValues;
		return this;
	}

	/**
	 * Retrieves the JSON representation of this entry. The {@linkplain JSONObject}
	 * returned will be the same way that this entry appears in the settings file.
	 * 
	 * @return A JSONObject representation of this entry
	 */
	public JSONObject getObjectEntry() {
		return new JSONObject().put("title", title).put("value", value).put("category", category).accumulate("accepted",
				acceptedValues);
	}

}
