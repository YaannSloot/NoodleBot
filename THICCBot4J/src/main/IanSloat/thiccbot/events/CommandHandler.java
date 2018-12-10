package main.IanSloat.thiccbot.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.ThiccBotMain;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import main.IanSloat.thiccbot.tools.MusicEmbedFactory;
import main.IanSloat.thiccbot.tools.WolframController;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.util.List;

public class CommandHandler {

	private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	private synchronized GuildMusicManager getGuildAudioPlayer(IGuild guild, IChannel channel) {
		long guildId = guild.getLongID();
		GuildMusicManager musicManager = Events.musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(Events.playerManager, channel);
			Events.musicManagers.put(guildId, musicManager);
		} else {
			musicManager.setPlayingMessageChannel(channel);
		}

		guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

		return musicManager;
	}

	public void MessageReceivedEvent(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)) {
			logger.info("Message recieved from: " + event.getAuthor().getName() + ", server="
					+ event.getGuild().getName() + ", Content=\"" + event.getMessage() + "\"");

			if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping"))
				event.getChannel().sendMessage("Pong!");

			if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
				event.getChannel().sendMessage("no u");
			}

			boolean commandMatch = (helpCommand(event) || questionCommand(event) || infoCommand(event)
					|| playCommand(event) || leaveCommand(event) || stopCommand(event) || volumeCommand(event)
					|| listSettingsCommand(event) || setCommand(event) || showQueueCommand(event)
					|| skipCommand(event));

			if (!commandMatch) {
				int random = (int) (Math.random() * 5 + 1);
				if (random == 1) {
					event.getChannel().sendMessage("What?");
				} else if (random == 2) {
					event.getChannel().sendMessage("What are you saying?");
				} else if (random == 3) {
					event.getChannel().sendMessage("What language is that?");
				} else if (random == 4) {
					event.getChannel().sendMessage("Are you ok?");
				} else {
					event.getChannel().sendMessage("What you're saying makes no sense.");
				}
				logger.info("Message did not match any commands");
			}
		}
	}

	private boolean helpCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "help")) {
			String help = "help - Lists available commands\n" + "die - No u\n"
					+ "play <video> - Plays a youtube video. You can enter the video name or the video URL\n"
					+ "volume <0-150> - Changes the volume of the video thats playing. Volume ranges from 0-150\n"
					+ "stop - Stops the current playing video\n" + "leave - Leaves the voice chat\n"
					+ "what, how, why, etc. <question> - Asks ThiccBot a question\n"
					+ "settings/list settings - Lists the current personalized settings for this server\n"
					+ "set <setting> <value> - Sets a new value for one of this servers settings\n"
					+ "info - Prints info about the bot\n\n"
					+ "Reminder: the calling word \'thicc\' is not case sensitive\n"
					+ "This is to accommodate for mobile users";
			event.getChannel().sendMessage(help);
			return true;
		} else {
			return false;
		}
	}

	private boolean questionCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
				&& BotUtils.checkForWords(event.getMessage().getContent(), ThiccBotMain.questionIDs, false, true)) {
			logger.info(event.getMessage().getContent().substring(BotUtils.BOT_PREFIX.length()));
			WolframController waClient = new WolframController(ThiccBotMain.waAppID);
			waClient.askQuestionAndSend(event.getMessage().getContent().substring(BotUtils.BOT_PREFIX.length()),
					event.getChannel());
			return true;
		} else {
			return false;
		}
	}

	private boolean infoCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "info")) {
			EmbedBuilder response = new EmbedBuilder();
			if (!(ThiccBotMain.locator.getIPAddress().equals("")))
				response.appendField("Current server location", ThiccBotMain.locator.getCity() + ", "
						+ ThiccBotMain.locator.getRegion() + ", " + ThiccBotMain.locator.getCountry(), false);
			response.appendField("Powered by", "Java", false);
			response.appendField("Bot Version", "thiccbot-v0.8alpha", false);
			response.appendField("Status", "Getting close to v1.0 alpha, just not quite there yet", false);
			response.appendField("Current shard count", event.getClient().getShardCount() + " Shards active", false);
			response.appendField("Current amount of threads running on server",
					Thread.activeCount() + " Active threads", false);
			response.withTitle("Bot Info");
			response.withColor(0, 255, 0);
			RequestBuffer.request(() -> event.getChannel().sendMessage(response.build()));
			return true;
		} else {
			return false;
		}
	}

	private boolean playCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")) {
			try {
				String videoURL = event.getMessage().getContent().substring((BotUtils.BOT_PREFIX + "play ").length());
				IVoiceChannel voiceChannel = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();
				if (voiceChannel != null) {
					voiceChannel.join();
					EmbedBuilder thinkingMsg = new EmbedBuilder();
					thinkingMsg.withTitle("Loading audio...");
					thinkingMsg.withColor(192, 255, 0);
					IMessage message = event.getChannel().sendMessage(thinkingMsg.build());
					GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
					if (!(videoURL.startsWith("http://") || videoURL.startsWith("https://"))) {
						videoURL = "ytsearch:" + videoURL;
					}
					GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
					if (setMgr.GetSetting("volume").equals(""))
						setMgr.SetSetting("volume", "100");
					musicManager.player.setVolume(Integer.parseInt(setMgr.GetSetting("volume")));
					final String URI = videoURL;
					Events.playerManager.loadItem("" + videoURL, new AudioLoadResultHandler() {
						@Override
						public void trackLoaded(AudioTrack track) {
							logger.info("we have vid");
							musicManager.scheduler.stop();
							musicManager.scheduler.queue(track);
							message.delete();
						}

						@Override
						public void playlistLoaded(AudioPlaylist playlist) {
							logger.info("we have playlist");
							musicManager.scheduler.stop();
							GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
							if (!URI.startsWith("ytsearch:") || setMgr.GetSetting("autoplay").equals("on")) {
								RequestBuffer.request(() -> event.getChannel()
										.sendMessage("Loaded " + playlist.getTracks().size() + " tracks"));
								for (AudioTrack track : playlist.getTracks()) {
									musicManager.scheduler.queue(track);
								}
							} else {
								logger.info("Was a search so only one track was loaded");
								musicManager.scheduler.queue(playlist.getTracks().get(0));
							}
							message.delete();
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
			return true;
		} else {
			return false;
		}
	}

	private boolean leaveCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "leave")) {
			IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
			if (voiceChannel != null) {
				GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
				musicManager.scheduler.stop();
				event.getChannel().sendMessage("Leaving voice channel");
				voiceChannel.leave();
			} else {
				event.getChannel().sendMessage("Not currently connected to any voice channels");
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean stopCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "stop")) {
			IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
			if (voiceChannel != null) {
				GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
				musicManager.scheduler.stop();
				event.getChannel().sendMessage("Stopped the current track");
			} else {
				event.getChannel().sendMessage("Not currently connected to any voice channels");
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean volumeCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "volume ")) {
			IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
			if (voiceChannel != null) {
				String volume = event.getMessage().getContent().substring((BotUtils.BOT_PREFIX + "volume ").length());
				try {
					GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
					musicManager.player.setVolume(Integer.parseInt(volume));
					event.getChannel().sendMessage("Set volume to " + Integer.parseInt(volume));
				} catch (java.lang.NumberFormatException e) {
					event.getChannel().sendMessage("Setting volume to... wait WHAT?!");
				}
			} else {
				event.getChannel().sendMessage("Not currently connected to any voice channels");
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean listSettingsCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "list settings")
				|| event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "settings")) {
			GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
			if (setMgr.GetSetting("volume").equals("")) {
				setMgr.SetSetting("volume", "100");
			}
			if (setMgr.GetSetting("autoplay").equals("")) {
				setMgr.SetSetting("autoplay", "off");
			}
			EmbedBuilder response = new EmbedBuilder();
			response.withColor(0, 200, 0);
			response.withTitle("Settings | " + event.getGuild().getName());
			response.appendField("Voice channel settings",
					"Default volume = " + setMgr.GetSetting("volume") + "\nAutoPlay = " + setMgr.GetSetting("autoplay"),
					false);
			RequestBuffer.request(() -> event.getChannel().sendMessage(response.build()));
			return true;
		} else {
			return false;
		}
	}

	private boolean setCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "set ")) {
			GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
			String command = BotUtils.normalizeSentence(
					event.getMessage().getContent().substring((BotUtils.BOT_PREFIX + "set").length()));
			String[] words = command.split(" ");
			if (command.toLowerCase().startsWith("default volume ") || words[0].equals("volume")
					|| command.toLowerCase().startsWith("default volume to ")
					|| command.toLowerCase().startsWith("volume to ")) {
				try {
					int value;
					if (command.toLowerCase().startsWith("default volume ")
							|| command.toLowerCase().startsWith("volume to ")) {
						value = Integer.parseInt(words[2]);
					} else if (command.toLowerCase().startsWith("default volume to ")) {
						value = Integer.parseInt(words[3]);
					} else {
						value = Integer.parseInt(words[1]);
					}
					setMgr.SetSetting("volume", Integer.toString(value));
					event.getChannel().sendMessage("Changed default volume to " + value);
				} catch (NumberFormatException e) {
					event.getChannel().sendMessage("The value provided is not valid for that setting");
				}
			} else if (command.toLowerCase().startsWith("autoplay ") && words.length >= 2) {
				if (words[1].toLowerCase().equals("on")) {
					setMgr.SetSetting("autoplay", "on");
					event.getChannel().sendMessage("Set AutoPlay to \'on\'");
				} else if (words[1].toLowerCase().equals("off")) {
					setMgr.SetSetting("autoplay", "off");
					event.getChannel().sendMessage("Set AutoPlay to \'off\'");
				} else {
					event.getChannel().sendMessage("The value provided is not valid for that setting");
				}
			} else {
				event.getChannel().sendMessage("That is not a valid setting");
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean showQueueCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "show queue")) {
			GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
			if (musicManager.scheduler.getPlaylist().size() > 0) {
				RequestBuffer.request(() -> event.getChannel().sendMessage(MusicEmbedFactory.generatePlaylistList(
						"Playlist | " + event.getGuild().getName(), musicManager.scheduler.getPlaylist())));
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean skipCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "skip")) {
			IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
			if (voiceChannel != null) {
				GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
				List<AudioTrack> tracks = musicManager.scheduler.getPlaylist();
				if (tracks.isEmpty()) {
					if (musicManager.player.getPlayingTrack() != null) {
						musicManager.scheduler.nextTrack();
						event.getChannel().sendMessage("Track skipped");
					}
				} else if (tracks.size() > 0) {
					musicManager.scheduler.nextTrack();
					event.getChannel().sendMessage("Track skipped");
				} else {
					event.getChannel().sendMessage("No tracks are playing or queued");
				}
			} else {
				event.getChannel().sendMessage("Not currently connected to any voice channels");
			}
			return true;
		} else {
			return false;
		}
	}

}
