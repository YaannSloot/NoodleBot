package com.IanSloat.noodlebot.reactivecore;

import net.dv8tion.jda.api.entities.User;

// TODO Write documentation
public class ButtonClickEvent {

	private String emoji = "";
	private User user;
	
	public ButtonClickEvent(String emoji, User user) {
		this.emoji = emoji;
		this.user = user;
	}
	
	public String getEmoji() {
		return this.emoji;
	}
	
	public User getUser() {
		return this.user;
	}
	
}
