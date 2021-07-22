package com.IanSloat.noodlebot.events.jda;

import com.IanSloat.noodlebot.commands.Command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

/**
 * Signifies that an error occurred when processing a {@linkplain Command} that
 * was triggered. Used for logging purposes
 */
public class GenericCommandErrorEvent extends GenericCommandEvent {

	private String errorMessage;

	/**
	 * Creates a new instance of the {@linkplain GenericCommandErrorEvent}
	 * 
	 * @param api            The JDA instance that fired this command
	 * @param responseNumber The response number for this event
	 * @param guild          The guild where this command was fired
	 * @param command        The {@linkplain Command} that was fired
	 * @param input          The raw message data that triggered this command
	 * @param commandIssuer  The {@linkplain Member} that triggered this command
	 * @param errorMessage   A string describing the error that occurred
	 */
	public GenericCommandErrorEvent(JDA api, long responseNumber, Guild guild, Command command, String input,
			Member commandIssuer, String errorMessage) {
		super(api, responseNumber, guild, command, input, commandIssuer);
		this.errorMessage = errorMessage;
	}

	/**
	 * Retrieves a {@linkplain String} containing information about the error that occurred
	 * @return A {@linkplain String} containing information about the error that occurred
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
