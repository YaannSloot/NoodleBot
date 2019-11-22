package com.IanSloat.noodlebot.commands;

/**
 * Indicates that a {@linkplain Command} tried to be executed but did not match
 * the message it was executed for
 */
public class NoMatchException extends RuntimeException {

	private static final long serialVersionUID = 2471442433345020561L;

	/**
	 * Creates a new {@linkplain NoMatchException}
	 */
	public NoMatchException() {
		super("Command was executed with an invalid input message");
	}

}
