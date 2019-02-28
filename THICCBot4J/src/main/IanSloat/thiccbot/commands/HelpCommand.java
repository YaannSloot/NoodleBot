package main.IanSloat.thiccbot.commands;

import java.awt.Color;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class HelpCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return true;
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "help");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if(!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		EmbedBuilder message = new EmbedBuilder();
		message.withTitle(
				"Available commands | " + event.getAuthor().getName() + " | " + event.getGuild().getName());
		message.appendField("**General Commands**", "**thicc help** - Lists available commands", false);
		message.withColor(Color.RED);
		PermissionsManager permMgr = getPermissionsManager(event.getGuild());
		if (permMgr.authUsage(permMgr.PLAY, event.getAuthor())
				|| permMgr.authUsage(permMgr.VOLUME, event.getAuthor())
				|| permMgr.authUsage(permMgr.SKIP, event.getAuthor())
				|| permMgr.authUsage(permMgr.STOP, event.getAuthor())
				|| permMgr.authUsage(permMgr.SHOW_QUEUE, event.getAuthor())
				|| permMgr.authUsage(permMgr.LEAVE, event.getAuthor())) {
			String hlpMsg = "";
			if (permMgr.authUsage(permMgr.PLAY, event.getAuthor())) {
				hlpMsg += "**thicc play <[scsearch:]Video name|Video URL>** - Plays a video or song\n";
			}
			if (permMgr.authUsage(permMgr.VOLUME, event.getAuthor())) {
				hlpMsg += "**thicc volume <0-150>** - Changes the player volume\n";
			}
			if (permMgr.authUsage(permMgr.SKIP, event.getAuthor())) {
				hlpMsg += "**thicc skip** - Skips the currently playing song\n";
			}
			if (permMgr.authUsage(permMgr.STOP, event.getAuthor())) {
				hlpMsg += "**thicc stop** - Stops the currently playing song and clears the queue\n";
			}
			if (permMgr.authUsage(permMgr.SHOW_QUEUE, event.getAuthor())) {
				hlpMsg += "**thicc show queue** - Lists the songs currently in the song queue\n";
			}
			if (permMgr.authUsage(permMgr.LEAVE, event.getAuthor())) {
				hlpMsg += "**thicc leave** - Makes the bot leave the chat\n";
			}
			message.appendField("**Player commands**", hlpMsg, false);
		}
		if (permMgr.authUsage(permMgr.CLEAR_COMMAND, event.getAuthor())
				|| permMgr.authUsage(permMgr.BY_FILTER, event.getAuthor())
				|| permMgr.authUsage(permMgr.SET_COMMAND, event.getAuthor())
				|| permMgr.authUsage(permMgr.LIST_SETTINGS, event.getAuthor())
				|| permMgr.authUsage(permMgr.GET_LOGIN, event.getAuthor())
				|| permMgr.authUsage(permMgr.PERMMGR, event.getAuthor())) {
			String hlpMsg = "";
			if (permMgr.authUsage(permMgr.CLEAR_COMMAND, event.getAuthor())) {
				hlpMsg += "**thicc clear message history** - Deletes all messages older than 1 week\n";
			}
			if (permMgr.authUsage(permMgr.BY_FILTER, event.getAuthor())) {
				hlpMsg += "**thicc delete messages**\n" + "Parameters:\n"
						+ "older than <number> <day(s)/week(s)/month(s)/year(S)>\n" + "from <@user|@role>\n"
						+ "Ex 1 - thicc delete messages older than 1 week 3 days from @everyone\n"
						+ "Ex 2 - thicc delete messages older than 1 month\n"
						+ "Ex 3 - thicc delete messages from @someuser\n";
			}
			if (permMgr.authUsage(permMgr.SET_COMMAND, event.getAuthor())) {
				hlpMsg += "**thicc set <setting> <value>** - Changes a server setting on the guild's settings file located on the bot server\n";
			}
			if (permMgr.authUsage(permMgr.LIST_SETTINGS, event.getAuthor())) {
				hlpMsg += "**thicc list settings or thicc settings** - Lists all of the settings and their values\n";
			}
			if (permMgr.authUsage(permMgr.GET_LOGIN, event.getAuthor())) {
				hlpMsg += "**thicc get gui login** - Creates a guild password for the bot's gui manager\n";
				hlpMsg += "**thicc get new gui login** - Creates a new guild password for the bot's gui manager\n";
			}
			if (permMgr.authUsage(permMgr.PERMMGR, event.getAuthor())) {
				hlpMsg += "**thicc permission <command id/command group> <allow/deny> <@user(s) and/or @role(s)>** - sets a permission for a command/command catagory\n";
			}
			message.appendField("**Server management commands**", hlpMsg, false);
		}
		if (permMgr.authUsage(permMgr.INFO, event.getAuthor())
				|| permMgr.authUsage(permMgr.QUESTION, event.getAuthor())) {
			String hlpMsg = "";
			if (permMgr.authUsage(permMgr.INFO, event.getAuthor())) {
				hlpMsg += "**thicc info** - Gets general info about the bot and it's current version number\n";
			}
			if (permMgr.authUsage(permMgr.QUESTION, event.getAuthor())) {
				hlpMsg += "**thicc <question>** - Sends a question to WolframAlpha\n";
			}
			message.appendField("**Utility commands**", hlpMsg, false);
		}
		if (permMgr.authUsage(permMgr.INSPIRE_ME, event.getAuthor())) {
			String hlpMsg = "";
			if (permMgr.authUsage(permMgr.INSPIRE_ME, event.getAuthor())) {
				hlpMsg += "**thicc inspire me** - Shows an inspirational image from InspiroBot\u2122\n";
			}
			message.appendField("**Other commands**", hlpMsg, false);
		}
		RequestBuffer.request(() -> event.getAuthor().getOrCreatePMChannel().sendMessage(message.build()));
	}
}
