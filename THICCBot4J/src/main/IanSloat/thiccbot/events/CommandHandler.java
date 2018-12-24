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
import main.IanSloat.thiccbot.threadbox.BulkMessageDeletionJob;
import main.IanSloat.thiccbot.threadbox.FilterMessageDeletionJob;
import main.IanSloat.thiccbot.threadbox.MessageDeleteTools;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import main.IanSloat.thiccbot.tools.InspirobotClient;
import main.IanSloat.thiccbot.tools.MusicEmbedFactory;
import main.IanSloat.thiccbot.tools.WolframController;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.RequestBuffer;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
					|| listSettingsCommand(event) || setCommand(event) || showQueueCommand(event) || skipCommand(event)
					|| clearMessageHistoryCommand(event) || deleteMessagesByFilterCommand(event)
					|| inspireMeCommand(event) || getClientLoginCredentials(event));

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
			response.appendField("Bot Version", ThiccBotMain.botVersion, false);
			response.appendField("Status", ThiccBotMain.devMsg, false);
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
					if (!(videoURL.startsWith("http://") || videoURL.startsWith("https://")
							|| videoURL.startsWith("scsearch:"))) {
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
							logger.info("A track was loaded");
							musicManager.scheduler.stop();
							musicManager.scheduler.queue(track);
							message.delete();
						}

						@Override
						public void playlistLoaded(AudioPlaylist playlist) {
							logger.info("A track playlist was loaded");
							musicManager.scheduler.stop();
							GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
							if (!URI.startsWith("ytsearch:") || !URI.startsWith("scsearch:")
									|| setMgr.GetSetting("autoplay").equals("on")) {
								IMessage trackMessage = RequestBuffer.request(() -> {
									return event.getChannel().sendMessage("Loaded " + playlist.getTracks().size() + " tracks");
								}).get();
								MessageDeleteTools.DeleteAfterMillis(trackMessage, 5000);
								for (AudioTrack track : playlist.getTracks()) {
									musicManager.scheduler.queue(track);
								}
							} else {
								logger.info("Was a search and autoplay is off so only one track was loaded");
								musicManager.scheduler.queue(playlist.getTracks().get(0));
							}
							message.delete();
						}

						@Override
						public void noMatches() {
							// Notify the user that we've got nothing
							EmbedBuilder newMsg = new EmbedBuilder();
							newMsg.withTitle("No results found");
							newMsg.withColor(255, 0, 0);
							message.edit(newMsg.build());
							MessageDeleteTools.DeleteAfterMillis(message, 5000);
							logger.info("Audio track search returned no results");
						}

						@Override
						public void loadFailed(FriendlyException throwable) {
							// Notify the user that everything exploded
							EmbedBuilder newMsg = new EmbedBuilder();
							newMsg.withTitle("An error occurred while attempting to load the requested audio\n"
									+ "The URL may be invalid\n"
									+ "If the URL is a stream, the stream can only be played if it is live");
							newMsg.withColor(255, 0, 0);
							message.edit(newMsg.build());
							MessageDeleteTools.DeleteAfterMillis(message, 5000);
							logger.info("An error occurred while attempting to load an audio track");
						}
					});
				} else {
					event.getChannel().sendMessage("Get in a voice channel first");
				}
			} catch (java.lang.StringIndexOutOfBoundsException e) {
				IMessage errorMessage = RequestBuffer.request(() -> {
					return event.getChannel().sendMessage("Play what?");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(errorMessage, 5000);
			}
			event.getMessage().delete();
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
				IMessage commandMessage = RequestBuffer.request(() -> {
					return event.getChannel().sendMessage("Stopped the current track");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(commandMessage, 5000);
			} else {
				IMessage commandMessage = RequestBuffer.request(() -> {
					return event.getChannel().sendMessage("Not currently connected to any voice channels");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(commandMessage, 5000);
			}
			event.getMessage().delete();
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
				musicManager.scheduler.printPlaylist();
			} else {
				RequestBuffer.request(() -> event.getChannel().sendMessage("Queue is currently empty"));
			}
			event.getMessage().delete();
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
						musicManager.player.playTrack(null);
						musicManager.scheduler.nextTrack();
						IMessage skipMessage = RequestBuffer.request(() -> {
							return event.getChannel().sendMessage("Track skipped");
						}).get();
						MessageDeleteTools.DeleteAfterMillis(skipMessage, 5000);
					}
				} else if (tracks.size() > 0) {
					musicManager.player.playTrack(null);
					musicManager.scheduler.nextTrack();
					IMessage skipMessage = RequestBuffer.request(() -> {
						return event.getChannel().sendMessage("Track skipped");
					}).get();
					MessageDeleteTools.DeleteAfterMillis(skipMessage, 5000);
				} else {
					IMessage skipMessage = RequestBuffer.request(() -> {
						return event.getChannel().sendMessage("No tracks are playing or queued");
					}).get();
					MessageDeleteTools.DeleteAfterMillis(skipMessage, 5000);
				}
			} else {
				IMessage skipMessage = RequestBuffer.request(() -> {
					return event.getChannel().sendMessage("Not currently connected to any voice channels");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(skipMessage, 5000);
			}
			event.getMessage().delete();
			return true;
		} else {
			return false;
		}
	}

	private boolean clearMessageHistoryCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "clear message history")) {
			final Instant currentTime = Instant.now().minus(7, ChronoUnit.DAYS);
			BulkMessageDeletionJob job = BulkMessageDeletionJob.getDeletionJobForChannel(event.getChannel(),
					currentTime);
			job.startJob();
			return true;
		} else {
			return false;
		}
	}

	private boolean deleteMessagesByFilterCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase()
				.startsWith(BotUtils.BOT_PREFIX + "delete messages older than ")
				|| event.getMessage().getContent().toLowerCase()
						.startsWith(BotUtils.BOT_PREFIX + "delete messages from ")) {
			String ageString = event.getMessage().getContent().toLowerCase()
					.replace(BotUtils.BOT_PREFIX + "delete messages older than ", "");
			ageString = ageString.replace(',', ' ');
			ageString = BotUtils.normalizeSentence(ageString);
			List<String> words = new ArrayList<String>();
			for (String word : ageString.split(" ")) {
				if (word.equals("day")) {
					word = "days";
				} else if (word.equals("week")) {
					word = "weeks";
				} else if (word.equals("month")) {
					word = "months";
				} else if (word.equals("year")) {
					word = "years";
				}
				words.add(word);
			}
			List<IUser> usersMentioned = new ArrayList<IUser>();
			List<IRole> rolesMentioned = new ArrayList<IRole>();
			if (words.contains("from")) {
				usersMentioned = event.getMessage().getMentions();
				rolesMentioned = event.getMessage().getRoleMentions();
			}
			Calendar date = new GregorianCalendar();
			String[] ageWords = { "days", "weeks", "months", "years" };
			int days = 0;
			int weeks = 0;
			int months = 0;
			int years = 0;
			while (BotUtils.checkForWords(words, ageWords)) {
				if (words.contains("days") && words.indexOf("days") != 0) {
					try {
						int amount = Integer.parseInt(words.get(words.indexOf("days") - 1));
						words.remove(words.get(words.indexOf("days") - 1));
						words.remove("days");
						date.roll(Calendar.DAY_OF_YEAR, amount * -1);
						days += amount;
					} catch (NumberFormatException e) {
						words.remove("days");
					}
				} else if (words.contains("weeks") && words.indexOf("weeks") != 0) {
					try {
						int amount = Integer.parseInt(words.get(words.indexOf("weeks") - 1));
						words.remove(words.get(words.indexOf("weeks") - 1));
						words.remove("weeks");
						date.roll(Calendar.WEEK_OF_YEAR, amount * -1);
						weeks += amount;
					} catch (NumberFormatException e) {
						words.remove("weeks");
					}
				} else if (words.contains("months") && words.indexOf("months") != 0) {
					try {
						int amount = Integer.parseInt(words.get(words.indexOf("months") - 1));
						words.remove(words.get(words.indexOf("months") - 1));
						words.remove("months");
						date.roll(Calendar.MONTH, amount * -1);
						months += amount;
					} catch (NumberFormatException e) {
						words.remove("months");
					}
				} else if (words.contains("years") && words.indexOf("years") != 0) {
					try {
						int amount = Integer.parseInt(words.get(words.indexOf("years") - 1));
						words.remove(words.get(words.indexOf("years") - 1));
						words.remove("years");
						date.roll(Calendar.YEAR, amount * -1);
						years += amount;
					} catch (NumberFormatException e) {
						words.remove("years");
					}
				} else if (words.indexOf("days") == 0 || words.indexOf("weeks") == 0 || words.indexOf("months") == 0
						|| words.indexOf("years") == 0) {
					words.remove(0);
				}
				// System.out.println(String.join(" ", words));
			}
			if (days > 0 || weeks > 0 || months > 0 || years > 0 || usersMentioned.size() > 0 || rolesMentioned.size() > 0) {
				FilterMessageDeletionJob job = FilterMessageDeletionJob.getDeletionJobForChannel(event.getChannel());
				job.setAge(date.toInstant());
				job.deleteByLength(0, true);
				if (usersMentioned.size() > 0 || rolesMentioned.size() > 0) {
					List<IUser> users = new ArrayList<IUser>();
					users.addAll(usersMentioned);
					for (IRole role : rolesMentioned) {
						for (IUser user : event.getGuild().getUsersByRole(role)) {
							if (!(users.contains(user))) {
								users.add(user);
							}
						}
					}
					job.deleteByUser(users, true);
				}
				SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
				final String dateString = dateFormatter.format(date.getTime());
				RequestBuffer
						.request(() -> event.getChannel().sendMessage("Finding messages older than " + dateString));
				job.startJob();
			} else {
				RequestBuffer
						.request(() -> event.getChannel().sendMessage("Not sure what kind of calendar you are using,\n"
								+ "but I cannot understand what you just said"));
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean inspireMeCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "inspire me")) {
			InspirobotClient iClient = new InspirobotClient();
			EmbedBuilder message = new EmbedBuilder();
			message.withImage(iClient.getNewImageUrl());
			message.withTitle("Here you go. Now start feeling inspired");
			message.withDesc("Brought to you by [InspiroBot\u2122](https://inspirobot.me/)");
			message.withFooterText("Image requested by " + event.getAuthor().getName() + " | "
					+ new SimpleDateFormat("MM/dd/yyyy").format(Date.from(event.getMessage().getTimestamp())));
			message.withColor(220, 20, 60);
			RequestBuffer.request(() -> event.getChannel().sendMessage(message.build()));
			return true;
		} else {
			return false;
		}
	}

	private boolean getClientLoginCredentials(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "get gui login")) {
			if(PermissionUtils.hasPermissions(event.getGuild(), event.getAuthor(), Permissions.ADMINISTRATOR)) {
				EmbedBuilder message = new EmbedBuilder();
				message.withTitle("Your server's login credentials");
				message.appendField("Guild ID:", event.getGuild().getStringID(), false);
				GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
				if(setMgr.GetSetting("guipasswd").equals("")) {
					String passwd = "";
					for(int i = 0; i < 32; i++) {
						passwd += (char)(int)(Math.random() * 93 + 34);
					}
					setMgr.SetSetting("guipasswd", passwd);
				}
				message.appendField("Special Password:", setMgr.GetSetting("guipasswd"), false);
				message.withColor(0, 255, 0);
				RequestBuffer.request(() -> event.getAuthor().getOrCreatePMChannel().sendMessage(message.build()));
				RequestBuffer.request(() -> event.getChannel().sendMessage("Sent you a private message with the login details"));
			} else {
				RequestBuffer.request(() -> event.getChannel().sendMessage("You must be an administrator of this server to use gui management"));
			}
			return true;
		} else {
			return false;
		}
	}
	
}
