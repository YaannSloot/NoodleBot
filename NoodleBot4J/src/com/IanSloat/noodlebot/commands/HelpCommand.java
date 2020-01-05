package com.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.events.CommandController;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

/**
 * Basic help command. Usage is always allowed and cannot be restricted.
 */
public class HelpCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user) {
		return true;
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		String commandData = command.getContentRaw();
		commandData = BotUtils.normalizeSentence(commandData);
		List<String> words = Arrays.asList(commandData.split(" "));
		if (words.size() >= 2)
			return (words.get(0).toLowerCase().equals(BotUtils.BOT_PREFIX.toLowerCase().trim())
					&& words.get(1).toLowerCase().equals("help")) ? true : false;
		else
			return false;
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();
		List<String> missingPermissions = new ArrayList<String>();
		try {
			event.getMessage().delete().queue();
		} catch (InsufficientPermissionException e) {
			missingPermissions.add(e.getPermission().getName());
		}
		if (!event.getGuild().getSelfMember().hasPermission(Permission.VOICE_MOVE_OTHERS)) {
			missingPermissions.add(Permission.VOICE_MOVE_OTHERS.getName());
		}
		String commandData = event.getMessage().getContentRaw();
		commandData = BotUtils.normalizeSentence(commandData);
		List<String> words = Arrays.asList(commandData.split(" "));
		if (words.size() == 2) {
			Map<CommandCategory, String> commandSnippets = new HashMap<>();
			for (Command c : CommandController.commandList) {
				if (c.CheckUsagePermission(event.getMember()))
					if (commandSnippets.replace(c.getCommandCategory(),
							commandSnippets.get(c.getCommandCategory()) + '\n' + c.getHelpSnippet()) == null)
						commandSnippets.put(c.getCommandCategory(), c.getHelpSnippet());
			}
			List<Field> fields = new ArrayList<Field>();
			for (CommandCategory ct : CommandCategory.values()) {
				if (commandSnippets.containsKey(ct))
					fields.add(new Field("**" + BotUtils.capitalizeWords(ct.toString()) + "**", commandSnippets.get(ct),
							false));
			}
			EmbedBuilder message = new EmbedBuilder();
			message.setTitle("Available commands | " + event.getMember().getUser().getName() + " | "
					+ event.getGuild().getName());
			fields.forEach(f -> message.addField(f));
			message.setColor(Color.red);
			event.getAuthor().openPrivateChannel().queue(c -> c.sendMessage(message.build()).queue());
		} else {
			List<String> commandIds = new ArrayList<String>();
			CommandController.commandList.forEach(c -> commandIds.add(c.getCommandId()));
			if (commandIds.contains(words.get(2))) {
				for (Command c : CommandController.commandList) {
					if (c.getCommandId().equals(words.get(2))) {
						event.getAuthor().openPrivateChannel()
								.queue(ch -> ch.sendMessage(c.getCommandHelpPage()).queue());
						break;
					}
				}
			} else {
				event.getAuthor().openPrivateChannel()
						.queue(ch -> ch.sendMessage(new EmbedBuilder().setTitle("Command not found")
								.appendDescription("The command you are referencing does not exist").setColor(Color.red)
								.build()).queue());
			}
		}
		if (missingPermissions.size() > 0) {
			EmbedBuilder error = new EmbedBuilder();
			String permissionField = "";
			for (String perm : missingPermissions)
				permissionField = permissionField.concat("**" + perm + "**\n");
			error.setTitle("Permission warning | " + event.getGuild().getName());
			error.addField("Missing permissions:", permissionField, false);
			error.addField("Info:",
					"The following permissions are required for the bot to function. Please enable them or contact a guild administrator and request that they enable these permissions for the bot's primary role.",
					false);
			error.setColor(Color.orange);
			event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(error.build()).queue());
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**" + BotUtils.BOT_PREFIX + " help** - Lists available commands _(" + BotUtils.BOT_PREFIX + " help "
				+ getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "help";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.GENERAL;
	}

	@Override
	public MessageEmbed getCommandHelpPage() {
		return new EmbedBuilder().setTitle("Help command | More Info").setColor(Color.red).setDescription(
				"**Syntax:**\n_nood help_ (command id)\n\n**Summary:**\nShows every command available to the user. Entries for commands the user does not have access to will be left out. Additionally, more detailed help entries for each command can be viewed by typing a particular command id after \"nood help\". If you don't know what to type, it is listed after each help entry.\n\n**Parameters:**\n(Optional) command id - Displays more details for a particular command as well as how to use it.\nExample:\nnood help help - will show more details about the help command.")
				.build();
	}

}
