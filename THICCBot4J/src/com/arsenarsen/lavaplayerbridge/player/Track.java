package com.arsenarsen.lavaplayerbridge.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wraps an {@link AudioTrack} to give it metadata and all that magic.
 */
public class Track implements Item {
    private AudioTrack track;
    private Map<String, Object> meta = new ConcurrentHashMap<>();

    private Track() {
    }

    /**
     * Creates a new {@link Track} with no preset meta.
     *
     * @param track {@link AudioTrack} to wrap
     */
    public Track(AudioTrack track) {
        if (track == null)
            throw new IllegalArgumentException("track cannot be null!");
        this.track = track;
    }

    /**
     * Creates a new {@link Track} and pre sets the metadata
     *
     * @param track {@link AudioTrack} to wrap
     * @param meta  Metadata to pre-set
     */
    public Track(AudioTrack track, Map<String, Object> meta) {
        this(track);
        if (meta != null)
            this.meta.putAll(meta);
    }

    /**
     * @return The wrapped track.
     */
    public AudioTrack getTrack() {
        return track;
    }

    /**
     * @return This Track's meta.
     */
    public Map<String, Object> getMeta() {
        return meta;
    }

    /**
     * Creates a playable clone of this track with the same meta
     *
     * @return A new {@link Track} object, identical to this one
     * @see AudioTrack#makeClone()
     */
    public Track makeClone() {
        return new Track(track.makeClone(), meta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Track track1 = (Track) o;

        if (!track.equals(track1.track)) return false;
        return meta.equals(track1.meta);
    }

    @Override
    public int hashCode() {
        int result = track.hashCode();
        result = 31 * result + meta.hashCode();
        return result;
    }
}
