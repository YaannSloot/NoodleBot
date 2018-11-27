package com.arsenarsen.lavaplayerbridge.player;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.hooks.HookManager;
import com.arsenarsen.lavaplayerbridge.hooks.QueueHook;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wraps a {@link AudioPlayer} providing functions like queue and similar.
 */
public class Player extends AudioEventAdapter {
    private final AtomicBoolean CRASHED = new AtomicBoolean(false);
    private String guildId;
    private AudioPlayer player;
    private ConcurrentLinkedQueue<Track> playlist = new ConcurrentLinkedQueue<>();
    private AudioPlayerManager manager;
    private PlayerManager parent;
    private volatile boolean looping = false;
    private Track currentTrack;
    private HookManager<QueueHook> queueHooks = new HookManager<>();
    private Provider provider;

    private Player() {
    }

    @Override
    public void onEvent(AudioEvent event) {
        if (CRASHED.get())
            return;
        if (!parent.getLibrary().isValidGuild(guildId)) {
            parent.deletePlayer(this);
            return;
        }
        if (!(event instanceof TrackExceptionEvent))
            inject();
        super.onEvent(event);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext)
            skip();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        skip();
    }

    /**
     * Used internally, do not construct outside PlayerManager. I mean, you could, but it would not be internally saved.
     */
    public Player(PlayerManager parent, AudioPlayer player, AudioPlayerManager manager, String guildId) {
        this.parent = parent;
        this.manager = manager;
        player.addListener(this);
        this.player = player;
        this.guildId = guildId;
        inject();
    }

    void inject() {
        inject(false);
    }

    void inject(boolean requeue) {
        if (requeue && currentTrack != null && player.getPlayingTrack() == null) {
            AudioTrack track = currentTrack.getTrack();
            currentTrack = currentTrack.makeClone();
            currentTrack.getTrack().setPosition(track.getPosition());
            player.playTrack(currentTrack.getTrack());
        }
        parent.getLibrary().setProvider(guildId, provider != null ? provider : (provider = new Provider(this)));
    }

    /**
     * Adds a {@link AudioEventListener} to the player instance held by this player.
     *
     * @param listener The listener to attach.
     */
    public void addEventListener(AudioEventListener listener) {
        player.addListener(listener);
    }

    /**
     * Gets the {@link AudioPlayer} this {@link Player} wraps.
     *
     * @return The {@link AudioPlayer} this {@link Player} wraps.
     */
    public AudioPlayer getPlayer() {
        return player;
    }

    /**
     * Resolves a {@link AudioItem} by its identifier.
     *
     * @param identifier The identifier to use during resolution.
     * @return {@link AudioItem} resolved. Null if there was no match.
     * @throws InterruptedException If the thread loading was ran on was interrupted.
     * @throws ExecutionException   If computation threw an exception.
     * @throws FriendlyException    If load failed.
     */
    public AudioItem resolve(String identifier) throws ExecutionException, InterruptedException {
        AudioItem[] resolved = new AudioItem[1];
        FriendlyException[] thrown = new FriendlyException[1];
        manager.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                resolved[0] = track;
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                resolved[0] = playlist;
            }

            @Override
            public void noMatches() {/* Returns null */}

            @Override
            public void loadFailed(FriendlyException exception) {
                thrown[0] = exception;
            }
        }).get();
        if (thrown[0] != null)
            throw new FriendlyException(thrown[0].getMessage(), thrown[0].severity, thrown[0]);
        // Adds the two stacks together due to futures
        return resolved[0];
    }

    /**
     * Returns the current playlist.
     *
     * @return The current playlist.
     */
    public Queue<Track> getPlaylist() {
        return playlist;
    }

    /**
     * Sets the volume for the stream.
     *
     * @param volume The volume to set the stream to. 0-150 range.
     * @see AudioPlayer#setVolume(int)
     */
    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    /**
     * Gets the volume the stream is currently set to.
     *
     * @return The current volume of the stream.
     */
    public int getVolume() {
        return player.getVolume();
    }

    /**
     * Plays the next track in the playlist.
     */
    public void skip() {
        if (getLooping() && currentTrack != null) {
            playlist.add(currentTrack.makeClone());
        }
        AudioTrack track = null;
        Track next = playlist.poll();
        if (next != null)
            track = next.getTrack();
        currentTrack = next;
        if (!getPaused())
            player.playTrack(track);
    }

    /**
     * Sets this player in or out of loop mode
     *
     * @param looping True if you want this player to loop
     */
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    /**
     * Checks to see is this player looping
     *
     * @return True if this player is looping
     */
    public boolean getLooping() {
        return looping;
    }

    /**
     * Queues an {@link Track}
     *
     * @param track The track to queue
     */
    public void queue(Track track) {
        if (track == null)
            throw new IllegalArgumentException("track must not be null.");
        getQueueHookManager().forEach(queueHook -> queueHook.execute(this, track));
        addToQueue(track);
    }

    /**
     * Queues an {@link Playlist}
     *
     * @param playlist The playlist to queue
     */
    public void queue(Playlist playlist) {
        if (playlist == null)
            throw new IllegalArgumentException("playlist must not be null.");
        getQueueHookManager().forEach(queueHook -> queueHook.execute(this, playlist));
        playlist.forEach(this::addToQueue);
    }

    private void addToQueue(Track track) {
        if (playlist.isEmpty() && currentTrack == null) {
            playlist.add(track);
            skip();
        } else
            playlist.add(track);
    }

    /**
     * Queues a {@link AudioTrack} with no attached metadata.
     *
     * @param track The track to queue
     */
    public void queue(AudioTrack track) {
        if (track == null)
            throw new IllegalArgumentException("track must not be null.");
        queue(new Track(track));
    }

    /**
     * Shuffles the current playlist
     */
    public void shuffle() {
        List<Track> trackList = new ArrayList<>(playlist.size());
        player.stopTrack();
        trackList.addAll(playlist);
        if (currentTrack != null)
            trackList.add(currentTrack.makeClone());
        currentTrack = null;
        Collections.shuffle(trackList);
        playlist.clear();
        playlist.addAll(trackList);
        play();
    }

    /**
     * Sets the player to not paused and plays the next track.
     */
    public void play() {
        if (currentTrack == null)
            skip();
        setPaused(false);
    }

    /**
     * Sets the Player's paused status.
     *
     * @param paused The new paused status.
     */
    public void setPaused(boolean paused) {
        player.setPaused(paused);
    }

    /**
     * @return True if the player is currently paused.
     */
    public boolean getPaused() {
        return player.isPaused();
    }

    /**
     * Gets the parent {@link PlayerManager}.
     *
     * @return This player's parent.
     */
    public PlayerManager getParent() {
        return parent;
    }

    /**
     * @return Currently playing track
     */
    public Track getPlayingTrack() {
        return currentTrack;
    }

    /**
     * Clears the playlist and stops music.
     */
    public void stop() {
        playlist.clear();
        player.playTrack(null);
        currentTrack = null;
    }

    /**
     * Returns the Guild ID this player is attached to.
     *
     * @return A string.
     */
    public String getGuildId() {
        return guildId;
    }

    // Hooks.

    /**
     * @return The {@link QueueHook} {@link HookManager} used with this instance.
     */
    public HookManager<QueueHook> getQueueHookManager() {
        return queueHooks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player1 = (Player) o;

        if (looping != player1.looping) return false;
        if (!player.equals(player1.player)) return false;
        if (!playlist.equals(player1.playlist)) return false;
        if (!manager.equals(player1.manager)) return false;
        if (!parent.equals(player1.parent)) return false;
        return currentTrack != null ? currentTrack.equals(player1.currentTrack) : player1.currentTrack == null;
    }

    @Override
    public int hashCode() {
        int result = player.hashCode();
        result = 31 * result + playlist.hashCode();
        result = 31 * result + manager.hashCode();
        result = 31 * result + parent.hashCode();
        result = 31 * result + (looping ? 1 : 0);
        result = 31 * result + (currentTrack != null ? currentTrack.hashCode() : 0);
        return result;
    }

    /**
     * Do not call.
     */
    public void crash() {
        CRASHED.set(true);
        player.destroy();
        playlist.clear();
        queueHooks.forEach(queueHooks::unRegister);
        //noinspection all
        new String("I warned you.");
    }
}
