package main.IanSloat.thiccbot;

import com.arsenarsen.lavaplayerbridge.player.Playlist;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import main.IanSloat.thiccbot.tools.MusicEmbedFactory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;
import java.util.List;

public class TrackScheduler extends AudioEventAdapter {

	private IChannel channel;
	private IMessage message;
	private IMessage playlistMessage;
	private String playlistTitle = "";
	private IGuild guild;
	public static final int PLAYLIST = 1;
	public static final int VIDEO = 0;
	private int bannerMode = 0;

	public TrackScheduler(IChannel channel) {
		this.channel = channel;
		this.guild = channel.getGuild();
	}

	public void setChannel(IChannel channel) {
		if (!(channel.equals(this.channel))) {
			if (!(message == null))
				message.delete();
			this.channel = channel;
		}
	}
	
	public IMessage getLastMessage() {
		return message;
	}

	public IGuild getGuild() {
		return guild;
	}

	public void setBannerMode(int mode) {
		bannerMode = mode;
	}

	public void setPlaylistMessage(IMessage message) {
		playlistMessage = message;
	}
	
	@Override
	public void onPlayerPause(AudioPlayer player) {
		// Player was paused
	}

	@Override
	public void onPlayerResume(AudioPlayer player) {
		// Player was resumed
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		if (channel.getMessageHistory(1).getLatestMessage().equals(message)) {
			MusicEmbedFactory musEmbed = new MusicEmbedFactory(track);
			RequestBuffer.request(() -> message.edit(musEmbed.getPlaying()));
		} else {
			MusicEmbedFactory musEmbed = new MusicEmbedFactory(track);
			if (message != null)
				message.delete();
			message = RequestBuffer.request(() -> {
				return channel.sendMessage(musEmbed.getPlaying());
			}).get();
		}
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason == AudioTrackEndReason.FINISHED) {
			if (Events.manager.getPlayer(channel.getGuild().getStringID()).getPlaylist().size() + bannerMode == 0) {
				message.delete();
			}
		}

		// endReason == FINISHED: A track finished or died by an exception (mayStartNext
		// = true).
		// endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
		// endReason == STOPPED: The player was stopped.
		// endReason == REPLACED: Another track started playing while this had not
		// finished
		// endReason == CLEANUP: Player hasn't been queried for a while, if you want you
		// can put a
		// clone of this back to your queue
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		// An already playing track threw an exception (track end event will still be
		// received separately)
	}

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		// Audio track has been unable to provide us any audio, might want to just start
		// a new track
	}
}
