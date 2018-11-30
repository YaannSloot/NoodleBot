package main.IanSloat.thiccbot.tools;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import sx.blah.discord.api.IDiscordClient;

public class MusicBox {

	private AudioPlayerManager playerManager;
	private PlayerManager manager;
	private IDiscordClient client;
	
	public MusicBox (IDiscordClient client) {
		this.client = client;
	}
	
}
