package com.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;
import com.IanSloat.noodlebot.controllers.settings.GuildSettings;
import com.IanSloat.noodlebot.controllers.settings.GuildSettingsController;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

// TODO add additional functionality for settings help

/**
 * Command for sending a list of the current settings applied for a specific
 * guild to the channel it was executed in.
 */
public class ListSettingsCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user) {
		try {
			return new GuildPermissionsController(user.getGuild()).canMemberUseCommand(user, this);
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return (command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "list settings")
				|| command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "settings"));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();
		try {
			try {
				event.getMessage().delete().queue();
				GuildSettingsController settingController = new GuildSettingsController(event.getGuild());
				GuildSettings settings = settingController.getSettings();
				settings.sortAlphabetically();
				EmbedBuilder message = new EmbedBuilder();
				message.setTitle("Settings | " + event.getGuild().getName());
				Map<String, String> fieldMap = new HashMap<>();
				settings.getCategories().forEach(ct -> fieldMap.put(ct, ""));
				settings.forEach(s -> fieldMap.replace(s.getCategory(),
						fieldMap.get(s.getCategory()) + s.getTitle() + " = " + s.getValue() + " `" + "<" + s.getKey()
								+ ">" + Arrays.toString(s.getAcceptedValues()) + "`\n"));
				fieldMap.forEach(
						(title, contents) -> message.addField(BotUtils.capitalizeWords(title), contents, false));
				message.setColor(new Color(62, 180, 137));
				message.setFooter("Guide: <setting id>[possible values]");
				event.getChannel().sendMessage(message.build()).queue();
			} catch (IOException e) {
				e.printStackTrace();
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
			// TODO Add event for logger when it is complete
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**" + BotUtils.BOT_PREFIX + "list settings** - Lists the bot's current settings _("
				+ BotUtils.BOT_PREFIX + "help " + getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "settings";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.MANAGEMENT;
	}

	@Override
	public MessageEmbed getCommandHelpPage() {
		return new EmbedBuilder().setTitle("Settings command | More Info").setColor(Color.red).setDescription(
				"**Syntax:**\n_nood settings_\n_nood list settings_\n\n**Summary:**\nLists the current value for each setting. Different settings can affect the way the bot functions. These settings are unique to each guild.")
				.build();
	}

}
