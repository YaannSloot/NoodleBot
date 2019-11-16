package com.IanSloat.noodlebot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

// TODO Figure out what to do with this. Otherwise it will stay deprecated
@Deprecated
public class GetGuiPasswordCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getHelpSnippet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCommandId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandCategory getCommandCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageEmbed getCommandHelpPage() {
		// TODO Auto-generated method stub
		return null;
	}

}
