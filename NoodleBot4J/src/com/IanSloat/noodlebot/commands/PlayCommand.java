package com.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.controllers.GuildPermissionsController;
import com.IanSloat.noodlebot.controllers.lavalink.GuildLavalinkController;
import com.IanSloat.noodlebot.controllers.settings.GuildSettingsController;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

/**
 * Command that operates the music system. Currently supported by lavalink.
 */
public class PlayCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user) {
		return new GuildPermissionsController(user.getGuild()).canMemberUseCommand(user, this);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return (command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play ")
				|| command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "add "));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();
		try {
			event.getMessage().delete().queue();
			if (event.getMember().getVoiceState().inVoiceChannel()) {
				GuildLavalinkController controller = GuildLavalinkController.getController(event.getGuild());
				controller.connect(event.getMember().getVoiceState().getChannel());
				controller.setOutputChannel(event.getTextChannel());
				try {
					GuildSettingsController settings = new GuildSettingsController(event.getGuild());
					if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play ")) {
						controller.setVolume(Integer.parseInt(settings.getSetting("volume").getValue()));
						controller.loadAndPlay(
								event.getMessage().getContentRaw().substring((BotUtils.BOT_PREFIX + "play ").length()),
								settings.getSetting("autoplay").getValue().equals("on"));
					} else if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "add ")
							&& controller.isPlaying()) {
						controller.setVolume(Integer.parseInt(settings.getSetting("volume").getValue()));
						controller.addToPlaylist(
								event.getMessage().getContentRaw().substring((BotUtils.BOT_PREFIX + "add ").length()));
					} else
						event.getChannel().sendMessage("Bot isn't playing anything right now!")
								.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				event.getChannel().sendMessage("You must be connected to a voice channel to use this command")
						.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
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
		return "**" + BotUtils.BOT_PREFIX + "play <song title>** - Plays a song from the internet _("
				+ BotUtils.BOT_PREFIX + "help " + getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "play";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.PLAYER;
	}

	@Override
	public MessageEmbed getCommandHelpPage() {
		return null;
	}

}
