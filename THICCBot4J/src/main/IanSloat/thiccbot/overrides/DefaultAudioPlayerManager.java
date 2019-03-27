package main.IanSloat.thiccbot.overrides;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

public class DefaultAudioPlayerManager extends com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager {

	public DefaultAudioPlayerManager() {
		super();
	}
	
	protected AudioPlayer constructPlayer() {
	    return new main.IanSloat.thiccbot.overrides.DefaultAudioPlayer(this);
	}
	
}
