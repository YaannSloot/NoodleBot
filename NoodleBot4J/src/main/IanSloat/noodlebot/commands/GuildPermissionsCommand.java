package main.IanSloat.noodlebot.commands;

import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GuildPermissionsCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
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
	public String getCommandCategory() {
		// TODO Auto-generated method stub
		return null;
	}

}
