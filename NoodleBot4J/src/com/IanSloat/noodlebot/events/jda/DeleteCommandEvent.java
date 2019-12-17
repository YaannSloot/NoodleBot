package com.IanSloat.noodlebot.events.jda;

import java.util.List;

import com.IanSloat.noodlebot.commands.Command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

/**
 * Signifies that a {@linkplain com.IanSloat.noodlebot.commands.DeleteCommand
 * DeleteCommand} has been fired. Used for logging purposes
 */
public class DeleteCommandEvent extends GenericCommandEvent {

	private List<Member> targetMembers;
	private List<Role> targetRoles;
	private List<Message> targetMessages;

	/**
	 * Creates a new instance of the {@linkplain DeleteCommandEvent}
	 * 
	 * @param api            The JDA instance that fired this command
	 * @param responseNumber The response number for this event
	 * @param guild          The guild where this command was fired
	 * @param command        The {@linkplain Command} that was fired
	 * @param input          The raw message data that triggered this command
	 * @param commandIssuer  The {@linkplain Member} that triggered this command
	 * @param targetMembers  The target members for the message deletion job
	 * @param targetRoles    The target roles for the message deletion job
	 * @param targetMessages The target messages for the message deletion job
	 */
	public DeleteCommandEvent(JDA api, long responseNumber, Guild guild, Command command, String input,
			Member commandIssuer, List<Member> targetMembers, List<Role> targetRoles, List<Message> targetMessages) {
		super(api, responseNumber, guild, command, input, commandIssuer);
		this.targetMembers = targetMembers;
		this.targetRoles = targetRoles;
		this.targetMessages = targetMessages;
	}

	/**
	 * Retrieves a list of the target members for this job
	 * 
	 * @return A list containing the target members for this job
	 */
	public List<Member> getTargetMembers() {
		return targetMembers;
	}

	/**
	 * Retrieves a list of the target roles for this job
	 * 
	 * @return A list containing the target roles for this job
	 */
	public List<Role> getTargetRoles() {
		return targetRoles;
	}

	/**
	 * Retrieves a list of the target messages for this job
	 * 
	 * @return Retrieves a list of the target messages for this job
	 */
	public List<Message> getTargetMessages() {
		return targetMessages;
	}

}
