package main.IanSloat.thiccbot.commands;

import java.awt.Color;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.RequestBuffer;

public class PermIDCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.PERMMGR, user);
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if(!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		if (PermissionUtils.hasPermissions(event.getGuild(), event.getAuthor(), Permissions.ADMINISTRATOR)) {
			EmbedBuilder message = new EmbedBuilder();
			message.withTitle("Permission IDs");
			message.withColor(Color.ORANGE);
			message.appendField("**Player command IDs (global id: player)**",
					"**play** - thicc play\n" + "**volume** - thicc volume\n" + "**skip** - thicc skip\n"
							+ "**stop** - thicc stop\n" + "**showqueue** - thicc show queue\n"
							+ "**leave** - thicc leave",
					false);
			message.appendField("**Server management command IDs (global id: management)**",
					"**clear** - thicc clear message history\n" + "**filter** - thicc delete messages\n"
							+ "**set** - thicc set\n" + "**listsettings** - thicc list settings\n"
							+ "**adminlogin**:\nthicc get gui login\nthicc get new gui login\n"
							+ "**permsettings** - thicc permission",
					false);
			message.appendField("**Utility command IDs (global id: utility)**",
					"**info** - thicc info\n" + "**question** - wolfram question command", false);
			message.appendField("**Miscellaneous command IDs (global id: misc)**", "**inspire** - thicc inspire me",
					false);
			message.appendField("View the current settings for your guild's permissions:",
					"[Click Here](http://thiccbot.site/pro/permissions?guildid=" + event.getGuild().getStringID() + ")",
					false);
			RequestBuffer.request(() -> event.getAuthor().getOrCreatePMChannel().sendMessage(message.build()));
		} else {
			RequestBuffer.request(() -> event.getChannel()
					.sendMessage("You must be an administrator of this server to manage permissions"));
		}
		
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "show permission ids");
	}

}
