package com.IanSloat.noodlebot.controllers.lavalink;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import com.IanSloat.noodlebot.reactivecore.Button;
import com.IanSloat.noodlebot.reactivecore.ReactiveMessage;
import com.IanSloat.noodlebot.tools.MusicEmbedFactory;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import lavalink.client.player.IPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

class LavalinkTrackManager extends PlayerEventListenerAdapter {

	private ReactiveMessage message;
	private TextChannel channel;
	private final BlockingQueue<AudioTrack> queue;
	private final IPlayer player;
	private boolean displayQueue = false;

	public LavalinkTrackManager(IPlayer player) {
		this.queue = new LinkedBlockingQueue<>();
		this.player = player;
	}

	public void setChannel(TextChannel channel) {
		if (channel != null)
			if (!(channel.equals(this.channel))) {
				if (message != null)
					message.dispose();
				this.channel = channel;
			}
	}

	public TextChannel getChannel() {
		return channel;
	}

	public void queue(AudioTrack track) {
		if (player.getPlayingTrack() != null)
			queue.offer(track);
		else
			player.playTrack(track);
	}

	public void reset() {
		player.stopTrack();
		queue.clear();
		if (message != null)
			message.dispose();
	}

	public void setPlaylistDisplay(boolean value) {
		displayQueue = value;
		updateStatus();
	}

	private void initNewSession() {
		MusicEmbedFactory musEmbed = new MusicEmbedFactory(player.getPlayingTrack());
		if (message != null)
			message.dispose();
		message = new ReactiveMessage(channel);
		message.setMessageContent(musEmbed.getPlaying(displayQueue, getPlaylist(), player.getVolume()));
		Button stopButton = new Button("U+23f9");
		stopButton.setButtonAction(() -> {
			if (stopButton.getUser() != null) {
				if (true) {
					reset();
				} else {
					stopButton.getUser().openPrivateChannel().queue(
							(c) -> c.sendMessage("You do not have permission to use the stop button in the guild "
									+ channel.getGuild().getName()).queue());
				}
			}
		});
		message.addButton(stopButton);
		Button playPause = new Button("U+23ef");
		playPause.setButtonAction(() -> {
			if (playPause.getUser() != null) {
				if (true) {
					if (player.isPaused()) {
						player.setPaused(false);
					} else {
						player.setPaused(true);
					}
				} else {
					playPause.getUser().openPrivateChannel().queue(
							(c) -> c.sendMessage("You do not have permission to use the play/pause button in the guild "
									+ channel.getGuild().getName()).queue());
				}
			}
		});
		message.addButton(playPause);
		Button nextTrack = new Button("U+23ed");
		nextTrack.setButtonAction(() -> {
			if (nextTrack.getUser() != null) {
				if (true) {
					nextTrack();
				} else {
					nextTrack.getUser().openPrivateChannel().queue(
							(c) -> c.sendMessage("You do not have permission to use the skip button in the guild "
									+ channel.getGuild().getName()).queue());
				}
			}
		});
		message.addButton(nextTrack);
		Button rewind = new Button("U+23ea");
		rewind.setButtonAction(() -> {
			if (rewind.getUser() != null) {
				if (true) {
					long jumpSize = player.getPlayingTrack().getDuration() / 20;
					long currentPosition = player.getTrackPosition();
					if (currentPosition - jumpSize < 0) {
						player.seekTo(0);
					} else {
						player.seekTo(currentPosition - jumpSize);
					}
				} else {
					rewind.getUser().openPrivateChannel().queue(
							(c) -> c.sendMessage("You do not have permission to use the time skip button in the guild "
									+ channel.getGuild().getName()).queue());
				}
			}
		});
		message.addButton(rewind);
		Button fastForward = new Button("U+23e9");
		fastForward.setButtonAction(() -> {
			if (fastForward.getUser() != null) {
				if (true) {
					long jumpSize = player.getPlayingTrack().getDuration() / 20;
					long currentPosition = player.getTrackPosition();
					if (currentPosition + jumpSize > player.getPlayingTrack().getDuration()) {
						nextTrack();
					} else {
						player.seekTo(currentPosition + jumpSize);
					}
				} else {
					fastForward.getUser().openPrivateChannel().queue(
							(c) -> c.sendMessage("You do not have permission to use the time skip button in the guild "
									+ channel.getGuild().getName()).queue());
				}
			}
		});
		message.addButton(fastForward);
		Button playlistDisplay = new Button("U+2139");
		playlistDisplay.setButtonAction(() -> {
			if (playlistDisplay.getUser() != null) {
				if (true) {
					if (displayQueue) {
						setPlaylistDisplay(false);
					} else {
						setPlaylistDisplay(true);
					}
				} else {
					playlistDisplay.getUser().openPrivateChannel()
							.queue((c) -> c.sendMessage(
									"You do not have permission to use the playlist display button in the guild "
											+ channel.getGuild().getName())
									.queue());
				}
			}
		});
		message.addButton(playlistDisplay);
		message.activate();
	}

	public void updateStatus() {
		channel.getHistory().retrievePast(1).queue(new Consumer<List<Message>>() {

			@Override
			public void accept(List<Message> t) {
				if(message != null)
					if (t.get(0).equals(message.getRegisteredMessage())) {
						MusicEmbedFactory musEmbed = new MusicEmbedFactory(player.getPlayingTrack());
						message.setMessageContent(musEmbed.getPlaying(displayQueue, getPlaylist(), player.getVolume()));
						message.update();
					} else {
						initNewSession();
					}
				else
					initNewSession();
			}

		});
	}

	public List<AudioTrack> getPlaylist() {
		return new ArrayList<AudioTrack>(queue);
	}

	public void nextTrack() {
		player.playTrack(queue.poll());
	}

	/**
	 * @param player Audio player
	 */
	public void onPlayerPause(IPlayer player) {
		// Adapter dummy method
	}

	/**
	 * @param player Audio player
	 */
	public void onPlayerResume(IPlayer player) {
		// Adapter dummy method
	}

	/**
	 * @param player Audio player
	 * @param track  Audio track that started
	 */
	public void onTrackStart(IPlayer player, AudioTrack track) {
		updateStatus();
	}

	/**
	 * @param player    Audio player
	 * @param track     Audio track that ended
	 * @param endReason The reason why the track stopped playing
	 */
	public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			nextTrack();
		}
	}

	/**
	 * @param player    Audio player
	 * @param track     Audio track where the exception occurred
	 * @param exception The exception that occurred
	 */
	public void onTrackException(IPlayer player, AudioTrack track, Exception exception) {
		// Adapter dummy method
	}

	/**
	 * @param player      Audio player
	 * @param track       Audio track where the exception occurred
	 * @param thresholdMs The wait threshold that was exceeded for this event to
	 *                    trigger
	 */
	public void onTrackStuck(IPlayer player, AudioTrack track, long thresholdMs) {
		// Adapter dummy method
	}

}
