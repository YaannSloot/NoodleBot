package com.IanSloat.noodlebot.reactivecore;

import net.dv8tion.jda.api.entities.User;

/**
 * Represents when a button is clicked
 */
public class ButtonClickEvent {

	private String emoji = "";
	private User user;

	/**
	 * Constructs a new {@linkplain ButtonClickEvent}
	 * 
	 * @param emoji The emoji that was clicked
	 * @param user  The user who clicked the button
	 */
	public ButtonClickEvent(String emoji, User user) {
		this.emoji = emoji;
		this.user = user;
	}

	/**
	 * Retrieves the name or codepoint of the emoji that was clicked
	 * 
	 * @return The name or codepoint of the emoji that was clicked
	 */
	public String getEmoji() {
		return this.emoji;
	}

	/**
	 * Retrieves the user who clicked the button
	 * 
	 * @return The user who clicked the button
	 */
	public User getUser() {
		return this.user;
	}

}
