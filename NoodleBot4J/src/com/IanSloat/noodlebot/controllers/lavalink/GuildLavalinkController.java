package com.IanSloat.noodlebot.controllers.lavalink;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.IanSloat.noodlebot.NoodleBotMain;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import lavalink.client.LavalinkUtil;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.IPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

/**
 * Used for controlling a lavalink session in a specific guild.
 */
public class GuildLavalinkController {

	private static final Map<Guild, GuildLavalinkController> controllerCache = new HashMap<>();

	private final JdaLink link;
	private final IPlayer player;
	private final LavalinkTrackManager manager;
	private SearchTarget target;

	/**
	 * The target site that the controller should use to search for songs.
	 */
	public enum SearchTarget {
		YOUTUBE("ytsearch:"), SOUNDCLOUD("scsearch:");

		private String prefix;

		SearchTarget(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public String toString() {
			return prefix;
		}

	}

	/**
	 * Retrieves a {@linkplain GuildLavalinkController} for the specified guild. All
	 * controllers are cached and you cannot instantiate new controllers manually.
	 * This method is used instead.
	 * 
	 * @param guild The guild to retrieve a {@linkplain GuildLavalinkController}
	 *              instance for
	 * @return The {@linkplain GuildLavalinkController} instance associated with the
	 *         specified guild
	 */
	public static synchronized GuildLavalinkController getController(Guild guild) {
		if (controllerCache.get(guild) == null) {
			controllerCache.put(guild, new GuildLavalinkController(guild));
		}
		return controllerCache.get(guild);
	}

	private GuildLavalinkController(Guild guild) {
		this.link = NoodleBotMain.lavalink.getLink(guild);
		this.player = this.link.getPlayer();
		this.manager = new LavalinkTrackManager(player);
		this.player.addListener(manager);
		this.target = SearchTarget.YOUTUBE;
	}

	/**
	 * Connects the bot to the specified {@linkplain VoiceChannel}
	 * 
	 * @param channel The channel to connect the bot to
	 */
	public void connect(VoiceChannel channel) {
		if (link.getChannel() != null)
			if (!link.getChannel().equals(channel.getId()))
				manager.reset();
		link.connect(channel);
	}

	/**
	 * Disconnects the bot from whatever {@linkplain VoiceChannel} it is connected
	 * to, if it is connected to a channel. Will also reset the track manager and
	 * reset the target {@linkplain TextChannel}
	 */
	public void disconnect() {
		manager.reset();
		link.disconnect();
	}

	/**
	 * Sets the target {@linkplain TextChannel} that the bot should send music
	 * player status messages to. This must not be null when playing a new
	 * {@linkplain AudioTrack}
	 * 
	 * @param channel The {@linkplain TextChannel} to send status messages to
	 */
	public void setOutputChannel(TextChannel channel) {
		manager.setChannel(channel);
	}

	/**
	 * Toggles whether the current track is paused or not if applicable
	 * 
	 * @return True if the player was paused as a result of using this method
	 */
	public boolean playPauseToggle() {
		boolean result = false;
		if (isPlaying()) {
			if (player.isPaused())
				player.setPaused(false);
			else {
				player.setPaused(true);
				result = true;
			}
		}
		return result;
	}

	/**
	 * Resets the track manager
	 */
	public void resetQueue() {
		manager.reset();
	}

	/**
	 * Sets the volume that the session should play {@linkplain AudioTrack}s at
	 * 
	 * @param volume An integer representing the volume percentage that the session
	 *               should play tracks at. Must be from 0-1000.
	 */
	public void setVolume(int volume) {
		player.setVolume(volume);
	}

	/**
	 * Sets the position of the current track if applicable
	 * 
	 * @param position The position to seek to
	 */
	public void jumpToPosition(long position) {
		if (isPlaying())
			player.seekTo(position);
	}

	/**
	 * The {@linkplain SearchTarget} to use when searching for new tracks
	 * 
	 * @param target A {@linkplain SearchTarget} representing which site to use when
	 *               searching for tracks. Defaults to YouTube.
	 */
	public void setSearchTarget(SearchTarget target) {
		this.target = target;
	}

	/**
	 * Checks whether the session is currently playing an {@linkplain AudioTrack}
	 * 
	 * @return True if the session is currently playing a track
	 */
	public boolean isPlaying() {
		return player.getPlayingTrack() != null;
	}

	/**
	 * Loads tracks from a search term or url. Will reset the track manager.
	 * 
	 * @param target   The search term or url to retrieve tracks from
	 * @param autoplay Whether to load multiple tracks from a search. Irrelevant to
	 *                 urls.
	 * @return True if the load was successful
	 */
	public boolean loadAndPlay(String target, boolean autoplay) {
		if (manager.getChannel() != null) {
			if (!(target.startsWith("http://") || target.startsWith("https://")))
				target = this.target.toString() + target;
			List<AudioTrack> tracks = searchForTracks(target);
			if (tracks.size() > 0) {
				manager.reset();
				if (target.startsWith("ytsearch:") || target.startsWith("scsearch"))
					if (autoplay)
						queueTracks(tracks);
					else
						manager.queue(tracks.get(0));
				else
					queueTracks(tracks);
			}
			return (tracks.size() > 0);
		} else
			throw new NullPointerException("Target channel must not be null");
	}

	/**
	 * Loads tracks from a search term or url and adds them to the manager queue.
	 * Searches only retrieve one track while playlist links load multiple. Make
	 * sure nothing is playing to avoid buggy behavior!
	 * 
	 * @param target The search term or url to retrieve tracks from
	 * @return True if the load was successful
	 */
	public boolean addToPlaylist(String target) {
		if (manager.getChannel() != null) {
			if (!(target.startsWith("http://") || target.startsWith("https://")))
				target = this.target.toString() + target;
			List<AudioTrack> tracks = searchForTracks(target);
			if (tracks.size() > 0) {
				if (target.startsWith("ytsearch:") || target.startsWith("scsearch"))
					manager.queue(tracks.get(0));
				else
					queueTracks(tracks);
				manager.updateStatus();
			}
			return (tracks.size() > 0);
		} else
			throw new NullPointerException("Target channel must not be null");
	}

	/**
	 * Adds the list of tracks to the track manager's queue.
	 * 
	 * @param tracks The tracks to add.
	 */
	public void queueTracks(List<AudioTrack> tracks) {
		if (manager.getChannel() != null)
			tracks.forEach(t -> manager.queue(t));
		else
			throw new NullPointerException("Target channel must not be null");
	}

	private static List<AudioTrack> searchForTracks(String identifier) {
		try {
			kong.unirest.json.JSONArray trackData = Unirest
					.get("http://" + NoodleBotMain.lavanode + "/loadtracks?identifier="
							+ URLEncoder.encode(identifier, "UTF-8"))
					.header("Authorization", NoodleBotMain.nodepass).asJson().getBody().getArray();

			ArrayList<AudioTrack> list = new ArrayList<>();
			trackData = trackData.getJSONObject(0).getJSONArray("tracks");
			trackData.forEach(o -> {
				try {
					list.add(LavalinkUtil.toAudioTrack(((kong.unirest.json.JSONObject) o).getString("track")));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});

			return list;
		} catch (UnirestException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
