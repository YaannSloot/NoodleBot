package main.IanSloat.thiccbot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import main.IanSloat.thiccbot.TrackScheduler;
import sx.blah.discord.handle.obj.IChannel;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {
  /**
   * Audio player for the guild.
   */
  public final AudioPlayer player;
  /**
   * Track scheduler for the player.
   */
  public final TrackScheduler scheduler;

  /**
   * Creates a player and a track scheduler.
   * @param manager Audio player manager to use for creating the player.
   */
  public GuildMusicManager(AudioPlayerManager manager, IChannel channel) {
    player = manager.createPlayer();
    scheduler = new TrackScheduler(channel, player);
    player.addListener(scheduler);
  }

  /**
   * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
   */
  public AudioProvider getAudioProvider() {
    return new AudioProvider(player);
  }
  
  public void setPlayingMessageChannel(IChannel channel) {
	  scheduler.setChannel(channel);
  }
  
}
