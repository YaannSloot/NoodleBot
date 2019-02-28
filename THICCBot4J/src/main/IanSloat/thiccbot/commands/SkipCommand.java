package main.IanSloat.thiccbot.commands;

import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.threadbox.MessageDeleteTools;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.RequestBuffer;

public class SkipCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.SKIP, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "skip");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
		if (voiceChannel != null) {
			GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
			List<AudioTrack> tracks = musicManager.scheduler.getPlaylist();
			if (tracks.isEmpty()) {
				if (musicManager.player.getPlayingTrack() != null) {
					musicManager.player.playTrack(null);
					musicManager.scheduler.nextTrack();
					IMessage skipMessage = RequestBuffer.request(() -> {
						return event.getChannel().sendMessage("Track skipped");
					}).get();
					MessageDeleteTools.DeleteAfterMillis(skipMessage, 5000);
				}
			} else if (tracks.size() > 0) {
				musicManager.player.playTrack(null);
				musicManager.scheduler.nextTrack();
				IMessage skipMessage = RequestBuffer.request(() -> {
					return event.getChannel().sendMessage("Track skipped");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(skipMessage, 5000);
			} else {
				IMessage skipMessage = RequestBuffer.request(() -> {
					return event.getChannel().sendMessage("No tracks are playing or queued");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(skipMessage, 5000);
			}
		} else {
			IMessage skipMessage = RequestBuffer.request(() -> {
				return event.getChannel().sendMessage("Not currently connected to any voice channels");
			}).get();
			MessageDeleteTools.DeleteAfterMillis(skipMessage, 5000);
		}
	}
}
