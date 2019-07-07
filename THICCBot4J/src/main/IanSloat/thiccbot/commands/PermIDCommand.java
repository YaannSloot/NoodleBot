package main.IanSloat.thiccbot.commands;

import java.awt.Color;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PermIDCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.PERMMGR, user);
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
					"**play** - thicc play/thicc add\n" + "**volume** - thicc volume\n" + "**skip** - thicc skip\n"
							+ "**jump** - thicc jump\n"
							+ "**stop** - thicc stop\n"
							+ "**pause** - thicc pause\n"
							+ "**queuemanage - thicc remove track"
							+ "**showqueue** - thicc show queue\n"
							+ "**leave** - thicc leave",
					false);
			message.addField("**Server management command IDs (global id: management)**",
							"**filter** - thicc delete messages\n"
							+ "**set** - thicc set\n" + "**listsettings** - thicc list settings\n"
							+ "**adminlogin**:\nthicc get gui login\nthicc get new gui login\n"
							+ "**permsettings** - thicc permission/thicc show permission ids/thicc apply default permissions",
					false);
			message.addField("**Utility command IDs (global id: utility)**",
					"**info** - thicc info\n" 
							+ "**question** - wolfram question command\n"
							+ "**wiki** - thicc wiki", 
					false);
			message.addField("**Miscellaneous command IDs (global id: misc)**", "**inspire** - thicc inspire me",
					false);
			message.addField("View the current settings for your guild's permissions:",
					"[Click Here](http://thiccbot.site/pro/permissions?guildid=" + event.getGuild().getId() + ")",
					false);
			
			event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(message.build()).queue());
		} else {
			event.getChannel().sendMessage("You must be an administrator of this server to manage permissions").queue();
		}
		
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "show permission ids");
	}

}
