package com.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.NoodleBotMain;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

/**
 * Command for sending info about the guild the bot is in and info about the bot
 * to the channel the command was executed in.
 */
public class StatusCommand extends Command {

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
		return command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "status");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();
		try {
			event.getMessage().delete().queue();
			EmbedBuilder message = new EmbedBuilder();
			message.setColor(Color.cyan);
			message.setThumbnail(event.getGuild().getIconUrl());
			message.setTitle("Status | " + event.getGuild().getName());
			message.addField("**Guild stats:**",
					"Name: " + event.getGuild().getName() + "\nMembers: " + event.getGuild().getMembers().size()
							+ "\nChannels: " + event.getGuild().getTextChannels().size() + " text channels, "
							+ event.getGuild().getVoiceChannels().size() + " voice channels, "
							+ event.getGuild().getCategories().size() + " channel categories" + "\nAFK Channel: "
							+ ((event.getGuild().getAfkChannel() != null) ? ":loud_sound:"
									+ event.getGuild().getAfkChannel().getName() : "DISABLED")
							+ "\nSystem message channel: "
							+ ((event.getGuild().getSystemChannel() != null) ? event.getGuild().getSystemChannel()
									.getAsMention() : "DISABLED")
							+ "\nRoles: " + event.getGuild().getRoles().size() + "\nAdmin roles: "
							+ event.getGuild().getRoles().stream()
									.filter(r -> r.hasPermission(Permission.ADMINISTRATOR) && event.getGuild()
											.getMembers().stream().filter(m -> m.getRoles().contains(r))
											.collect(Collectors.toList()).size() > 0)
									.collect(Collectors.toList()).size()
							+ " active, "
							+ event.getGuild().getRoles().stream()
									.filter(r -> r.hasPermission(Permission.ADMINISTRATOR)).collect(Collectors.toList())
									.size()
							+ " total" + "\nMember verification level: " + event.getGuild().getVerificationLevel()
							+ "\nContent filter settings: " + event.getGuild().getExplicitContentLevel()
							+ "\nNotification settings: " + event.getGuild().getDefaultNotificationLevel()
							+ "\nTime created: "
							+ new SimpleDateFormat("MM/dd/yyyy")
									.format(Date.from(event.getGuild().getTimeCreated().toInstant()))
							+ "\nRegion: " + event.getGuild().getRegion(),
					false);
			message.addField("**Bot status:**", "Version: " + NoodleBotMain.botVersion + "\nGuilds: "
					+ event.getJDA().getShardManager().getGuilds().size() + "\nShards: "
					+ event.getJDA().getShardManager().getShardsTotal() + "\nCurrent shard: "
					+ event.getJDA().getShardInfo().getShardId() + "\nWeb dashboard gateway: "
					+ NoodleBotMain.server.getStatus() + "\n JVM thread count: " + Thread.activeCount()
					+ "\nPowered by [JDA](https://github.com/DV8FromTheWorld/JDA) | Report issues [here](https://github.com/YaannSloot/NoodleBot/issues)",
					false);

			event.getChannel().sendMessage(message.build()).queue();
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
		return "**" + BotUtils.BOT_PREFIX + "status** - Shows stats about the bot and the guild it's in _("
				+ BotUtils.BOT_PREFIX + "help " + getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "status";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public MessageEmbed getCommandHelpPage() {
		return new EmbedBuilder().setTitle("Status command | More Info").setColor(Color.red).setDescription(
				"**Syntax:**\n_nood status_\n\n**Summary:**\nThis command displays information about the guild the bot is in as well as information and statistics about the bot")
				.build();
	}

}
