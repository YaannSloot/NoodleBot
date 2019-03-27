package main.IanSloat.thiccbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RemoveTrackCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage("queuemanage", user);
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
		event.getMessage().delete().queue();
		try {
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
					Message response = event.getChannel()
							.sendMessage("Please only specify either a single track or one range of tracks").submit()
							.get();
					response.delete().queueAfter(5, TimeUnit.SECONDS);
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
						Message response = event.getChannel()
								.sendMessage("Please only reference tracks via whole numbers in base 10").submit()
								.get();
						response.delete().queueAfter(5, TimeUnit.SECONDS);
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
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}
