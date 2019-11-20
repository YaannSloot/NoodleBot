package com.IanSloat.noodlebot.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

// TODO Document this class
public abstract class Command {
	
	static final Logger logger = LoggerFactory.getLogger(Command.class);
	
	// Known command categories
	public enum CommandCategory{
		GENERAL("general"),
		PLAYER("player"),
		MANAGEMENT("management"),
		UTILITY("utility"),
		MISC("misc");
		
		private String id;
		
		CommandCategory(String id){
			this.id = id;
		}
		
		public String toString() {
			return id;
		}
		
	}
	
	public abstract boolean CheckUsagePermission(Member user);
	
	public abstract boolean CheckForCommandMatch(Message command);
	
	public abstract void execute(MessageReceivedEvent event) throws NoMatchException;
	
	public abstract String getHelpSnippet();
	
	public abstract String getCommandId();
	
	public abstract CommandCategory getCommandCategory();
	
	public abstract MessageEmbed getCommandHelpPage();
	
}
