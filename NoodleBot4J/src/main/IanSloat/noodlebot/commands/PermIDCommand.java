package main.IanSloat.noodlebot.commands;

import java.awt.Color;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PermIDCommand extends Command {
	
	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if(!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			EmbedBuilder message = new EmbedBuilder();
			message.setTitle("Permission IDs");
			message.setColor(Color.ORANGE);
			message.addField("**Player command IDs (global id: player)**",
					"**play** - nood play/nood add\n" + "**volume** - nood volume\n" + "**skip** - nood skip\n"
							+ "**jump** - nood jump\n"
							+ "**stop** - nood stop\n"
							+ "**pause** - nood pause\n"
							+ "**queuemanage - nood remove track"
							+ "**showqueue** - nood show queue\n"
							+ "**leave** - nood leave",
					false);
			message.addField("**Server management command IDs (global id: management)**",
							"**filter** - nood delete messages\n"
							+ "**set** - nood set\n" + "**listsettings** - nood list settings\n"
							+ "**adminlogin**:\nnood get gui login\nnood get new gui login\n"
							+ "**permsettings** - nood permission/nood show permission ids/nood apply default permissions",
					false);
			message.addField("**Utility command IDs (global id: utility)**",
					"**info** - nood info\n" 
							+ "**question** - wolfram question command\n"
							+ "**wiki** - nood wiki", 
					false);
			message.addField("**Miscellaneous command IDs (global id: misc)**", "**inspire** - nood inspire me",
					false);
			//message.addField("View the current settings for your guild's permissions:",
			//		"[Click Here](http://noodbot.site/pro/permissions?guildid=" + event.getGuild().getId() + ")",
			//		false);
			
			event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(message.build()).queue());
		} else {
			event.getChannel().sendMessage("You must be an administrator of this server to manage permissions").queue();
		}
		
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "show permission ids");
	}

	@Override
	public String getHelpSnippet() {
		return "**nood show permission ids** - lists the command id/group id for all of the bot's available commands";
	}

	@Override
	public String getCommandId() {
		return "permsettings";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_MANAGEMENT;
	}

}
