package com.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.controllers.lavalink.GuildLavalinkController;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;
import com.IanSloat.noodlebot.errors.MalformedTimecodeException;
import com.IanSloat.noodlebot.tools.Timecode;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class JumpCommand implements Command {

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
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "jump");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();

		try {
			event.getMessage().delete().queue();
			if (event.getMember().getVoiceState().inVoiceChannel()) {
				GuildLavalinkController controller = GuildLavalinkController.getController(event.getGuild());
				if (controller.isPlaying()) {
					List<String> commandWords = Arrays.asList(
							BotUtils.normalizeSentence(event.getMessage().getContentRaw().toLowerCase()).split(" "));
					Timecode time = new Timecode(commandWords.get(commandWords.size() - 1));
					try {
						time.decode();
						controller.jumpToPosition(time.getMillis());
					} catch (MalformedTimecodeException e) {
						event.getChannel().sendMessage("Timecode format is incorrect. Please use HH:MM:SS formatting")
								.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					} catch (NumberFormatException e) {
						event.getChannel().sendMessage("Please only use real numbers 0-9 and : as a delimiter")
								.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					}
				} else
					event.getChannel().sendMessage("No tracks are currently playing")
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
				+ "jump <timestamp>** - Changes the position of the current track to the specified timestamp _("
				+ BotUtils.BOT_PREFIX + "help " + getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "jump";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.PLAYER;
	}

	@Override
	public MessageEmbed getCommandHelpPage() {
		return new EmbedBuilder().setTitle("Track position jump command | More Info").setColor(Color.red).setDescription(
				"**Syntax:**\n"
				+ "_" + BotUtils.BOT_PREFIX + "jump_ <timestamp>\n\n"
				+ "**Summary:**\n"
				+ "This command sets the time position of the current track to whatever is specified in the command arguments. "
				+ "Since you can't actually use a scrub bar during playback, this command is used instead. This command also has two reaction button "
				+ "equivalents found on the player dialog box. These buttons skip the current time forward or backward 5% of the track length.\n\n"
				+ "**Parameters:**\n"
				+ "timestamp - The desired position to jump to. Must be in HH:MM:SS format. Shorter timecodes in MM:SS or Seconds format are also accepted."
				).build();
	}

}
