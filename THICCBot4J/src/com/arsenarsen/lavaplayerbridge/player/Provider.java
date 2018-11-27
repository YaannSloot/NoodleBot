package com.arsenarsen.lavaplayerbridge.player;

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

/**
 * Provides audio to libraries.
 */
public class Provider {
    private Player player;
    private byte[] frame;

    Provider(){}
    Provider(Player player){
        this.player = player;
    }

    // I hate writing documentation..
    /**
     * Checks if data provided from the player at this time is not null.
     * @return True if the data from the player isn't null.
     */
    public boolean isReady(){
        player.inject(true);
        AudioFrame frame = player.getPlayer().provide();
        if(frame != null)
            this.frame = frame.getData();
        return frame != null;
    }

    /**
     * Returns a frame of data to stream to Discord.
     * @return A frame of data or an empty frame if {@link Provider#isReady()} returns null.
     */
    public byte[] provide(){
        return frame != null ? frame : new byte[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Provider provider = (Provider) o;

        return player.equals(provider.player);
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }
}
