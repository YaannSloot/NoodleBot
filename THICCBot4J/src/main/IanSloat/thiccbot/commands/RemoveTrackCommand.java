package main.IanSloat.thiccbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.threadbox.MessageDeleteTools;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.RequestBuffer;

public class RemoveTrackCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage("queuemanage", user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "remove track");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
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
}
