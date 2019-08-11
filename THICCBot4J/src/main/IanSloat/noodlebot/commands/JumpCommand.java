package main.IanSloat.noodlebot.commands;

import java.util.concurrent.TimeUnit;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.errors.MalformedTimecodeException;
import main.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.Timecode;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class JumpCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage("jump", user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "jump");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		VoiceChannel voiceChannel = event.getGuild().getAudioManager().getConnectedChannel();
		if (voiceChannel != null) {
			GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getTextChannel());
			String command = event.getMessage().getContentRaw();
			command = command.replace(BotUtils.BOT_PREFIX + "jump", "");
			command = BotUtils.normalizeSentence(command);
			command = command.replace(" ", "");
			Timecode timecode = new Timecode(command);
			try {
				timecode.decode();
				long currentLength = musicManager.player.getPlayingTrack().getDuration();
				if (timecode.getMillis() > currentLength) {
					musicManager.player.playTrack(null);
					musicManager.scheduler.nextTrack();
					event.getChannel().sendMessage("Track was skipped").queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
				} else {
					musicManager.player.getPlayingTrack().setPosition(timecode.getMillis());
					final String time = command;
					event.getChannel().sendMessage("Set position to " + time).queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
				}
			} catch (NumberFormatException | NullPointerException e) {
				event.getChannel().sendMessage("Numbers only please").queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
			} catch (MalformedTimecodeException e) {
				event.getChannel().sendMessage("That's not a valid timecode").queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		}
	}
}
