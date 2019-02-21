package main.IanSloat.thiccbot.events;

import org.apache.commons.lang3.StringUtils;
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
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;
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

import java.awt.Color;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler {

	private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	private final static Map<IGuild, PermissionsManager> permManagers = new HashMap<>();

	public static synchronized PermissionsManager getPermissionsManager(IGuild guild) {
		PermissionsManager permMgr = permManagers.get(guild);

		if (permMgr == null) {
			permMgr = new PermissionsManager(guild);
			permManagers.put(guild, permMgr);
		}

		return permMgr;
	}

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
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
				&& event.getChannel().isPrivate() == false) {
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
					|| inspireMeCommand(event) || getClientLoginCredentials(event) || setNewGuiPassword(event)
					|| setPermission(event) || setPermDefaults(event) || showCommandIds(event) || pauseCommand(event)
					|| removeFromQueueCommand(event));

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
		} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
				&& event.getChannel().isPrivate()) {
			logger.info("Private message recieved from: " + event.getAuthor().getName() + ", Content=\""
					+ event.getMessage() + "\"");
			if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping"))
				event.getChannel().sendMessage("Pong!");

			if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
				event.getChannel().sendMessage("no u");
			}
		}
	}

	private boolean helpCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "help")) {
			EmbedBuilder message = new EmbedBuilder();
			message.withTitle(
					"Available commands | " + event.getAuthor().getName() + " | " + event.getGuild().getName());
			message.appendField("**General Commands**", "**thicc help** - Lists available commands", false);
			message.withColor(Color.RED);
			PermissionsManager permMgr = getPermissionsManager(event.getGuild());
			permMgr.setQuietMode(true);
			if (permMgr.authUsage(permMgr.PLAY, event.getChannel(), event.getAuthor())
					|| permMgr.authUsage(permMgr.VOLUME, event.getChannel(), event.getAuthor())
					|| permMgr.authUsage(permMgr.SKIP, event.getChannel(), event.getAuthor())
					|| permMgr.authUsage(permMgr.STOP, event.getChannel(), event.getAuthor())
					|| permMgr.authUsage(permMgr.SHOW_QUEUE, event.getChannel(), event.getAuthor())
					|| permMgr.authUsage(permMgr.LEAVE, event.getChannel(), event.getAuthor())) {
				String hlpMsg = "";
				if (permMgr.authUsage(permMgr.PLAY, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc play <[scsearch:]Video name|Video URL>** - Plays a video or song\n";
				}
				if (permMgr.authUsage(permMgr.VOLUME, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc volume <0-150>** - Changes the player volume\n";
				}
				if (permMgr.authUsage(permMgr.SKIP, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc skip** - Skips the currently playing song\n";
				}
				if (permMgr.authUsage(permMgr.STOP, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc stop** - Stops the currently playing song and clears the queue\n";
				}
				if (permMgr.authUsage(permMgr.SHOW_QUEUE, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc show queue** - Lists the songs currently in the song queue\n";
				}
				if (permMgr.authUsage(permMgr.LEAVE, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc leave** - Makes the bot leave the chat\n";
				}
				message.appendField("**Player commands**", hlpMsg, false);
			}
			if (permMgr.authUsage(permMgr.CLEAR_COMMAND, event.getChannel(), event.getAuthor())
					|| permMgr.authUsage(permMgr.BY_FILTER, event.getChannel(), event.getAuthor())
					|| permMgr.authUsage(permMgr.SET_COMMAND, event.getChannel(), event.getAuthor())
					|| permMgr.authUsage(permMgr.LIST_SETTINGS, event.getChannel(), event.getAuthor())
					|| permMgr.authUsage(permMgr.GET_LOGIN, event.getChannel(), event.getAuthor())
					|| permMgr.authUsage(permMgr.PERMMGR, event.getChannel(), event.getAuthor())) {
				String hlpMsg = "";
				if (permMgr.authUsage(permMgr.CLEAR_COMMAND, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc clear message history** - Deletes all messages older than 1 week\n";
				}
				if (permMgr.authUsage(permMgr.BY_FILTER, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc delete messages**\n" + "Parameters:\n"
							+ "older than <number> <day(s)/week(s)/month(s)/year(S)>\n" + "from <@user|@role>\n"
							+ "Ex 1 - thicc delete messages older than 1 week 3 days from @everyone\n"
							+ "Ex 2 - thicc delete messages older than 1 month\n"
							+ "Ex 3 - thicc delete messages from @someuser\n";
				}
				if (permMgr.authUsage(permMgr.SET_COMMAND, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc set <setting> <value>** - Changes a server setting on the guild's settings file located on the bot server\n";
				}
				if (permMgr.authUsage(permMgr.LIST_SETTINGS, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc list settings or thicc settings** - Lists all of the settings and their values\n";
				}
				if (permMgr.authUsage(permMgr.GET_LOGIN, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc get gui login** - Creates a guild password for the bot's gui manager\n";
					hlpMsg += "**thicc get new gui login** - Creates a new guild password for the bot's gui manager\n";
				}
				if (permMgr.authUsage(permMgr.PERMMGR, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc permission <command id/command group> <allow/deny> <@user(s) and/or @role(s)>** - sets a permission for a command/command catagory\n";
				}
				message.appendField("**Server management commands**", hlpMsg, false);
			}
			if (permMgr.authUsage(permMgr.INFO, event.getChannel(), event.getAuthor())
					|| permMgr.authUsage(permMgr.QUESTION, event.getChannel(), event.getAuthor())) {
				String hlpMsg = "";
				if (permMgr.authUsage(permMgr.INFO, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc info** - Gets general info about the bot and it's current version number\n";
				}
				if (permMgr.authUsage(permMgr.QUESTION, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc <question>** - Sends a question to WolframAlpha\n";
				}
				message.appendField("**Utility commands**", hlpMsg, false);
			}
			if (permMgr.authUsage(permMgr.INSPIRE_ME, event.getChannel(), event.getAuthor())) {
				String hlpMsg = "";
				if (permMgr.authUsage(permMgr.INSPIRE_ME, event.getChannel(), event.getAuthor())) {
					hlpMsg += "**thicc inspire me** - Shows an inspirational image from InspiroBot\u2122\n";
				}
				message.appendField("**Other commands**", hlpMsg, false);
			}
			RequestBuffer.request(() -> event.getAuthor().getOrCreatePMChannel().sendMessage(message.build()));
			permMgr.setQuietMode(false);
			RequestBuffer.request(() -> event.getMessage().delete());

			return true;
		} else {
			return false;
		}
	}

	private boolean questionCommand(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
				&& BotUtils.checkForWords(event.getMessage().getContent(), ThiccBotMain.questionIDs, false, true)) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).QUESTION,
					event.getChannel(), event.getAuthor())) {
				logger.info(event.getMessage().getContent().substring(BotUtils.BOT_PREFIX.length()));
				WolframController waClient = new WolframController(ThiccBotMain.waAppID);
				waClient.askQuestionAndSend(event.getMessage().getContent().substring(BotUtils.BOT_PREFIX.length()),
						event.getChannel());
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean infoCommand(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "info")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).INFO,
					event.getChannel(), event.getAuthor())) {
				EmbedBuilder response = new EmbedBuilder();
				if (!(ThiccBotMain.locator.getIPAddress().equals("")))
					response.appendField(
							"Current server location", ThiccBotMain.locator.getCity() + ", "
									+ ThiccBotMain.locator.getRegion() + ", " + ThiccBotMain.locator.getCountry(),
							false);
				response.appendField("Powered by", "Java", false);
				response.appendField("Bot Version", ThiccBotMain.botVersion, false);
				response.appendField("Status", ThiccBotMain.devMsg, false);
				response.appendField("Current shard count", event.getClient().getShardCount() + " Shards active",
						false);
				response.appendField("Current amount of threads running on server",
						Thread.activeCount() + " Active threads", false);
				response.withTitle("Bot Info");
				response.withColor(0, 255, 0);
				RequestBuffer.request(() -> event.getChannel().sendMessage(response.build()));
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean playCommand(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")
				|| event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "add")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).PLAY,
					event.getChannel(), event.getAuthor())) {
				try {
					String videoURL = "";
					if (event.getMessage().getAttachments().size() == 0) {
						videoURL = event.getMessage().getContent().substring((BotUtils.BOT_PREFIX + "play ").length());
					} else {
						videoURL = event.getMessage().getAttachments().get(0).getUrl();
					}
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
						TBMLSettingsParser setParser = setMgr.getTBMLParser();
						setParser.setScope(TBMLSettingsParser.DOCROOT);
						setParser.addObj("PlayerSettings");
						setParser.setScope("PlayerSettings");
						if (setParser.getFirstInValGroup("volume").equals(""))
							setParser.addVal("volume", "100");
						musicManager.scheduler.setVolume(Integer.parseInt(setParser.getFirstInValGroup("volume")));
						final String URI = videoURL;
						Events.playerManager.loadItem("" + videoURL, new AudioLoadResultHandler() {
							@Override
							public void trackLoaded(AudioTrack track) {
								logger.info("A track was loaded");
								if (event.getMessage().getContent().toLowerCase()
										.startsWith(BotUtils.BOT_PREFIX + "play")) {
									musicManager.scheduler.stop();
								} else {
									IMessage result = RequestBuffer.request(() -> {
										return event.getChannel()
												.sendMessage("Added " + track.getInfo().title + " to queue");
									}).get();
									MessageDeleteTools.DeleteAfterMillis(result, 5000);
									musicManager.scheduler.updateStatus();
								}
								musicManager.scheduler.queue(track);
								message.delete();
							}

							@Override
							public void playlistLoaded(AudioPlaylist playlist) {
								logger.info("A track playlist was loaded");
								if (event.getMessage().getContent().toLowerCase()
										.startsWith(BotUtils.BOT_PREFIX + "play")) {
									musicManager.scheduler.stop();
								}
								if ((!URI.startsWith("ytsearch:") || !URI.startsWith("scsearch:")
										|| setParser.getFirstInValGroup("autoplay").equals("on"))
										&& event.getMessage().getContent().toLowerCase()
												.startsWith(BotUtils.BOT_PREFIX + "play")) {
									IMessage trackMessage = RequestBuffer.request(() -> {
										return event.getChannel()
												.sendMessage("Loaded " + playlist.getTracks().size() + " tracks");
									}).get();
									MessageDeleteTools.DeleteAfterMillis(trackMessage, 5000);
									for (AudioTrack track : playlist.getTracks()) {
										musicManager.scheduler.queue(track);
									}
								} else {
									logger.info("Was a search and autoplay is off so only one track was loaded");
									musicManager.scheduler.queue(playlist.getTracks().get(0));
									if (event.getMessage().getContent().toLowerCase()
											.startsWith(BotUtils.BOT_PREFIX + "add")) {
										IMessage result = RequestBuffer.request(() -> {
											return event.getChannel().sendMessage("Added "
													+ playlist.getTracks().get(0).getInfo().title + " to queue");
										}).get();
										MessageDeleteTools.DeleteAfterMillis(result, 5000);
										musicManager.scheduler.updateStatus();
									}
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
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean leaveCommand(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "leave")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).LEAVE,
					event.getChannel(), event.getAuthor())) {
				IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
				if (voiceChannel != null) {
					GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
					musicManager.scheduler.stop();
					event.getChannel().sendMessage("Leaving voice channel");
					voiceChannel.leave();
				} else {
					event.getChannel().sendMessage("Not currently connected to any voice channels");
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean stopCommand(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "stop")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).STOP,
					event.getChannel(), event.getAuthor())) {
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
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean volumeCommand(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "volume ")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).VOLUME,
					event.getChannel(), event.getAuthor())) {
				IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
				if (voiceChannel != null) {
					String volume = event.getMessage().getContent()
							.substring((BotUtils.BOT_PREFIX + "volume ").length());
					GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
					TBMLSettingsParser setParser = setMgr.getTBMLParser();
					try {
						GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
						if (setParser.getFirstInValGroup("volumecap").equals("off")) {
							musicManager.scheduler.setVolume(Integer.parseInt(volume));
						} else {
							musicManager.scheduler.setVolume(
									Math.min(200, Math.max(0, Integer.parseInt(volume))));
						}
						musicManager.scheduler.updateStatus();
						event.getChannel().sendMessage("Set volume to " + Integer.parseInt(volume));
					} catch (java.lang.NumberFormatException e) {
						event.getChannel().sendMessage("Setting volume to... wait WHAT?!");
					}
				} else {
					event.getChannel().sendMessage("Not currently connected to any voice channels");
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean listSettingsCommand(MessageReceivedEvent event) {

		if ((event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "list settings")
				|| event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "settings"))) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).LIST_SETTINGS,
					event.getChannel(), event.getAuthor())) {
				GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
				TBMLSettingsParser setParser = setMgr.getTBMLParser();
				setParser.setScope(TBMLSettingsParser.DOCROOT);
				setParser.addObj("PlayerSettings");
				setParser.setScope("PlayerSettings");
				if (setParser.getFirstInValGroup("volume").equals("")) {
					setParser.addVal("volume", "100");
				}
				if (setParser.getFirstInValGroup("autoplay").equals("")) {
					setParser.addVal("autoplay", "off");
				}
				if (setParser.getFirstInValGroup("volumecap").equals("")) {
					setParser.addVal("volumecap", "on");
				}
				EmbedBuilder response = new EmbedBuilder();
				response.withColor(0, 200, 0);
				response.withTitle("Settings | " + event.getGuild().getName());
				response.appendField("Voice channel settings",
						"Default volume = " + setParser.getFirstInValGroup("volume") + "\nAutoPlay = "
								+ setParser.getFirstInValGroup("autoplay") + "\nEnforce volume cap = "
								+ setParser.getFirstInValGroup("volumecap"),
						false);
				RequestBuffer.request(() -> event.getChannel().sendMessage(response.build()));
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean setCommand(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "set ")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).SET_COMMAND,
					event.getChannel(), event.getAuthor())) {
				GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
				TBMLSettingsParser setParser = setMgr.getTBMLParser();
				setParser.setScope(TBMLSettingsParser.DOCROOT);
				String command = BotUtils.normalizeSentence(event.getMessage().getContent().toLowerCase()
						.substring((BotUtils.BOT_PREFIX + "set").length()));
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
						setParser.setScope(TBMLSettingsParser.DOCROOT);
						setParser.addObj("PlayerSettings");
						setParser.setScope("PlayerSettings");
						if (setParser.getFirstInValGroup("volume").equals("")) {
							setParser.addVal("volume", "100");
						}
						setParser.setFirstInValGroup("volume", Integer.toString(value));
						event.getChannel().sendMessage("Changed default volume to " + value);
					} catch (NumberFormatException e) {
						event.getChannel().sendMessage("The value provided is not valid for that setting");
					}
				} else if (command.toLowerCase().startsWith("autoplay ") && words.length >= 2) {
					setParser.setScope(TBMLSettingsParser.DOCROOT);
					setParser.addObj("PlayerSettings");
					setParser.setScope("PlayerSettings");
					if (setParser.getFirstInValGroup("autoplay").equals("")) {
						setParser.addVal("autoplay", "off");
					}
					if (words[1].toLowerCase().equals("on")) {
						setParser.setFirstInValGroup("autoplay", "on");
						event.getChannel().sendMessage("Set AutoPlay to \'on\'");
					} else if (words[1].toLowerCase().equals("off")) {
						setParser.setFirstInValGroup("autoplay", "off");
						event.getChannel().sendMessage("Set AutoPlay to \'off\'");
					} else {
						event.getChannel().sendMessage("The value provided is not valid for that setting");
					}
				} else if (command.toLowerCase().startsWith("volumecap ") && words.length >= 2) {
					setParser.setScope(TBMLSettingsParser.DOCROOT);
					setParser.addObj("PlayerSettings");
					setParser.setScope("PlayerSettings");
					if (setParser.getFirstInValGroup("volumecap").equals("")) {
						setParser.addVal("volumecap", "on");
					}
					if (words[1].toLowerCase().equals("on")) {
						setParser.setFirstInValGroup("volumecap", "on");
						event.getChannel().sendMessage("Set volume limit to \'on\'");
					} else if (words[1].toLowerCase().equals("off")) {
						setParser.setFirstInValGroup("volumecap", "off");
						event.getChannel().sendMessage("Set volume limit to \'off\'");
					} else {
						event.getChannel().sendMessage("The value provided is not valid for that setting");
					}
				} else {
					event.getChannel().sendMessage("That is not a valid setting");
				}

			}
			return true;
		} else {
			return false;
		}
	}

	private boolean showQueueCommand(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "show queue")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).SHOW_QUEUE,
					event.getChannel(), event.getAuthor())) {
				GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
				if (musicManager.scheduler.getPlaylist().size() > 0) {
					musicManager.scheduler.setPlaylistDisplay(true);
					musicManager.scheduler.updateStatus();
				} else {
					RequestBuffer.request(() -> event.getChannel().sendMessage("Queue is currently empty"));
				}
				event.getMessage().delete();
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean pauseCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "pause")) {
			if (getPermissionsManager(event.getGuild()).authUsage("pause", event.getChannel(), event.getAuthor())) {
				IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
				if (voiceChannel != null) {
					GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
					if (musicManager.scheduler.isPaused() == false) {
						musicManager.scheduler.pauseTrack();
						RequestBuffer.request(() -> {
							event.getChannel().sendMessage("Paused the current track");
						});
					} else {
						musicManager.scheduler.unpauseTrack();
						RequestBuffer.request(() -> {
							event.getChannel().sendMessage("Unpaused the current track");
						});
					}
				} else {
					RequestBuffer.request(() -> {
						event.getChannel().sendMessage("No tracks are currently playing");
					});
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean removeFromQueueCommand(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "remove track")) {
			RequestBuffer.request(() -> {
				event.getMessage().delete();
			});
			if (getPermissionsManager(event.getGuild()).authUsage("queuemanage", event.getChannel(),
					event.getAuthor())) {
				IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
				if (voiceChannel != null) {
					String command = event.getMessage().getContent();
					command = command.replace(BotUtils.BOT_PREFIX + "remove track", "");
					command = BotUtils.normalizeSentence(command);
					List<String> wordList = Arrays.asList(command.split(" "));
					command = "";
					for (String word : wordList) {
						command += word;
					}
					wordList = Arrays.asList(command.split("-"));
					if (wordList.size() < 1 || wordList.size() > 2) {
						IMessage response = RequestBuffer.request(() -> {
							return event.getChannel()
									.sendMessage("Please only specify either a single track or one range of tracks");
						}).get();
						MessageDeleteTools.DeleteAfterMillis(response, 5000);
					} else {
						List<Integer> trackNumbers = new ArrayList<Integer>();
						boolean parseError = false;
						for (String number : wordList) {
							try {
								trackNumbers.add(Integer.parseInt(number));
							} catch (NumberFormatException e) {
								parseError = true;
							}
						}
						if (parseError == true) {
							IMessage response = RequestBuffer.request(() -> {
								return event.getChannel()
										.sendMessage("Please only reference tracks via whole numbers in base 10");
							}).get();
							MessageDeleteTools.DeleteAfterMillis(response, 5000);
						} else {
							GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
							;
							trackNumbers.sort(Comparator.naturalOrder());
							if (trackNumbers.get(0) < 1) {
								trackNumbers.set(0, 1);
							} else if (trackNumbers.get(0) > musicManager.scheduler.getPlaylist().size()) {
								trackNumbers.set(0, musicManager.scheduler.getPlaylist().size());
							}
							if (trackNumbers.size() == 1) {
								musicManager.scheduler.removeTrackFromQueue(trackNumbers.get(0));
								musicManager.scheduler.updateStatus();
							} else {
								if (trackNumbers.get(1) < 1) {
									trackNumbers.set(1, 1);
								} else if (trackNumbers.get(1) > musicManager.scheduler.getPlaylist().size()) {
									trackNumbers.set(1, musicManager.scheduler.getPlaylist().size());
								}
								int amount = trackNumbers.get(1) - trackNumbers.get(0) + 1;
								for (int i = 0; i < amount; i++) {
									musicManager.scheduler.removeTrackFromQueue(trackNumbers.get(0));
								}
								musicManager.scheduler.updateStatus();
							}
						}
					}
				} else {
					RequestBuffer.request(() -> {
						event.getChannel().sendMessage("No tracks are currently playing");
					});
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean skipCommand(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "skip")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).SKIP,
					event.getChannel(), event.getAuthor())) {
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
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean clearMessageHistoryCommand(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "clear message history")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).CLEAR_COMMAND,
					event.getChannel(), event.getAuthor())) {
				final Instant currentTime = Instant.now().minus(7, ChronoUnit.DAYS);
				BulkMessageDeletionJob job = BulkMessageDeletionJob.getDeletionJobForChannel(event.getChannel(),
						currentTime);
				job.startJob();
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean deleteMessagesByFilterCommand(MessageReceivedEvent event) {

		if ((event.getMessage().getContent().toLowerCase()
				.startsWith(BotUtils.BOT_PREFIX + "delete messages older than ")
				|| event.getMessage().getContent().toLowerCase()
						.startsWith(BotUtils.BOT_PREFIX + "delete messages from "))) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).BY_FILTER,
					event.getChannel(), event.getAuthor())) {
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
				if (days > 0 || weeks > 0 || months > 0 || years > 0 || usersMentioned.size() > 0
						|| rolesMentioned.size() > 0) {
					FilterMessageDeletionJob job = FilterMessageDeletionJob
							.getDeletionJobForChannel(event.getChannel());
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
					RequestBuffer.request(
							() -> event.getChannel().sendMessage("Not sure what kind of calendar you are using,\n"
									+ "but I cannot understand what you just said"));
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean inspireMeCommand(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "inspire me")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).INSPIRE_ME,
					event.getChannel(), event.getAuthor())) {
				InspirobotClient iClient = new InspirobotClient();
				EmbedBuilder message = new EmbedBuilder();
				message.withImage(iClient.getNewImageUrl());
				message.withTitle("Here you go. Now start feeling inspired");
				message.withDesc("Brought to you by [InspiroBot\u2122](https://inspirobot.me/)");
				message.withFooterText("Image requested by " + event.getAuthor().getName() + " | "
						+ new SimpleDateFormat("MM/dd/yyyy").format(Date.from(event.getMessage().getTimestamp())));
				message.withColor(220, 20, 60);
				RequestBuffer.request(() -> event.getChannel().sendMessage(message.build()));
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean getClientLoginCredentials(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "get gui login")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).GET_LOGIN,
					event.getChannel(), event.getAuthor())) {
				if (PermissionUtils.hasPermissions(event.getGuild(), event.getAuthor(), Permissions.ADMINISTRATOR)) {
					EmbedBuilder message = new EmbedBuilder();
					message.withTitle("Your server's login credentials");
					message.appendField("Guild ID:", event.getGuild().getStringID(), false);
					GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
					TBMLSettingsParser setParser = setMgr.getTBMLParser();
					setParser.setScope(TBMLSettingsParser.DOCROOT);
					setParser.addObj("GuiSettings");
					setParser.setScope("GuiSettings");
					if (setParser.getFirstInValGroup("guipasswd").equals("")) {
						String passwd = "";
						for (int i = 0; i < 32; i++) {
							passwd += (char) (int) (Math.random() * 93 + 34);
						}
						setParser.addVal("guipasswd", passwd);
					}
					message.appendField("Special Password:", setParser.getFirstInValGroup("guipasswd"), false);
					message.withColor(0, 255, 0);
					RequestBuffer.request(() -> event.getAuthor().getOrCreatePMChannel().sendMessage(message.build()));
					RequestBuffer.request(
							() -> event.getChannel().sendMessage("Sent you a private message with the login details"));
				} else {
					RequestBuffer.request(() -> event.getChannel()
							.sendMessage("You must be an administrator of this server to use gui management"));
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean setNewGuiPassword(MessageReceivedEvent event) {

		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "get new gui login")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).GET_LOGIN,
					event.getChannel(), event.getAuthor())) {
				if (PermissionUtils.hasPermissions(event.getGuild(), event.getAuthor(), Permissions.ADMINISTRATOR)) {
					EmbedBuilder message = new EmbedBuilder();
					message.appendField("Guild ID:", event.getGuild().getStringID(), false);
					GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
					TBMLSettingsParser setParser = setMgr.getTBMLParser();
					setParser.setScope(TBMLSettingsParser.DOCROOT);
					setParser.addObj("GuiSettings");
					setParser.setScope("GuiSettings");
					String passwd = "";
					for (int i = 0; i < 32; i++) {
						passwd += (char) (int) (Math.random() * 93 + 34);
					}
					if (setParser.getFirstInValGroup("guipasswd").equals("")) {
						setParser.addVal("guipasswd", passwd);
						message.withTitle("Your server's login credentials");
					} else {
						setParser.setFirstInValGroup("guipasswd", passwd);
						message.withTitle("Your server's new login credentials");
					}
					message.appendField("Special Password:", setParser.getFirstInValGroup("guipasswd"), false);
					message.withColor(0, 255, 0);
					RequestBuffer.request(() -> event.getAuthor().getOrCreatePMChannel().sendMessage(message.build()));
					RequestBuffer.request(
							() -> event.getChannel().sendMessage("Sent you a private message with the login details"));
				} else {
					RequestBuffer.request(() -> event.getChannel()
							.sendMessage("You must be an administrator of this server to use gui management"));
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean setPermission(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "permission")) {
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).PERMMGR,
					event.getChannel(), event.getAuthor())) {
				if (PermissionUtils.hasPermissions(event.getGuild(), event.getAuthor(), Permissions.ADMINISTRATOR)) {
					String command = BotUtils.normalizeSentence(event.getMessage().getContent().toLowerCase()
							.substring((BotUtils.BOT_PREFIX + "permission").length()));
					String[] words = command.split(" ");
					List<IUser> users = event.getMessage().getMentions();
					List<IRole> roles = event.getMessage().getRoleMentions();
					List<IRole> authorRoles = event.getAuthor().getRolesForGuild(event.getGuild());
					if (users.size() == 0 && roles.size() == 0) {
						RequestBuffer.request(() -> event.getMessage().delete());
						IMessage errorMsg = RequestBuffer.request(() -> {
							return event.getChannel()
									.sendMessage("You must specify at least one role or user to apply a permission to");
						}).get();
						MessageDeleteTools.DeleteAfterMillis(errorMsg, 5000);
					} else if (users.size() == 1 && users.get(0).equals(event.getAuthor()) && roles.size() == 0) {
						RequestBuffer.request(() -> event.getMessage().delete());
						IMessage errorMsg = RequestBuffer.request(() -> {
							return event.getChannel().sendMessage("You cannot apply a permission to yourself");
						}).get();
						MessageDeleteTools.DeleteAfterMillis(errorMsg, 5000);
					} else if (!(BotUtils.stringArrayContains(PermissionsManager.commandWords, words[0]))) {
						RequestBuffer.request(() -> event.getMessage().delete());
						IMessage errorMsg = RequestBuffer.request(() -> {
							return event.getChannel()
									.sendMessage("You must reference a valid command identifier or command group");
						}).get();
						MessageDeleteTools.DeleteAfterMillis(errorMsg, 5000);
					} else if (!(BotUtils.stringArrayContains(new String[] { "allow", "deny" }, words[1]))) {
						RequestBuffer.request(() -> event.getMessage().delete());
						IMessage errorMsg = RequestBuffer.request(() -> {
							return event.getChannel().sendMessage(
									"You must explicitly state whether to allow or deny usage of this command or command group");
						}).get();
						MessageDeleteTools.DeleteAfterMillis(errorMsg, 5000);
					} else {
						List<IUser> userCopy = users;
						List<IRole> roleCopy = roles;
						for (int i = 0; i < userCopy.size(); i++) {
							if (!(PermissionUtils.isUserHigher(event.getGuild(), event.getAuthor(), userCopy.get(i)))) {
								users.remove(userCopy.get(i));
							}
						}
						for (int i = 0; i < roleCopy.size(); i++) {
							List<IRole> roleQuestion = new ArrayList<IRole>();
							roleQuestion.add(roleCopy.get(i));
							if (!(PermissionUtils.isUserHigher(event.getGuild(), event.getAuthor(), roleQuestion))) {
								roles.remove(roleCopy.get(i));
							}
						}
						users = userCopy;
						roles = roleCopy;
						if (users.size() == 0 && roles.size() == 0) {
							IMessage errorMsg = RequestBuffer.request(() -> {
								return event.getChannel().sendMessage(
										"The users/roles you mentioned all have higher positions than you and you cannot set their permissions");
							}).get();
							MessageDeleteTools.DeleteAfterMillis(errorMsg, 5000);
						} else {
							PermissionsManager permMgr = getPermissionsManager(event.getGuild());
							RequestBuffer.request(() -> event.getMessage().delete());
							String permission = "";
							String authorException = "";
							if (BotUtils.stringArrayContains(new String[] { "player", "management", "utility", "misc" },
									words[0])) {
								if (words[1].equals("allow")) {
									permission = permMgr.ALLOW_GLOBAL;
								} else {
									permission = permMgr.DENY_GLOBAL;
									authorException = permMgr.ALLOW_GLOBAL;
								}
							} else {
								if (words[1].equals("allow")) {
									permission = permMgr.ALLOW;
								} else {
									permission = permMgr.DENY;
									authorException = permMgr.ALLOW;
								}
							}
							for (IUser user : users) {
								permMgr.SetPermission(words[0], user, permission);
							}
							for (IRole role : roles) {
								permMgr.SetPermission(words[0], role, permission);
							}
							if (BotUtils.checkForElement(roles, authorRoles) && words[1].equals("deny")) {
								permMgr.SetPermission(words[0], event.getAuthor(), authorException);
							}
							if (users.contains(event.getAuthor()) && words[1].equals("deny")) {
								permMgr.SetPermission(words[0], event.getAuthor(), authorException);
							}
							IMessage completeMsg = RequestBuffer.request(() -> {
								return event.getChannel().sendMessage("Permissions set successfully");
							}).get();
							MessageDeleteTools.DeleteAfterMillis(completeMsg, 5000);
						}
					}
				} else {
					RequestBuffer.request(() -> event.getChannel()
							.sendMessage("You must be an administrator of this server to manage permissions"));
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean setPermDefaults(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "apply default permissions")) {
			RequestBuffer.request(() -> event.getMessage().delete());
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).PERMMGR,
					event.getChannel(), event.getAuthor())) {
				if (event.getGuild().getOwner().equals(event.getAuthor())) {
					PermissionsManager permMgr = getPermissionsManager(event.getGuild());
					permMgr.clearPermissions();
					List<IUser> guildUsers = new ArrayList<IUser>();
					List<IRole> guildRoles = new ArrayList<IRole>();
					guildUsers.addAll(event.getGuild().getUsers());
					guildRoles.addAll(event.getGuild().getRoles());
					guildUsers.remove(event.getGuild().getOwner());
					for (IUser user : guildUsers) {
						if (PermissionUtils.hasPermissions(event.getGuild(), user,
								Permissions.ADMINISTRATOR) == false) {
							permMgr.SetPermission(permMgr.INFO, user, permMgr.DENY);
							permMgr.SetPermission(permMgr.MANAGE_GLOBAL, user, permMgr.DENY_GLOBAL);
						}
					}
					for (IRole role : guildRoles) {
						if (!(role.getPermissions().contains(Permissions.ADMINISTRATOR))) {
							permMgr.SetPermission(permMgr.INFO, role, permMgr.DENY);
							permMgr.SetPermission(permMgr.MANAGE_GLOBAL, role, permMgr.DENY_GLOBAL);
						}
					}
					IMessage completeMsg = RequestBuffer.request(() -> {
						return event.getChannel().sendMessage("Permissions set successfully");
					}).get();
					MessageDeleteTools.DeleteAfterMillis(completeMsg, 5000);
				} else {
					RequestBuffer.request(() -> event.getChannel()
							.sendMessage("You must be the owner of this server to set default permissions"));
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean showCommandIds(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "show permission ids")) {
			RequestBuffer.request(() -> event.getMessage().delete());
			if (getPermissionsManager(event.getGuild()).authUsage(getPermissionsManager(event.getGuild()).PERMMGR,
					event.getChannel(), event.getAuthor())) {
				if (PermissionUtils.hasPermissions(event.getGuild(), event.getAuthor(), Permissions.ADMINISTRATOR)) {
					String[] commandWords = { "clear", "filter", "adminlogin", "info", "inspire", "leave",
							"listsettings", "play", "question", "set", "skip", "stop", "volume", "showqueue",
							"permsettings", "player", "management", "utility", "misc" };
					EmbedBuilder message = new EmbedBuilder();
					message.withTitle("Permission IDs");
					message.withColor(Color.ORANGE);
					message.appendField("**Player command IDs (global id: player)**",
							"**play** - thicc play\n" + "**volume** - thicc volume\n" + "**skip** - thicc skip\n"
									+ "**stop** - thicc stop\n" + "**showqueue** - thicc show queue\n"
									+ "**leave** - thicc leave",
							false);
					message.appendField("**Server management command IDs (global id: management)**",
							"**clear** - thicc clear message history\n" + "**filter** - thicc delete messages\n"
									+ "**set** - thicc set\n" + "**listsettings** - thicc list settings\n"
									+ "**adminlogin**:\nthicc get gui login\nthicc get new gui login\n"
									+ "**permsettings** - thicc permission",
							false);
					message.appendField("**Utility command IDs (global id: utility)**",
							"**info** - thicc info\n" + "**question** - wolfram question command", false);
					message.appendField("**Miscellaneous command IDs (global id: misc)**",
							"**inspire** - thicc inspire me", false);
					message.appendField("View the current settings for your guild's permissions:",
							"[Click Here](http://thiccbot.site/pro/permissions?guildid="
									+ event.getGuild().getStringID() + ")",
							false);
					RequestBuffer.request(() -> event.getAuthor().getOrCreatePMChannel().sendMessage(message.build()));
				} else {
					RequestBuffer.request(() -> event.getChannel()
							.sendMessage("You must be an administrator of this server to manage permissions"));
				}
			}
			return true;
		} else {
			return false;
		}
	}

}