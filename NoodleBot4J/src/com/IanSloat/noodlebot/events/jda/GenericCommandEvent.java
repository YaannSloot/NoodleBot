package com.IanSloat.noodlebot.events.jda;

import com.IanSloat.noodlebot.commands.Command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

/**
 * Signifies that a {@linkplain Command} has been fired. Used for logging purposes
 * purposes
 */
public class GenericCommandEvent extends GenericGuildEvent {

	private Command command;
	private String input;
	private Member commandIssuer;

	/**
	 * Creates a new instance of the {@linkplain GenericCommandEvent}
	 * 
	 * @param api            The JDA instance that fired this command
	 * @param responseNumber The response number for this event
	 * @param guild          The guild where this command was fired
	 * @param command        The {@linkplain Command} that was fired
	 * @param input          The raw message data that triggered this command
	 * @param commandIssuer  The {@linkplain Member} that triggered this command
	 */
	public GenericCommandEvent(JDA api, long responseNumber, Guild guild, Command command, String input,
			Member commandIssuer) {
		super(api, responseNumber, guild);
		this.command = command;
		this.input = input;
		this.commandIssuer = commandIssuer;
	}

	/**
	 * Retrieves the {@linkplain Command} object associated with this event
	 * 
	 * @return The {@linkplain Command} object associated with this event
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * Retrieves the raw message data that triggered the {@linkplain Command}
	 * associated with this event
	 * 
	 * @return A String representing the raw message data that triggered the command
	 *         associated with this event
	 */
	public String getInput() {
		return input;
	}

	/**
	 * Retrieves the {@linkplain Member} who triggered the {@linkplain Command}
	 * associated with this event
	 * 
	 * @return The guild member who triggered the command associated with this event
	 */
	public Member getCommandIssuer() {
		return commandIssuer;
	}

}
