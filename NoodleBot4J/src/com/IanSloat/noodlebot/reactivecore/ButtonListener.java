package com.IanSloat.noodlebot.reactivecore;

/**
 * A {@linkplain ButtonClickEvent} listener that handles broadcasted events
 */
public interface ButtonListener {

	/**
	 * Called when an event is fired
	 * 
	 * @param event The event that was fired
	 */
	void onButtonClick(ButtonClickEvent event);

	/**
	 * Retrieves the button associated with this listener
	 * 
	 * @return The button associated with this listener
	 */
	Button getButton();
}
