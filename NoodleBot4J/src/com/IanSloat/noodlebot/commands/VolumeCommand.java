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

public class VolumeCommand implements Command {

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
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "volume");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();
		try {
			event.getMessage().delete().queue();
			if (event.getMember().getVoiceState().inVoiceChannel()) {
				List<String> words = Arrays.asList(
						BotUtils.normalizeSentence(event.getMessage().getContentRaw().toLowerCase()).split(" "));
				if (words.size() == 3) {
					try {
						int volume = Integer.parseInt(words.get(2));
						if (volume < 0)
							volume = 0;
						if (volume > 200)
							volume = 200;
						GuildLavalinkController controller = GuildLavalinkController.getController(event.getGuild());
						if (controller.isPlaying()) {
							controller.setVolume(volume);
							controller.updateStatus();
						} else
							event.getChannel().sendMessage("Bot isn't playing anything right now!")
									.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					} catch (NumberFormatException e) {
						event.getChannel().sendMessage("Only real numbers are accepted as arguments for this command")
								.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					}
				} else if (words.size() > 3)
					event.getChannel()
							.sendMessage("Too many arguments passed for this command. Command syntax is \""
									+ BotUtils.BOT_PREFIX + "volume <0-200>\"")
							.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
				else
					event.getChannel()
							.sendMessage("Too few arguments passed for this command. Command syntax is \""
									+ BotUtils.BOT_PREFIX + "volume <0-200>\"")
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
		return "**" + BotUtils.BOT_PREFIX + "volume <0-200>** - Changes the output volume of the music player _("
				+ BotUtils.BOT_PREFIX + "help " + getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "volume";
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
