package com.IanSloat.noodlebot.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * The generic type of the object that represents commands on the bot. Each
 * command object contains a {@link Command#execute(MessageReceivedEvent)
 * execute()} method used once the command is ready to fire
 */
public interface Command {

	static final Logger logger = LoggerFactory.getLogger(Command.class);

	/**
	 * The set of categories that each command will have assigned to it
	 */
	public enum CommandCategory {
		GENERAL("general"), PLAYER("player"), MANAGEMENT("management"), UTILITY("utility");

		private String id;

		CommandCategory(String id) {
			this.id = id;
		}

		public String toString() {
			return id;
		}

	}

	/**
	 * Checks whether a specified guild member has access to this command
	 * 
	 * @param user The member to check for
	 * @return true if the member can use this command
	 */
	public boolean CheckUsagePermission(Member user);

	/**
	 * Checks whether the provided message matches the identifiers for this command
	 * 
	 * @param command The message to check
	 * @return true if the message matches this command's trigger identifier
	 */
	public boolean CheckForCommandMatch(Message command);

	/**
	 * Executes this command with the given event
	 * 
	 * @param event The event to execute this command with
	 * @throws NoMatchException If the message in the specified event does not match
	 *                          this command's trigger identifier
	 */
	public void execute(MessageReceivedEvent event) throws NoMatchException;

	/**
	 * Retrieves this command's help entry
	 * 
	 * @return A string representing this command's help entry
	 */
	public String getHelpSnippet();

	/**
	 * Retrieves this command's special id
	 * 
	 * @return A string representing this command's special id
	 */
	public String getCommandId();

	/**
	 * Retrieves the {@linkplain CommandCategory} associated with this command
	 * 
	 * @return The {@linkplain CommandCategory} associated with this command
	 */
	public CommandCategory getCommandCategory();

	/**
	 * Retrieves the in-depth explanation of this command's functionality
	 * 
	 * @return A {@linkplain MessageEmbed} representing the in-depth explanation of
	 *         this command's functionality
	 */
	public MessageEmbed getCommandHelpPage();

}
