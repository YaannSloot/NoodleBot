package main.IanSloat.noodlebot.commands;

import java.awt.Color;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return true;
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "help");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if(!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		EmbedBuilder message = new EmbedBuilder();
		message.setTitle(
				"Available commands | " + event.getMember().getUser().getName() + " | " + event.getGuild().getName());
		message.addField("**General Commands**", "**nood help** - Lists available commands", false);
		message.setColor(Color.RED);
		PermissionsManager permMgr = getPermissionsManager(event.getGuild());
		if (permMgr.authUsage(permMgr.PLAY, event.getMember())
				|| permMgr.authUsage(permMgr.VOLUME, event.getMember())
				|| permMgr.authUsage(permMgr.SKIP, event.getMember())
				|| permMgr.authUsage(permMgr.STOP, event.getMember())
				|| permMgr.authUsage(permMgr.SHOW_QUEUE, event.getMember())
				|| permMgr.authUsage(permMgr.LEAVE, event.getMember())
				|| permMgr.authUsage("jump", event.getMember())
				|| permMgr.authUsage("pause", event.getMember())
				|| permMgr.authUsage("queuemanage", event.getMember())) {
			String hlpMsg = "";
			if (permMgr.authUsage(permMgr.PLAY, event.getMember())) {
				hlpMsg += "**nood play <[scsearch:]Video name|Video URL>** - Plays a video or song\n";
				hlpMsg += "**nood add <[scsearch:]Video name|Video URL>** - Adds a video or song to the end of the queue\n";
			}
			if (permMgr.authUsage(permMgr.VOLUME, event.getMember())) {
				hlpMsg += "**nood volume <0-150>** - Changes the player volume\n";
			}
			if (permMgr.authUsage(permMgr.SKIP, event.getMember())) {
				hlpMsg += "**nood skip** - Skips the currently playing song\n";
			}
			if (permMgr.authUsage("jump", event.getMember())) {
				hlpMsg += "**nood jump <position>** - Jumps to a specific position in the currently playing track specified by a timecode in HH:MM:SS.ss form\n";
			}
			if (permMgr.authUsage("pause", event.getMember())) {
				hlpMsg += "**nood pause** - This command is a toggle. It will either pause or unpause the current track\n";
			}
			if (permMgr.authUsage("queuemanage", event.getMember())) {
				hlpMsg += "**nood remove track <track number/range of track numbers>** - Removes a track or range of tracks from the queue\n";
			}
			if (permMgr.authUsage(permMgr.STOP, event.getMember())) {
				hlpMsg += "**nood stop** - Stops the currently playing song and clears the queue\n";
			}
			if (permMgr.authUsage(permMgr.SHOW_QUEUE, event.getMember())) {
				hlpMsg += "**nood show queue** - Lists the songs currently in the song queue\n";
			}
			
			if (permMgr.authUsage(permMgr.LEAVE, event.getMember())) {
				hlpMsg += "**nood leave** - Makes the bot leave the chat\n";
			}
			message.addField("**Player commands**", hlpMsg, false);
		}
		if (permMgr.authUsage(permMgr.BY_FILTER, event.getMember())
				|| permMgr.authUsage(permMgr.SET_COMMAND, event.getMember())
				|| permMgr.authUsage(permMgr.LIST_SETTINGS, event.getMember())
				|| permMgr.authUsage(permMgr.GET_LOGIN, event.getMember())
				|| permMgr.authUsage(permMgr.PERMMGR, event.getMember())) {
			String hlpMsg = "";
			
			if (permMgr.authUsage(permMgr.BY_FILTER, event.getMember())) {
				hlpMsg += "**nood delete messages**\n" + "Parameters:\n"
						+ "older than <number> <day(s)/week(s)/month(s)/year(S)>\n" + "from <@user|@role>\n"
						+ "Ex 1 - nood delete messages older than 1 week 3 days from @everyone\n"
						+ "Ex 2 - nood delete messages from @someuser\n";
			}
			if (permMgr.authUsage(permMgr.SET_COMMAND, event.getMember())) {
				hlpMsg += "**nood set <setting> <value>** - Changes a server setting on the guild's settings file located on the bot server\n";
			}
			if (permMgr.authUsage(permMgr.LIST_SETTINGS, event.getMember())) {
				hlpMsg += "**nood list settings or nood settings** - Lists all of the settings and their values\n";
			}
			if (permMgr.authUsage(permMgr.GET_LOGIN, event.getMember())) {
				hlpMsg += "**nood get gui login** - Creates a guild password for the bot's gui manager\n";
				hlpMsg += "**nood get new gui login** - Creates a new guild password for the bot's gui manager\n";
			}
			if (permMgr.authUsage(permMgr.PERMMGR, event.getMember())) {
				hlpMsg += "**nood permission <command id/command group> <allow/deny> <@user(s) and/or @role(s)>** - sets a permission for a command/command catagory\n";
				hlpMsg += "**nood show permission ids** - lists the command id/group id for all of the bot's available commands\n";
			}
			if (permMgr.authUsage(permMgr.PERMMGR, event.getMember()) && event.getGuild().getOwner().equals(event.getMember())) {
				hlpMsg += "**nood apply default permissions** - Sets the recommended default permissions for your server\n";
			}
			message.addField("**Server management commands**", hlpMsg, false);
		}
		if (permMgr.authUsage(permMgr.INFO, event.getMember())
				|| permMgr.authUsage(permMgr.QUESTION, event.getMember())
				|| permMgr.authUsage("wiki", event.getMember())) {
			String hlpMsg = "";
			if (permMgr.authUsage(permMgr.INFO, event.getMember())) {
				hlpMsg += "**nood info** - Gets general info about the bot and it's current version number\n";
			}
			if (permMgr.authUsage(permMgr.QUESTION, event.getMember())) {
				hlpMsg += "**nood <question>** - Sends a question to WolframAlpha\n";
			}
			if (permMgr.authUsage("wiki", event.getMember())) {
				hlpMsg += "**nood wiki** - Looks up an article on Wikipedia\n";
			}
			message.addField("**Utility commands**", hlpMsg, false);
		}
		if (permMgr.authUsage(permMgr.INSPIRE_ME, event.getMember())) {
			String hlpMsg = "";
			if (permMgr.authUsage(permMgr.INSPIRE_ME, event.getMember())) {
				hlpMsg += "**nood inspire me** - Shows an inspirational image from InspiroBot\u2122\n";
			}
			message.addField("**Other commands**", hlpMsg, false);
		}
		event.getMember().getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(message.build()).queue());
	}
}
