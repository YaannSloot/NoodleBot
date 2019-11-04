package main.IanSloat.noodlebot.reactivecore;

import net.dv8tion.jda.api.entities.User;

public class Button implements ButtonListener{

	private Runnable action;
	private String emojiName;
	private ButtonClickEvent lastEvent;
	
	public Button(String emojiName, Runnable action) {
		this.emojiName = emojiName;
		this.action = action;
	}
	
	public Button(String emojiName) {
		this.emojiName = emojiName;
	}
	
	public void setButtonAction(Runnable action) {
		this.action = action;
	}
	
	/**
	 * Retrieves the codepoint or custom emoji name associated with this button
	 * @return The String object containing the name or codepoint of this button's associated emoji
	 */
	public String getEmojiName() {
		return this.emojiName;
	}
	
	/**
	 * Gets the user that most recently triggered the button
	 * @return The most recent user or null if no user has triggered the button yet
	 */
	public User getUser() {
		if(lastEvent != null) {
			return lastEvent.getUser();
		} else {
			return null;
		}
	}

	@Override
	public void onButtonClick(ButtonClickEvent event) {
		if(event.getEmoji().equals(this.emojiName)) {
			lastEvent = event;
			action.run();
		}
	}

	@Override
	public Button getButton() {
		return this;
	}
	
}
