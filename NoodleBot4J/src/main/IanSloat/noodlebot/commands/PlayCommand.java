package main.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class PlayCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return (command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")
				|| command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "add"));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		try {
			event.getMessage().delete().queue();
			try {
				String URL = "";
				if (event.getMessage().getAttachments().size() == 0) {
					URL = event.getMessage().getContentRaw().substring((BotUtils.BOT_PREFIX + "play ").length());
				} else {
					URL = event.getMessage().getAttachments().get(0).getUrl();
				}
				VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
				if (voiceChannel != null) {
					event.getGuild().getAudioManager().openAudioConnection(voiceChannel);
					EmbedBuilder thinkingMsg = new EmbedBuilder();
					thinkingMsg.setTitle("Loading audio...");
					thinkingMsg.setColor(new Color(192, 255, 0));
					if (!(URL.startsWith("http://") || URL.startsWith("https://") || URL.startsWith("scsearch:"))) {
						URL = "ytsearch:" + URL;
					}
					final String videoURL = URL;
					event.getChannel().sendMessage(thinkingMsg.build()).queue(new Consumer<Message>() {

						@Override
						public void accept(Message message) {
							GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(),
									event.getTextChannel());
							GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
							NBMLSettingsParser setParser = setMgr.getNBMLParser();
							setParser.setScope(NBMLSettingsParser.DOCROOT);
							setParser.addObj("PlayerSettings");
							setParser.setScope("PlayerSettings");
							if (setParser.getFirstInValGroup("volume").equals(""))
								setParser.addVal("volume", "100");
							musicManager.scheduler.setVolume(Integer.parseInt(setParser.getFirstInValGroup("volume")));
							final String URI = videoURL;
							playerManager.loadItem("" + videoURL, new AudioLoadResultHandler() {
								@Override
								public void trackLoaded(AudioTrack track) {
									logger.info("A track was loaded");
									if (event.getMessage().getContentRaw().toLowerCase()
											.startsWith(BotUtils.BOT_PREFIX + "play")) {
										musicManager.scheduler.stop();
									} else {
										event.getChannel().sendMessage("Added " + track.getInfo().title + " to queue")
												.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
										musicManager.scheduler.updateStatus();
									}
									musicManager.scheduler.queue(track);
									message.delete().queue();
								}

								@Override
								public void playlistLoaded(AudioPlaylist playlist) {
									logger.info("A track playlist was loaded");
									if (event.getMessage().getContentRaw().toLowerCase()
											.startsWith(BotUtils.BOT_PREFIX + "play")) {
										musicManager.scheduler.stop();
									}
									if ((!URI.startsWith("ytsearch:") || !URI.startsWith("scsearch:")
											|| setParser.getFirstInValGroup("autoplay").equals("on"))
											&& event.getMessage().getContentRaw().toLowerCase()
													.startsWith(BotUtils.BOT_PREFIX + "play")) {
										event.getChannel()
												.sendMessage("Loaded " + playlist.getTracks().size() + " tracks")
												.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
										for (AudioTrack track : playlist.getTracks()) {
											musicManager.scheduler.queue(track);
										}
									} else {
										logger.info("Was a search and autoplay is off so only one track was loaded");
										musicManager.scheduler.queue(playlist.getTracks().get(0));
										if (event.getMessage().getContentRaw().toLowerCase()
												.startsWith(BotUtils.BOT_PREFIX + "add")) {
											event.getChannel()
													.sendMessage("Added " + playlist.getTracks().get(0).getInfo().title
															+ " to queue")
													.queue((message) -> message.delete().queueAfter(5,
															TimeUnit.SECONDS));
											musicManager.scheduler.updateStatus();
										}
									}
									message.delete().queue();
								}

								@Override
								public void noMatches() {
									// Notify the user that we've got nothing
									EmbedBuilder newMsg = new EmbedBuilder();
									newMsg.setTitle("No results found");
									newMsg.setColor(new Color(255, 0, 0));
									message.editMessage(newMsg.build()).queue();
									message.delete().queueAfter(5, TimeUnit.SECONDS);
									logger.info("Audio track search returned no results");
								}

								@Override
								public void loadFailed(FriendlyException throwable) {
									// Notify the user that everything exploded
									EmbedBuilder newMsg = new EmbedBuilder();
									newMsg.setTitle("An error occurred while attempting to load the requested audio\n"
											+ "The URL may be invalid\n"
											+ "If the URL is a stream, the stream can only be played if it is live");
									newMsg.setColor(new Color(255, 0, 0));
									message.editMessage(newMsg.build()).queue();
									message.delete().queueAfter(5, TimeUnit.SECONDS);
									logger.info("An error occurred while attempting to load an audio track");
								}
							});
							musicManager.scheduler.unpauseTrack();
						}

					});

				} else {
					event.getChannel().sendMessage("Get in a voice channel first").queue();
				}
			} catch (java.lang.StringIndexOutOfBoundsException e) {
				event.getChannel().sendMessage("Play what?")
						.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		} catch (InsufficientPermissionException e) {
			String permission = e.getPermission().getName();
			EmbedBuilder message = new EmbedBuilder();
			message.setTitle("Missing permission error | " + event.getGuild().getName());
			message.addField("Error message:", "Bot is missing required permission **" + permission
					+ "**. Please grant this permission to the bot's role or contact a guild administrator to apply this permission to the bot's role.",
					false);
			message.setColor(Color.red);
			event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(message.build()).queue());
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood play <[scsearch:]Video name|Video URL>** - Plays a video or song";
	}

	@Override
	public String getCommandId() {
		return "play";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_PLAYER;
	}
}
