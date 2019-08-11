package main.IanSloat.noodlebot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import main.IanSloat.noodlebot.commands.Command;
import main.IanSloat.noodlebot.reactivecore.Button;
import main.IanSloat.noodlebot.reactivecore.ReactiveMessage;
import main.IanSloat.noodlebot.tools.MusicEmbedFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackScheduler extends AudioEventAdapter {

	static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);

	private TextChannel channel;
	private ReactiveMessage reactive;
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
			if (!(reactive == null))
				reactive.dispose();
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
		if (reactive != null) {
			reactive.dispose();
		}
		if (playlistMessage != null) {
			playlistMessage.delete().queue();
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
		return reactive.getRegisteredMessage();
	}

	public Guild getGuild() {
		return guild;
	}

	private void initNewSession() {
		MusicEmbedFactory musEmbed = new MusicEmbedFactory(player.getPlayingTrack());
		if (reactive != null)
			reactive.dispose();
		reactive = new ReactiveMessage(channel);
		reactive.setMessageContent(musEmbed.getPlaying(displayQueue, getPlaylist(), getVolume()));
		Button stopButton = new Button("U+23f9");
		stopButton.setButtonAction(() -> {
			if (stopButton.getUser() != null) {
				if (Command.getPermissionsManager(guild).authUsage("stop", guild.getMember(stopButton.getUser()))) {
					stop();
					logger.info("Player stop button has been clicked for player session in guild " + guild.getName()
							+ " (id:" + guild.getId() + ")");
				} else {
					stopButton.getUser().openPrivateChannel()
							.queue((c) -> c.sendMessage(
									"You do not have permission to use the stop button in the guild " + guild.getName())
									.queue());
				}
			}
		});
		reactive.addButton(stopButton);
		Button playPause = new Button("U+23ef");
		playPause.setButtonAction(() -> {
			if (playPause.getUser() != null) {
				if (Command.getPermissionsManager(guild).authUsage("pause", guild.getMember(playPause.getUser()))) {
					if (isPaused()) {
						unpauseTrack();
					} else {
						pauseTrack();
					}
					logger.info("Player play/pause button has been clicked for player session in guild " + guild.getName()
							+ " (id:" + guild.getId() + ")");
				} else {
					playPause.getUser().openPrivateChannel()
							.queue((c) -> c.sendMessage(
									"You do not have permission to use the play/pause button in the guild " + guild.getName())
									.queue());
				}
			}
		});
		reactive.addButton(playPause);
		Button nextTrack = new Button("U+23ed");
		nextTrack.setButtonAction(() -> {
			if (nextTrack.getUser() != null) {
				if (Command.getPermissionsManager(guild).authUsage("skip", guild.getMember(nextTrack.getUser()))) {
					nextTrack();
					logger.info("Player skip button has been clicked for player session in guild " + guild.getName()
							+ " (id:" + guild.getId() + ")");
				} else {
					nextTrack.getUser().openPrivateChannel()
							.queue((c) -> c.sendMessage(
									"You do not have permission to use the skip button in the guild " + guild.getName())
									.queue());
				}
			}
		});
		reactive.addButton(nextTrack);
		Button rewind = new Button("U+23ea");
		rewind.setButtonAction(() -> {
			if (rewind.getUser() != null) {
				if (Command.getPermissionsManager(guild).authUsage("jump", guild.getMember(rewind.getUser()))) {
					long jumpSize = player.getPlayingTrack().getDuration() / 20;
					long currentPosition = player.getPlayingTrack().getPosition();
					if (currentPosition - jumpSize < 0) {
						player.getPlayingTrack().setPosition(0);
					} else {
						player.getPlayingTrack().setPosition(currentPosition - jumpSize);
					}
					logger.info("Player rewind button has been clicked for player session in guild " + guild.getName()
							+ " (id:" + guild.getId() + ")");
				} else {
					rewind.getUser().openPrivateChannel()
							.queue((c) -> c.sendMessage(
									"You do not have permission to use the time skip button in the guild " + guild.getName())
									.queue());
				}
			}
		});
		reactive.addButton(rewind);
		Button fastForward = new Button("U+23e9");
		fastForward.setButtonAction(() -> {
			if (fastForward.getUser() != null) {
				if (Command.getPermissionsManager(guild).authUsage("jump", guild.getMember(fastForward.getUser()))) {
					long jumpSize = player.getPlayingTrack().getDuration() / 20;
					long currentPosition = player.getPlayingTrack().getPosition();
					if (currentPosition + jumpSize > player.getPlayingTrack().getDuration()) {
						nextTrack();
					} else {
						player.getPlayingTrack().setPosition(currentPosition + jumpSize);
					}
					logger.info("Player fast forward button has been clicked for player session in guild " + guild.getName()
							+ " (id:" + guild.getId() + ")");
				} else {
					fastForward.getUser().openPrivateChannel()
							.queue((c) -> c.sendMessage(
									"You do not have permission to use the time skip button in the guild " + guild.getName())
									.queue());
				}
			}
		});
		reactive.addButton(fastForward);
		Button playlistDisplay = new Button("U+2139");
		playlistDisplay.setButtonAction(() -> {
			if (playlistDisplay.getUser() != null) {
				if (Command.getPermissionsManager(guild).authUsage("showqueue", guild.getMember(playlistDisplay.getUser()))) {
					if(displayQueue) {
						setPlaylistDisplay(false);
					} else {
						setPlaylistDisplay(true);
					}
					logger.info("Player playlist display button has been clicked for player session in guild " + guild.getName()
							+ " (id:" + guild.getId() + ")");
				} else {
					playlistDisplay.getUser().openPrivateChannel()
							.queue((c) -> c.sendMessage(
									"You do not have permission to use the playlist display button in the guild " + guild.getName())
									.queue());
				}
			}
		});
		reactive.addButton(playlistDisplay);
		reactive.activate();
	}

	public void updateStatus() {
		channel.getHistory().retrievePast(1).queue(new Consumer<List<Message>>() {

			@Override
			public void accept(List<Message> t) {
				try {
					if (t.get(0).equals(reactive.getRegisteredMessage())) {
						MusicEmbedFactory musEmbed = new MusicEmbedFactory(player.getPlayingTrack());
						reactive.setMessageContent(musEmbed.getPlaying(displayQueue, getPlaylist(), getVolume()));
						reactive.update();
					} else {
						initNewSession();
					}
				} catch (NullPointerException e) {
					initNewSession();
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
