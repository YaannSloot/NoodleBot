package main.IanSloat.thiccbot.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.errors.MalformedTimecodeException;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.Timecode;
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
					Message commandMessage = event.getChannel().sendMessage("Track was skipped").submit().get();
					commandMessage.delete().queueAfter(5, TimeUnit.SECONDS);
				} else {
					musicManager.player.getPlayingTrack().setPosition(timecode.getMillis());
					final String time = command;
					Message commandMessage = event.getChannel().sendMessage("Set position to " + time).submit().get();
					commandMessage.delete().queueAfter(5, TimeUnit.SECONDS);
				}
			} catch (NumberFormatException | NullPointerException e) {
				Message commandMessage;
				try {
					commandMessage = event.getChannel().sendMessage("Numbers only please").submit().get();
					commandMessage.delete().queueAfter(5, TimeUnit.SECONDS);
				} catch (InterruptedException | ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (MalformedTimecodeException e) {
				Message commandMessage;
				try {
					commandMessage = event.getChannel().sendMessage("That's not a valid timecode").submit().get();
					commandMessage.delete().queueAfter(5, TimeUnit.SECONDS);
				} catch (InterruptedException | ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
