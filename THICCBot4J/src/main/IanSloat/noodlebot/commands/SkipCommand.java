package main.IanSloat.noodlebot.commands;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SkipCommand extends Command {
	
	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "skip");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		VoiceChannel voiceChannel = event.getGuild().getAudioManager().getConnectedChannel();
		if (voiceChannel != null) {
			GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getTextChannel());
			List<AudioTrack> tracks = musicManager.scheduler.getPlaylist();
			if (tracks.isEmpty()) {
				if (musicManager.player.getPlayingTrack() != null) {
					musicManager.player.playTrack(null);
					musicManager.scheduler.nextTrack();
					event.getChannel().sendMessage("Track skipped")
							.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
				}
			} else if (tracks.size() > 0) {
				musicManager.player.playTrack(null);
				musicManager.scheduler.nextTrack();
				event.getChannel().sendMessage("Track skipped")
						.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
			} else {
				event.getChannel().sendMessage("No tracks are playing or queued")
						.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		} else {
			event.getChannel().sendMessage("Not currently connected to any voice channels")
					.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood skip** - Skips the currently playing song";
	}

	@Override
	public String getCommandId() {
		return "skip";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_PLAYER;
	}
}
