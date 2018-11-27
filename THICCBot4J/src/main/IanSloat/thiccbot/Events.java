package main.IanSloat.thiccbot;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.util.audio.AudioPlayer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.libraries.LibraryFactory;
import com.arsenarsen.lavaplayerbridge.libraries.UnknownBindingException;
import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.IanSloat.thicbot.tools.WolframController;
import main.IanSloat.thiccbot.THICCBotMain;
import main.IanSloat.thiccbot.threadbox.AutoLeaveCounter;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.impl.events.shard.LoginEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

public class Events {

	private AudioPlayerManager playerManager;

	private static ArrayList<AutoLeaveCounter> counters = new ArrayList<AutoLeaveCounter>();

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)) {

			System.out.println("Message recieved from: " + event.getAuthor().getName() + " server="
					+ event.getGuild().getName() + " Content=\"" + event.getMessage() + "\"");
			if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping"))
				BotUtils.sendMessage(event.getChannel(), "pong");

			else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "help")) {

				String help = "help - Lists available commands\n" + "die - No u\n"
						+ "play <video> - Plays a youtube video. You can enter the video name or the video URL\n"
						+ "volume <0-150> - Changes the volume of the video thats playing. Volume ranges from 0-150\n"
						+ "stop - Stops the current playing video\n" + "leave - Leaves the voice chat\n"
						+ "what <question> - Asks ThiccBot a question\n" + "info - Prints info about the bot\n\n"
						+ "Reminder: the calling word \'thicc\' is not case sensitive\n"
						+ "This is to accommodate for mobile users";
				BotUtils.sendMessage(event.getChannel(), help);
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
				BotUtils.sendMessage(event.getChannel(), "no u");
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
					&& BotUtils.checkForWords(event.getMessage().getContent(), THICCBotMain.questionIDs, false, true)) {
				System.out.println(event.getMessage().getContent().substring(BotUtils.BOT_PREFIX.length()));
				WolframController waClient = new WolframController(THICCBotMain.waAppID);
				waClient.askQuestionAndSend(event.getMessage().getContent().substring(BotUtils.BOT_PREFIX.length()), event.getChannel());
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "info")) {
				EmbedBuilder response = new EmbedBuilder();
				response.appendField("Current server location", "University of Illinois at Urbana-Champaign", false);
				response.appendField("Powered by", "Java", false);
				response.appendField("Bot Version", "v0.7alpha", false);
				response.appendField("Status", "Currently being fixed up. May need more duct tape", false);
				response.appendField("Current shard count", event.getClient().getShardCount() + " Shards active",
						false);
				response.appendField("Current amount of threads running on server",
						Thread.activeCount() + " Active threads", false);
				response.withTitle("Bot Info");
				response.withColor(0, 255, 0);
				RequestBuffer.request(() -> event.getChannel().sendMessage(response.build()));
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")) {
				try {
					String videoURL = event.getMessage().getContent().substring((BotUtils.BOT_PREFIX + "play ").length());
					IVoiceChannel voiceChannel = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();
					if (voiceChannel != null) {
						voiceChannel.join();
						playerManager = new DefaultAudioPlayerManager();
						playerManager.registerSourceManager(new YoutubeAudioSourceManager());
						playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
						playerManager.registerSourceManager(new BandcampAudioSourceManager());
						playerManager.registerSourceManager(new VimeoAudioSourceManager());
						playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
						playerManager.registerSourceManager(new BeamAudioSourceManager());
						playerManager.registerSourceManager(new HttpAudioSourceManager());
						playerManager.registerSourceManager(new LocalAudioSourceManager());
						PlayerManager manager = PlayerManager
								.getPlayerManager(LibraryFactory.getLibrary(event.getClient()));
						manager.getManager();
						if (!(videoURL.startsWith("http://") || videoURL.startsWith("https://"))) {
							videoURL = "ytsearch:" + videoURL;
						}
						Player player = manager.getPlayer(event.getGuild().getStringID());
						final String URI = videoURL;
						playerManager.loadItem("" + videoURL, new AudioLoadResultHandler() {
							@Override
							public void trackLoaded(AudioTrack track) {
								System.out.println("we have vid");
								player.stop();
								player.queue(track);
								player.play();
								EmbedBuilder response = new EmbedBuilder();
								if(track.getSourceManager().getSourceName().equals("youtube")) {
									System.out.println("Youtube is result");
									response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ')', true);
									response.appendField("Uploaded by:", track.getInfo().author, true);
									String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
									response.appendField("Duration: ", duration, false);
									response.withAuthorName("YouTube");
									response.withAuthorIcon("http://thiccbot.site/boticons/youtubeicon.png");
								}
								event.getChannel().sendMessage(response.build());
							}

							@Override
							public void playlistLoaded(AudioPlaylist playlist) {
								System.out.println("we have playlist");
								player.stop();
								if(!URI.startsWith("ytsearch:")) {
									for (AudioTrack track : playlist.getTracks()) {
										player.queue(track);
									}
								}
								else {
									System.out.println("Was a search so only one track was loaded");
									player.queue(playlist.getTracks().get(0));
								}
								player.play();
								EmbedBuilder response = new EmbedBuilder();
								AudioTrack track = player.getPlayingTrack().getTrack();
								if(track.getSourceManager().getSourceName().equals("youtube")) {
									System.out.println("Youtube is result");
									response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ')', true);
									response.appendField("Uploaded by:", track.getInfo().author, true);
									String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
									response.appendField("Duration: ", duration, false);
									response.withAuthorName("YouTube");
									response.withAuthorIcon("http://thiccbot.site/boticons/youtubeicon.png");
								}
								event.getChannel().sendMessage(response.build());
							}

							@Override
							public void noMatches() {
								// Notify the user that we've got nothing
								System.out.println("nothin");
							}

							@Override
							public void loadFailed(FriendlyException throwable) {
								// Notify the user that everything exploded
								System.out.println("explosion");
							}
						});
					} else {
						event.getChannel().sendMessage("Get in a voice channel first");
					}
				} catch (java.lang.StringIndexOutOfBoundsException e) {
					event.getChannel().sendMessage("Play what?");
				} catch (UnknownBindingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "leave")) {
				IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
				if (voiceChannel != null) {
					PlayerManager manager;
					try {
						manager = PlayerManager
								.getPlayerManager(LibraryFactory.getLibrary(event.getClient()));
						manager.getManager();
						Player player = manager.getPlayer(event.getGuild().getStringID());
						player.stop();
						event.getChannel().sendMessage("Leaving voice channel");
					} catch (UnknownBindingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					voiceChannel.leave();
				} else {
					event.getChannel().sendMessage("Not currently connected to any voice channels");
				}
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "stop")) {
				IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
				if (voiceChannel != null) {
					PlayerManager manager;
					try {
						manager = PlayerManager
								.getPlayerManager(LibraryFactory.getLibrary(event.getClient()));
						manager.getManager();
						Player player = manager.getPlayer(event.getGuild().getStringID());
						player.stop();
						event.getChannel().sendMessage("Stopped the current track");
					} catch (UnknownBindingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					event.getChannel().sendMessage("Not currently connected to any voice channels");
				}
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "volume ")) {
				IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
				if (voiceChannel != null) {
					String volume = event.getMessage().getContent().substring((BotUtils.BOT_PREFIX + "volume ").length());
					PlayerManager manager;
					try {
						manager = PlayerManager
								.getPlayerManager(LibraryFactory.getLibrary(event.getClient()));
						manager.getManager();
						Player player = manager.getPlayer(event.getGuild().getStringID());
						try {
						player.setVolume(Integer.parseInt(volume));
						event.getChannel().sendMessage("Set volume to " + Integer.parseInt(volume));
						} catch (java.lang.NumberFormatException e) {
							event.getChannel().sendMessage("Setting volume to... wait WHAT?!");
						}
					} catch (UnknownBindingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					event.getChannel().sendMessage("Not currently connected to any voice channels");
				}
			}
		}
	}

	@EventSubscriber
	public void onBotLogin(LoginEvent event) {
		System.out.println("Logged in.");
		event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, BotUtils.BOT_PREFIX + "help");
	}

	@EventSubscriber
	public void onUserLeavesVoice(UserVoiceChannelLeaveEvent event) {
		try {
			if (event.getGuild().getConnectedVoiceChannel().getStringID()
					.equals(event.getVoiceChannel().getStringID())) {
				System.out.println("User: " + event.getUser().getName() + "(id:" + event.getUser().getStringID() + ')'
						+ " disconnected from connected voice channel on guild \"" + event.getGuild().getName()
						+ "\"(id:" + event.getGuild().getLongID() + "). Remaining users: "
						+ (event.getVoiceChannel().getConnectedUsers().size() - 1));
				if (event.getVoiceChannel().getConnectedUsers().size() == 1) {
					System.out.println("No more users are currently connected. Auto-Leave countdown has been started.");
					AutoLeaveCounter counter = new AutoLeaveCounter(event.getGuild().getConnectedVoiceChannel());
					counter.start();
					counters.add(counter);
				}
			}
		} catch (NullPointerException e) {
		}
	}

	@EventSubscriber
	public void onUserMovesOutOfVoice(UserVoiceChannelMoveEvent event) {
		try {
			if (event.getGuild().getConnectedVoiceChannel().getStringID().equals(event.getOldChannel().getStringID())) {
				System.out.println("User: " + event.getUser().getName() + "(id:" + event.getUser().getStringID() + ')'
						+ " moved out of connected voice channel on guild \"" + event.getGuild().getName() + "\"(id:"
						+ event.getGuild().getLongID() + "). Remaining users: "
						+ (event.getOldChannel().getConnectedUsers().size() - 1));
				if (event.getOldChannel().getConnectedUsers().size() == 1) {
					System.out.println("No more users are currently connected. Auto-Leave countdown has been started.");
					AutoLeaveCounter counter = new AutoLeaveCounter(event.getGuild().getConnectedVoiceChannel());
					counter.start();
					counters.add(counter);
				}
			}
		} catch (NullPointerException e) {
		}
	}
}
