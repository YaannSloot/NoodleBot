package main.IanSloat.noodlebot.commands;

import java.awt.Color;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.jdaevents.GenericCommandErrorEvent;
import main.IanSloat.noodlebot.jdaevents.GenericCommandEvent;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class ListSettingsCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return (command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "list settings")
				|| command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "settings"));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		NoodleBotMain.eventListener.onEvent(new GenericCommandEvent(event.getJDA(), event.getResponseNumber(),
				event.getGuild(), this, event.getMessage().getContentRaw().toLowerCase(), event.getMember()));
		try {
			event.getMessage().delete().queue();
			GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
			NBMLSettingsParser setParser = setMgr.getNBMLParser();
			setParser.setScopePath("PlayerSettings");
			if (setParser.getFirstInValGroup("volume").equals("")) {
				setParser.addVal("volume", "100");
			}
			if (setParser.getFirstInValGroup("autoplay").equals("")) {
				setParser.addVal("autoplay", "off");
			}
			if (setParser.getFirstInValGroup("volumecap").equals("")) {
				setParser.addVal("volumecap", "on");
			}
			EmbedBuilder response = new EmbedBuilder();
			response.setColor(new Color(0, 200, 0));
			response.setTitle("Settings | " + event.getGuild().getName());
			response.addField("Voice channel settings",
					"Default volume = " + setParser.getFirstInValGroup("volume") + "\nAutoPlay = "
							+ setParser.getFirstInValGroup("autoplay") + "\nEnforce volume cap = "
							+ setParser.getFirstInValGroup("volumecap"),
					false);
			setParser.setScopePath("LoggerSettings");
			String loggerDestination = setParser.getFirstInValGroup("LoggerChannel");
			if (loggerDestination.equals("")) {
				loggerDestination = "logging is disabled";
			} else {
				loggerDestination = "<#" + loggerDestination + ">";
			}
			String loggerMentions = setParser.getFirstInValGroup("LoggerMentions");
			if (loggerMentions.equals("")) {
				loggerMentions = "true";
			}
			response.addField("Logging settings",
					"Logger channel = " + loggerDestination + "\nUse @ mentions on entries = " + loggerMentions, false);
			event.getChannel().sendMessage(response.build()).queue();
		} catch (InsufficientPermissionException e) {
			String permission = e.getPermission().getName();
			EmbedBuilder message = new EmbedBuilder();
			message.setTitle("Missing permission error | " + event.getGuild().getName());
			message.addField("Error message:", "Bot is missing required permission **" + permission
					+ "**. Please grant this permission to the bot's role or contact a guild administrator to apply this permission to the bot's role.",
					false);
			message.setColor(Color.red);
			event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(message.build()).queue());
			NoodleBotMain.eventListener.onEvent(new GenericCommandErrorEvent(event.getJDA(), event.getResponseNumber(),
					event.getGuild(), this, event.getMessage().getContentRaw(), event.getMember(),
					"Command execution failed due to missing permission: " + permission));
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood list settings or nood settings** - Lists all of the settings and their values";
	}

	@Override
	public String getCommandId() {
		return "listsettings";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_MANAGEMENT;
	}
}
