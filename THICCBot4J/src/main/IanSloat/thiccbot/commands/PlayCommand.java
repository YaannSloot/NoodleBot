package main.IanSloat.thiccbot.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.threadbox.MessageDeleteTools;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class PlayCommand extends Command {
	
	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.PLAY, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return (command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")
				|| command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "add"));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
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
				playerManager.loadItem("" + videoURL, new AudioLoadResultHandler() {
					@Override
					public void trackLoaded(AudioTrack track) {
						logger.info("A track was loaded");
						if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")) {
							musicManager.scheduler.stop();
						} else {
							IMessage result = RequestBuffer.request(() -> {
								return event.getChannel().sendMessage("Added " + track.getInfo().title + " to queue");
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
						if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")) {
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
							if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "add")) {
								IMessage result = RequestBuffer.request(() -> {
									return event.getChannel().sendMessage(
											"Added " + playlist.getTracks().get(0).getInfo().title + " to queue");
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
				musicManager.scheduler.unpauseTrack();
			} else {
				event.getChannel().sendMessage("Get in a voice channel first");
			}
		} catch (java.lang.StringIndexOutOfBoundsException e) {
			IMessage errorMessage = RequestBuffer.request(() -> {
				return event.getChannel().sendMessage("Play what?");
			}).get();
			MessageDeleteTools.DeleteAfterMillis(errorMessage, 5000);
		}
	}
}
