package com.arsenarsen.lavaplayerbridge.player;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Playlist implements Iterable<Track>, Item {
    private List<Track> playlist = new CopyOnWriteArrayList<>();
    private Map<String, Object> meta = new ConcurrentHashMap<>();

    private Playlist() {
    }

    /**
     * Creates a new {@link Playlist} with no preset meta.
     *
     * @param playlist A {@code List <}{@link Track}{@code >} to wrap.
     */
    public Playlist(List<Track> playlist) {
        if (playlist == null)
            throw new IllegalArgumentException("playlist cannot be null!");
        this.playlist.addAll(playlist);
    }

    /**
     * Creates a new {@link Playlist} and pre sets the metadata
     *
     * @param playlist An {@code List <}{@link Track}{@code >} to wrap.
     * @param meta  Metadata to pre-set
     */
    public Playlist(List<Track> playlist, Map<String, Object> meta) {
        this(playlist);
        if (meta != null)
            this.meta.putAll(meta);
    }

    // AudioPlaylist from here on. I would use a List<AudioTrack> but type erasure.

    /**
     * Creates a new {@link Playlist} with no preset meta.
     *
     * @param playlist An {@link AudioPlaylist} to wrap.
     */
    public Playlist(AudioPlaylist playlist) {
        if (playlist == null)
            throw new IllegalArgumentException("playlist cannot be null!");
        this.playlist.addAll(playlist.getTracks().stream().map(Track::new).collect(Collectors.toList()));
    }

    /**
     * Creates a new {@link Playlist} and pre sets the metadata
     *
     * @param playlist An {@link AudioPlaylist} to wrap.
     * @param meta  Metadata to pre-set
     */
    public Playlist(AudioPlaylist playlist, Map<String, Object> meta) {
        this(playlist);
        if (meta != null)
            this.meta.putAll(meta);
    }

    /**
     * @return An {@code UnmodifiableList <}{@link Track}{@code >} of the current playlist.
     */
    public List<Track> getPlaylist(){
        return Collections.unmodifiableList(playlist);
    }

    /**
     * @param playlist The new playlist to set internally.
     */
    public void setPlaylist(List<Track> playlist){
        this.playlist.clear();
        this.playlist.addAll(playlist);
    }

    /**
     * @return This Playlist's meta.
     */
    public Map<String, Object> getMeta(){
        return meta;
    }

    @Override
    public Iterator<Track> iterator() {
        return playlist.iterator();
    }
}
