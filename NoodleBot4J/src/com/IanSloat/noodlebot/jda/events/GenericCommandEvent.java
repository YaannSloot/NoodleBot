package com.IanSloat.noodlebot.jda.events;

import com.IanSloat.noodlebot.commands.Command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

//TODO Document class
public class GenericCommandEvent extends GenericGuildEvent {
	
	private Command command;
	private String input;
	private Member commandIssuer;
	
	public GenericCommandEvent(JDA api, long responseNumber, Guild guild, Command command, String input, Member commandIssuer) {
		super(api, responseNumber, guild);
		this.command = command;
		this.input = input;
		this.commandIssuer = commandIssuer;
	}

	public Command getCommand() {
		return command;
	}
	
	public String getInput() {
		return input;
	}
	
	public Member getCommandIssuer() {
		return commandIssuer;
	}
	
}
