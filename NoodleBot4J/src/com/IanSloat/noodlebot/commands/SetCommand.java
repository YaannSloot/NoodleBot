package com.IanSloat.noodlebot.commands;

import java.awt.Color;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.controllers.GuildPermissionsController;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class SetCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user) {
		return new GuildPermissionsController(user.getGuild()).canMemberUseCommand(user, this);
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
		// TODO Auto-generated method stub
		return null;
	}

}
