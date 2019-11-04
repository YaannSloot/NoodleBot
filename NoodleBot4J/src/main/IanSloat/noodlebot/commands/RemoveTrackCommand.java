package main.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.jdaevents.GenericCommandErrorEvent;
import main.IanSloat.noodlebot.jdaevents.GenericCommandEvent;
import main.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class RemoveTrackCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "remove track");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		NoodleBotMain.eventListener.onEvent(new GenericCommandEvent(event.getJDA(), event.getResponseNumber(),
				event.getGuild(), this, event.getMessage().getContentRaw().toLowerCase(), event.getMember()));
		try {
			event.getMessage().delete().queue();
			VoiceChannel voiceChannel = event.getGuild().getAudioManager().getConnectedChannel();
			if (voiceChannel != null) {
				String command = event.getMessage().getContentRaw();
				command = command.replace(BotUtils.BOT_PREFIX + "remove track", "");
				command = BotUtils.normalizeSentence(command);
				List<String> wordList = Arrays.asList(command.split(" "));
				command = "";
				for (String word : wordList) {
					command += word;
				}
				wordList = Arrays.asList(command.split("-"));
				if (wordList.size() < 1 || wordList.size() > 2) {
					event.getChannel().sendMessage("Please only specify either a single track or one range of tracks")
							.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
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
						event.getChannel().sendMessage("Please only reference tracks via whole numbers in base 10")
								.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
					} else {
						GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getTextChannel());
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
				event.getChannel().sendMessage("No tracks are currently playing").queue();
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
			NoodleBotMain.eventListener.onEvent(new GenericCommandErrorEvent(event.getJDA(),
					event.getResponseNumber(), event.getGuild(), this, event.getMessage().getContentRaw(),
					event.getMember(), "Command execution failed due to missing permission: " + permission));
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood remove track <track number/range of track numbers>** - Removes a track or range of tracks from the queue";
	}

	@Override
	public String getCommandId() {
		return "queuemanage";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_PLAYER;
	}
}
