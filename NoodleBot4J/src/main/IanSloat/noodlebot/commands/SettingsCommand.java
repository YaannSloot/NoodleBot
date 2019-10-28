package main.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class SettingsCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "set ");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		try {
			event.getMessage().delete().queue();
			GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
			NBMLSettingsParser setParser = setMgr.getNBMLParser();
			setParser.setScope(NBMLSettingsParser.DOCROOT);
			String command = BotUtils.normalizeSentence(
					event.getMessage().getContentRaw().toLowerCase().substring((BotUtils.BOT_PREFIX + "set").length()));
			String[] words = command.split(" ");
			if (command.toLowerCase().startsWith("default volume ") || words[0].equals("volume")
					|| command.toLowerCase().startsWith("default volume to ")
					|| command.toLowerCase().startsWith("volume to ")) {
				try {
					int value;
					if (command.toLowerCase().startsWith("default volume ")
							|| command.toLowerCase().startsWith("volume to ")) {
						value = Integer.parseInt(words[2]);
					} else if (command.toLowerCase().startsWith("default volume to ")) {
						value = Integer.parseInt(words[3]);
					} else {
						value = Integer.parseInt(words[1]);
					}
					setParser.setScope(NBMLSettingsParser.DOCROOT);
					setParser.addObj("PlayerSettings");
					setParser.setScope("PlayerSettings");
					if (setParser.getFirstInValGroup("volume").equals("")) {
						setParser.addVal("volume", "100");
					}
					setParser.setFirstInValGroup("volume", Integer.toString(value));
					event.getChannel().sendMessage("Changed default volume to " + value).queue();
				} catch (NumberFormatException e) {
					event.getChannel().sendMessage("The value provided is not valid for that setting").queue();
				}
			} else if (command.toLowerCase().startsWith("autoplay ") && words.length >= 2) {
				setParser.setScope(NBMLSettingsParser.DOCROOT);
				setParser.addObj("PlayerSettings");
				setParser.setScope("PlayerSettings");
				if (setParser.getFirstInValGroup("autoplay").equals("")) {
					setParser.addVal("autoplay", "off");
				}
				if (words[1].toLowerCase().equals("on")) {
					setParser.setFirstInValGroup("autoplay", "on");
					event.getChannel().sendMessage("Set AutoPlay to \'on\'").queue();
				} else if (words[1].toLowerCase().equals("off")) {
					setParser.setFirstInValGroup("autoplay", "off");
					event.getChannel().sendMessage("Set AutoPlay to \'off\'").queue();
				} else {
					event.getChannel().sendMessage("The value provided is not valid for that setting").queue();
				}
			} else if (command.toLowerCase().startsWith("volumecap ") && words.length >= 2) {
				setParser.setScope(NBMLSettingsParser.DOCROOT);
				setParser.addObj("PlayerSettings");
				setParser.setScope("PlayerSettings");
				if (setParser.getFirstInValGroup("volumecap").equals("")) {
					setParser.addVal("volumecap", "on");
				}
				if (words[1].toLowerCase().equals("on")) {
					setParser.setFirstInValGroup("volumecap", "on");
					event.getChannel().sendMessage("Set volume limit to \'on\'").queue();
				} else if (words[1].toLowerCase().equals("off")) {
					setParser.setFirstInValGroup("volumecap", "off");
					event.getChannel().sendMessage("Set volume limit to \'off\'").queue();
				} else {
					event.getChannel().sendMessage("The value provided is not valid for that setting").queue();
				}
			} else if (command.toLowerCase().startsWith("loggingchannel")
					&& event.getMessage().getMentionedChannels().size() > 0) {
				if (event.getMessage().getMentionedChannels().size() >= 1) {
					if (event.getMessage().getMentionedChannels().size() == 1) {
						setParser.setScopePath("LoggerSettings");
						setParser.removeValGroup("LoggerChannel");
						setParser.addVal("LoggerChannel", event.getMessage().getMentionedChannels().get(0).getId());
						event.getChannel()
								.sendMessage("Set logging channel to <#"
										+ event.getMessage().getMentionedChannels().get(0).getId() + ">")
								.queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
					} else {
						event.getChannel().sendMessage("Please only mention one channel")
								.queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
					}
				} else if (words.length == 2) {
					if (words[1].equals("disable")) {
						setParser.removeValGroup("LoggerChannel");
						event.getChannel().sendMessage("Disabled logging")
								.queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
					}
				} else {
					event.getChannel().sendMessage("The value provided is not valid for that setting").queue();
				}
			} else {
				event.getChannel().sendMessage("That is not a valid setting").queue();
			}
		} catch (InsufficientPermissionException e) {
			String permission = e.getPermission().getName();
			EmbedBuilder message = new EmbedBuilder();
			message.setTitle("Missing permission error | " + event.getGuild().getName());
			message.addField("Error message:", "Bot is missing required permission **" + permission
					+ "**. Please grant this permission to the bot's role or contact a guild administrator to apply this permission to the bot's role.",
					false);
			message.setColor(Color.red);
			event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(message.build()).queue());
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood set <setting> <value>** - Changes a server setting on the guild's settings file located on the bot server";
	}

	@Override
	public String getCommandId() {
		return "set";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_MANAGEMENT;
	}
}
