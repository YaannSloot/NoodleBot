package main.IanSloat.thiccbot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import main.IanSloat.thiccbot.tools.MusicEmbedFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class TrackScheduler extends AudioEventAdapter {

	private TextChannel channel;
	private Message message;
	private Message playlistMessage;
	private Guild guild;
	private final BlockingQueue<AudioTrack> queue;
	private final AudioPlayer player;
	private boolean displayQueue = false;
	private int trackedVolume = 100;

	public TrackScheduler(TextChannel channel, AudioPlayer player) {
		this.channel = channel;
		this.guild = channel.getGuild();
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
	}

	public void setVolume(int volume) {
		trackedVolume = volume;
		player.setVolume(volume);
	}

	public int getVolume() {
		return trackedVolume;
	}

	public void setChannel(TextChannel channel) {
		if (!(channel.equals(this.channel))) {
			if (!(message == null))
				message.delete();
			this.channel = channel;
		}
	}

	public void queue(AudioTrack track) {
		// Calling startTrack with the noInterrupt set to true will start the track only
		// if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the
		// player was already playing so this
		// track goes to the queue instead.
		if (!player.startTrack(track, true)) {
			queue.offer(track);
		}
	}

	public void setPlaylistDisplay(boolean value) {
		displayQueue = value;
		updateStatus();
	}

	public List<AudioTrack> getPlaylist() {
		return new ArrayList<AudioTrack>(queue);
	}

	public void stop() {
		player.stopTrack();
		queue.clear();
		if (message != null) {
			message.delete();
		}
		if (playlistMessage != null) {
			playlistMessage.delete();
		}
	}

	public void pauseTrack() {
		player.setPaused(true);
	}

	public void unpauseTrack() {
		player.setPaused(false);
	}

	public boolean isPaused() {
		return player.isPaused();
	}

	public boolean removeTrackFromQueue(int trackNumber) {
		boolean result = false;
		if (trackNumber <= queue.size()) {
			trackNumber--;
			result = queue.remove(this.getPlaylist().get(trackNumber));
		}
		return result;
	}

	public void stopTrack() {
		player.stopTrack();
	}

	public void nextTrack() {
		// Start the next track, regardless of if something is already playing or not.
		// In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the
		// player.
		player.startTrack(queue.poll(), false);
	}

	public Message getLastMessage() {
		return message;
	}

	public Guild getGuild() {
		return guild;
	}

	public void updateStatus() {
		channel.getHistory().retrievePast(1).queue(new Consumer<List<Message>>() {

			@Override
			public void accept(List<Message> t) {
				if (t.get(0).equals(message)) {
					MusicEmbedFactory musEmbed = new MusicEmbedFactory(player.getPlayingTrack());
					message.editMessage(musEmbed.getPlaying(displayQueue, getPlaylist(), getVolume())).queue();
				} else {
					MusicEmbedFactory musEmbed = new MusicEmbedFactory(player.getPlayingTrack());
					if (message != null)
						message.delete().queue();
					channel.sendMessage(musEmbed.getPlaying(displayQueue, getPlaylist(), getVolume())).queue((m) -> message = m);
				}
			}
			
		});
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
		updateStatus();
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			nextTrack();
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
