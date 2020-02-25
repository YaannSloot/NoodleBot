package com.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.controllers.lavalink.GuildLavalinkController;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class RemoveTrackCommand extends Command {

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
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "remove track")
				|| command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "remove tracks");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();
		try {
			event.getMessage().delete().queue();
			GuildLavalinkController controller = GuildLavalinkController.getController(event.getGuild());
			if (event.getMember().getVoiceState().inVoiceChannel()) {
				if (event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
					if (event.getMember().getVoiceState().getChannel()
							.equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
						if (controller.isPlaying()) {
							if (controller.getPlaylist().size() > 0) {
								List<String> words = Arrays.asList(
										BotUtils.normalizeSentence(event.getMessage().getContentRaw()).split(" "));
								if (words.size() == 4) {
									try {
										int targetIndex = Integer.parseInt(words.get(3));
										if (controller.removeFromQueue(targetIndex))
											controller.updateStatus();
										else
											event.getChannel().sendMessage("Specified track id is out of range.")
													.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
									} catch (NumberFormatException e) {
										String[] targets = words.get(3).split("-");
										if (targets.length == 2) {
											try {
												int[] targetIds = new int[2];
												targetIds[0] = Integer.parseInt(targets[0]);
												targetIds[1] = Integer.parseInt(targets[1]);
												if (controller.removeFromQueue(targetIds[0], targetIds[1]))
													controller.updateStatus();
												else
													event.getChannel()
															.sendMessage("Specified track ids are out of range.")
															.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
											} catch (NumberFormatException e1) {
												event.getChannel().sendMessage(
														"Please use only numbers when referencing target tracks in the queue")
														.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
											}
										} else if (targets.length < 2)
											event.getChannel().sendMessage(
													"Please use only numbers when referencing target tracks in the queue")
													.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
										else
											event.getChannel().sendMessage(
													"Too many tracks specified. Please only list two tracks when specifying a range of tracks")
													.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
									}
								} else
									event.getChannel().sendMessage(
											"Too many or too few arguments provided. Please use a valid number of arguments when using this command.")
											.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
							} else
								event.getChannel().sendMessage("The queue is currently empty")
										.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
						} else
							event.getChannel().sendMessage("No tracks are currently playing")
									.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					} else
						event.getChannel().sendMessage(
								"You must be connected to the same voice channel that the bot is connected to to use this command")
								.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
				} else
					event.getChannel().sendMessage("The bot is not currently connected to any voice channel")
							.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
			} else
				event.getChannel().sendMessage("You must be connected to a voice channel to use this command")
						.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
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
		return "**" + BotUtils.BOT_PREFIX
				+ "remove track (track #|track # range)** - Removes tracks from the track queue _("
				+ BotUtils.BOT_PREFIX + "help " + getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "removetrack";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.PLAYER;
	}

	@Override
	public MessageEmbed getCommandHelpPage() {
		// TODO Auto-generated method stub
		return null;
	}

}
