package com.IanSloat.noodlebot.overrides;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

public class DefaultAudioPlayerManager extends com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager {

	public DefaultAudioPlayerManager() {
		super();
	}
	
	protected AudioPlayer constructPlayer() {
	    return new com.IanSloat.noodlebot.overrides.DefaultAudioPlayer(this);
	}
	
}
