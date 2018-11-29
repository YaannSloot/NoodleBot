package main.IanSloat.thiccbot;

import sx.blah.discord.api.events.EventSubscriber;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.util.FileSystemUtils;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.libraries.LibraryFactory;
import com.arsenarsen.lavaplayerbridge.libraries.UnknownBindingException;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.IanSloat.thiccbot.THICCBotMain;
import main.IanSloat.thiccbot.threadbox.AutoLeaveCounter;
import main.IanSloat.thiccbot.tools.WolframController;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.impl.events.shard.LoginEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Events {

	private AudioPlayerManager playerManager;
	private PlayerManager manager;
	private static ArrayList<AutoLeaveCounter> counters = new ArrayList<AutoLeaveCounter>();
	private static List<String> knownGuildIds = new ArrayList<String>();
	private static final Logger logger = LoggerFactory.getLogger(Events.class);

	@EventSubscriber
	public void MessageReceivedEvent(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)) {
			logger.info("Message recieved from: " + event.getAuthor().getName() + " server="
					+ event.getGuild().getName() + " Content=\"" + event.getMessage() + "\"");
			if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping"))
				event.getChannel().sendMessage("Pong!");

			else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "help")) {

				String help = "help - Lists available commands\n" + "die - No u\n"
						+ "play <video> - Plays a youtube video. You can enter the video name or the video URL\n"
						+ "volume <0-150> - Changes the volume of the video thats playing. Volume ranges from 0-150\n"
						+ "stop - Stops the current playing video\n" + "leave - Leaves the voice chat\n"
						+ "what <question> - Asks ThiccBot a question\n" + "info - Prints info about the bot\n\n"
						+ "Reminder: the calling word \'thicc\' is not case sensitive\n"
						+ "This is to accommodate for mobile users";
				event.getChannel().sendMessage(help);
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
				event.getChannel().sendMessage("no u");
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
					&& BotUtils.checkForWords(event.getMessage().getContent(), THICCBotMain.questionIDs, false, true)) {
				logger.info(event.getMessage().getContent().substring(BotUtils.BOT_PREFIX.length()));
				WolframController waClient = new WolframController(THICCBotMain.waAppID);
				waClient.askQuestionAndSend(event.getMessage().getContent().substring(BotUtils.BOT_PREFIX.length()),
						event.getChannel());
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "info")) {
				EmbedBuilder response = new EmbedBuilder();
				if (!(THICCBotMain.locator.getIPAddress().equals("")))
					response.appendField(
							"Current server location", THICCBotMain.locator.getCity() + ", "
									+ THICCBotMain.locator.getRegion() + ", " + THICCBotMain.locator.getCountry(),
							false);
				response.appendField("Powered by", "Java", false);
				response.appendField("Bot Version", "thiccbot-v0.7alpha", false);
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
					String videoURL = event.getMessage().getContent()
							.substring((BotUtils.BOT_PREFIX + "play ").length());
					IVoiceChannel voiceChannel = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();
					if (voiceChannel != null) {
						voiceChannel.join();
						EmbedBuilder thinkingMsg = new EmbedBuilder();
						thinkingMsg.withTitle("Loading audio...");
						thinkingMsg.withColor(192, 255, 0);
						IMessage message = event.getChannel().sendMessage(thinkingMsg.build());
						if (!(videoURL.startsWith("http://") || videoURL.startsWith("https://"))) {
							videoURL = "ytsearch:" + videoURL;
						}
						final String URI = videoURL;
						playerManager.loadItem("" + videoURL, new AudioLoadResultHandler() {
							@Override
							public void trackLoaded(AudioTrack track) {
								logger.info("we have vid");
								manager.getPlayer(event.getGuild().getStringID()).stop();
								manager.getPlayer(event.getGuild().getStringID()).queue(track);
								manager.getPlayer(event.getGuild().getStringID()).play();
								EmbedBuilder response = new EmbedBuilder();
								if (track.getSourceManager().getSourceName().equals("youtube")) {
									logger.info("Youtube is result");
									response.appendField("Now playing: ",
											'[' + track.getInfo().title + "](" + track.getInfo().uri + ')', true);
									response.appendField("Uploaded by:", track.getInfo().author, true);
									String duration = DurationFormatUtils.formatDuration(track.getInfo().length,
											"**H:mm:ss**", true);
									response.appendField("Duration: ", duration, false);
									response.withAuthorName("YouTube");
									response.withAuthorIcon("http://thiccbot.site/boticons/youtubeicon.png");
									response.withColor(238, 36, 21);
								}
								RequestBuffer.request(() -> message.edit(response.build()));
							}

							@Override
							public void playlistLoaded(AudioPlaylist playlist) {
								logger.info("we have playlist");
								manager.getPlayer(event.getGuild().getStringID()).stop();
								if (!URI.startsWith("ytsearch:")) {
									for (AudioTrack track : playlist.getTracks()) {
										manager.getPlayer(event.getGuild().getStringID()).queue(track);
									}
								} else {
									logger.info("Was a search so only one track was loaded");
									manager.getPlayer(event.getGuild().getStringID())
											.queue(playlist.getTracks().get(0));
								}
								manager.getPlayer(event.getGuild().getStringID()).play();
								EmbedBuilder response = new EmbedBuilder();
								AudioTrack track = manager.getPlayer(event.getGuild().getStringID()).getPlayingTrack()
										.getTrack();
								if (track.getSourceManager().getSourceName().equals("youtube")) {
									logger.info("Youtube is result");
									response.appendField("Now playing: ",
											'[' + track.getInfo().title + "](" + track.getInfo().uri + ')', true);
									response.appendField("Uploaded by:", track.getInfo().author, true);
									String duration = DurationFormatUtils.formatDuration(track.getInfo().length,
											"**H:mm:ss**", true);
									response.appendField("Duration: ", duration, false);
									response.withAuthorName("YouTube");
									response.withAuthorIcon("http://thiccbot.site/boticons/youtubeicon.png");
									response.withColor(238, 36, 21);
								}
								RequestBuffer.request(() -> message.edit(response.build()));
							}

							@Override
							public void noMatches() {
								// Notify the user that we've got nothing
								logger.info("nothin");
							}

							@Override
							public void loadFailed(FriendlyException throwable) {
								// Notify the user that everything exploded
								logger.info("explosion");
							}
						});
					} else {
						event.getChannel().sendMessage("Get in a voice channel first");
					}
				} catch (java.lang.StringIndexOutOfBoundsException e) {
					event.getChannel().sendMessage("Play what?");
				}
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "leave")) {
				IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
				if (voiceChannel != null) {
					manager.getPlayer(event.getGuild().getStringID()).stop();
					event.getChannel().sendMessage("Leaving voice channel");
					voiceChannel.leave();
				} else {
					event.getChannel().sendMessage("Not currently connected to any voice channels");
				}
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "stop")) {
				IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
				if (voiceChannel != null) {
					manager.getPlayer(event.getGuild().getStringID()).stop();
					event.getChannel().sendMessage("Stopped the current track");
				} else {
					event.getChannel().sendMessage("Not currently connected to any voice channels");
				}
			} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "volume ")) {
				IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
				if (voiceChannel != null) {
					String volume = event.getMessage().getContent()
							.substring((BotUtils.BOT_PREFIX + "volume ").length());
					try {
						manager.getPlayer(event.getGuild().getStringID()).setVolume(Integer.parseInt(volume));
						event.getChannel().sendMessage("Set volume to " + Integer.parseInt(volume));
					} catch (java.lang.NumberFormatException e) {
						event.getChannel().sendMessage("Setting volume to... wait WHAT?!");
					}
				} else {
					event.getChannel().sendMessage("Not currently connected to any voice channels");
				}
			}
		}
	}

	@EventSubscriber
	public void BotLoginEvent(LoginEvent event) {
		logger.info("Logged in.");
		event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, BotUtils.BOT_PREFIX + "help");
		try {
			playerManager = new DefaultAudioPlayerManager();
			playerManager.registerSourceManager(new YoutubeAudioSourceManager());
			playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
			playerManager.registerSourceManager(new BandcampAudioSourceManager());
			playerManager.registerSourceManager(new VimeoAudioSourceManager());
			playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
			playerManager.registerSourceManager(new BeamAudioSourceManager());
			playerManager.registerSourceManager(new HttpAudioSourceManager());
			playerManager.registerSourceManager(new LocalAudioSourceManager());
			manager = PlayerManager.getPlayerManager(LibraryFactory.getLibrary(event.getClient()));
		} catch (UnknownBindingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		class loadSettings extends Thread {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (IGuild guild : event.getClient().getGuilds()) {
					File SettingDirectory = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR
							+ "guildSettings" + BotUtils.PATH_SEPARATOR + guild.getStringID());
					if (!(SettingDirectory.exists())) {
						SettingDirectory.mkdirs();
						logger.info("Settings directory added for guild:" + guild.getName() + "(id:"
								+ guild.getStringID() + ") at path " + SettingDirectory.getAbsolutePath());
					}
				}
				logger.info("Settings files loaded successfully. Settings files located in "
						+ System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "guildSettings");
				for (IGuild guild : event.getClient().getGuilds()) {
					knownGuildIds.add(guild.getStringID());
				}
			}
		}
		logger.info("Loading guild settings...");
		new loadSettings().run();
	}

	@EventSubscriber
	public void GuildJoinEvent(GuildCreateEvent event) {
		try {
			if (!(knownGuildIds.contains(event.getGuild().getStringID()))) {
				event.getGuild().getChannels().get(0).sendMessage(
						"Hello! Thanks for adding me to your server.\nFor a list of commands, type \"thicc help\"");
				logger.info("Added to new guild. Guild: " + event.getGuild().getName() + "(id:"
						+ event.getGuild().getStringID() + ")");
				File SettingDirectory = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR
						+ "guildSettings" + BotUtils.PATH_SEPARATOR + event.getGuild().getStringID());
				if (SettingDirectory.exists()) {
					SettingDirectory.delete();
					SettingDirectory.mkdirs();
				} else {
					SettingDirectory.mkdirs();
				}
				knownGuildIds.add(event.getGuild().getStringID());
			}
		} catch (sx.blah.discord.util.DiscordException e) {

		}
	}

	@EventSubscriber
	public void GuildLeaveEvent(GuildLeaveEvent event) {
		File SettingDirectory = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "guildSettings"
				+ BotUtils.PATH_SEPARATOR + event.getGuild().getStringID());
		if (SettingDirectory.exists()) {
			FileSystemUtils.deleteRecursively(SettingDirectory);
		}
		knownGuildIds.remove(event.getGuild().getStringID());
		logger.info("Removed from guild " + event.getGuild().getStringID() + ". Removed settings files");
	}

	@EventSubscriber
	public void UserLeftVoiceEvent(UserVoiceChannelLeaveEvent event) {
		try {
			if (event.getGuild().getConnectedVoiceChannel().getStringID()
					.equals(event.getVoiceChannel().getStringID())) {
				logger.info("User: " + event.getUser().getName() + "(id:" + event.getUser().getStringID() + ')'
						+ " disconnected from connected voice channel on guild \"" + event.getGuild().getName()
						+ "\"(id:" + event.getGuild().getLongID() + "). Remaining users: "
						+ (event.getVoiceChannel().getConnectedUsers().size() - 1));
				if (event.getVoiceChannel().getConnectedUsers().size() == 1) {
					logger.info("No more users are currently connected. Auto-Leave countdown has been started.");
					AutoLeaveCounter counter = new AutoLeaveCounter(event.getGuild().getConnectedVoiceChannel());
					counter.start();
					counters.add(counter);
				}
			}
		} catch (NullPointerException e) {
		}
	}

	@EventSubscriber
	public void UserMovedOutOfVoiceEvent(UserVoiceChannelMoveEvent event) {
		try {
			if (event.getGuild().getConnectedVoiceChannel().getStringID().equals(event.getOldChannel().getStringID())) {
				logger.info("User: " + event.getUser().getName() + "(id:" + event.getUser().getStringID() + ')'
						+ " moved out of connected voice channel on guild \"" + event.getGuild().getName() + "\"(id:"
						+ event.getGuild().getLongID() + "). Remaining users: "
						+ (event.getOldChannel().getConnectedUsers().size() - 1));
				if (event.getOldChannel().getConnectedUsers().size() == 1) {
					logger.info("No more users are currently connected. Auto-Leave countdown has been started.");
					AutoLeaveCounter counter = new AutoLeaveCounter(event.getGuild().getConnectedVoiceChannel());
					counter.start();
					counters.add(counter);
				}
			}
		} catch (NullPointerException e) {
		}
	}
}
