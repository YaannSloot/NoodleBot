package com.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;
import com.IanSloat.noodlebot.controllers.settings.GuildSetting;
import com.IanSloat.noodlebot.controllers.settings.GuildSettingsController;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class SetCommand implements Command {

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
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "set");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();
		try {
			event.getMessage().delete().queue();
			try {
				List<String> words = Arrays.asList(BotUtils.normalizeSentence(
						event.getMessage().getContentRaw().toLowerCase().replace(BotUtils.BOT_PREFIX + "set", ""))
						.split(" "));
				if (words.size() > 0) {
					if (words.size() == 2) {
						GuildSettingsController controller = new GuildSettingsController(event.getGuild());
						GuildSetting setting = controller.getSetting(words.get(0));
						if (setting != null) {
							List<String> acceptedValues = Arrays.asList(setting.getAcceptedValues());
							if (acceptedValues.size() == 1 && (acceptedValues.get(0).startsWith("range!")
									|| acceptedValues.get(0).startsWith("type!"))) {
								if (acceptedValues.get(0).startsWith("range!")) {
									acceptedValues.set(0, acceptedValues.get(0).replace("range!", ""));
									int lower = Integer.parseInt(acceptedValues.get(0).split("-")[0]);
									int upper = Integer.parseInt(acceptedValues.get(0).split("-")[1]);
									if (lower > upper) {
										int temp = upper;
										upper = lower;
										lower = temp;
									}
									try {
										int value = Integer.parseInt(words.get(1));
										if (value > upper)
											value = upper;
										if (value < lower)
											value = lower;
										controller.setSetting(setting.setValue("" + value)).writeSettings();
										event.getChannel()
												.sendMessage("Set setting \"" + setting.getKey() + "\" to " + value)
												.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
									} catch (NumberFormatException e) {
										event.getChannel().sendMessage(
												"Only numbers are valid values for this setting. Please specify a valid value")
												.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
									}
								} else {
									List<TextChannel> mentions = event.getMessage().getMentionedChannels();
									if (mentions.size() == 1) {
										controller.setSetting(setting.setValue(mentions.get(0).getAsMention()))
												.writeSettings();
										event.getChannel()
												.sendMessage("Set setting \"" + setting.getKey() + "\" to "
														+ mentions.get(0).getAsMention())
												.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
									} else if (words.get(1).equals("disable") || words.get(1).equals("disabled")) {
										controller.setSetting(setting.setValue("disabled")).writeSettings();
										event.getChannel()
												.sendMessage("Set setting \"" + setting.getKey() + "\" to disabled")
												.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
									} else
										event.getChannel()
												.sendMessage("Please specify one Text Channel for this setting")
												.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
								}
							} else {
								acceptedValues.forEach(s -> System.out.print(s));
								if (acceptedValues.contains(words.get(1))) {
									controller.setSetting(setting.setValue(words.get(1))).writeSettings();
									event.getChannel()
											.sendMessage("Set setting \"" + setting.getKey() + "\" to " + words.get(1))
											.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
								} else
									event.getChannel().sendMessage(
											"Specified value is not valid for this setting. Please specify a valid value")
											.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
							}
						} else
							event.getChannel()
									.sendMessage("Specified setting does not exist. Please specify a valid setting")
									.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					} else
						event.getChannel().sendMessage("No value given. Please specify a valid value")
								.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
				} else
					event.getChannel().sendMessage("No setting given. Please specify a valid setting")
							.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
			} catch (IOException e) {
				event.getAuthor().openPrivateChannel()
						.queue(c -> c.sendMessage("Error accessing guild settings file. Please contact bot owner at "
								+ event.getJDA().retrieveApplicationInfo().complete().getOwner().getAsTag()
								+ " to report this issue").queue());
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
		return "**" + BotUtils.BOT_PREFIX + "set <setting> <value>** - Changes one of the bot's settings _("
				+ BotUtils.BOT_PREFIX + "help " + getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "set";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.MANAGEMENT;
	}

	@Override
	public MessageEmbed getCommandHelpPage() {
		return new EmbedBuilder().setTitle("Settings modifier command | More Info").setColor(Color.red)
				.setDescription("**Syntax:**\n" + "_" + BotUtils.BOT_PREFIX + "set_ <setting id> <value>\n\n"
						+ "**Summary:**\n"
						+ "This command modifies guild specific settings that change how the bot functions. All ids and possible values can be viewed using the list settings command.\n\n"
						+ "**Parameters:**\n"
						+ "setting id - The special identifier for the setting you are trying to modify\n"
						+ "setting value - The new desired value to change the setting to")
				.build();
	}

}
