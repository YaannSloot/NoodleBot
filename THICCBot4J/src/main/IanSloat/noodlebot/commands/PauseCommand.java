package main.IanSloat.noodlebot.commands;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PauseCommand extends Command {
	
	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "pause");
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
			if (musicManager.scheduler.isPaused() == false) {
				musicManager.scheduler.pauseTrack();
				event.getChannel().sendMessage("Paused the current track").queue();
			} else {
				musicManager.scheduler.unpauseTrack();
				event.getChannel().sendMessage("Unpaused the current track").queue();
			}
		} else {
			event.getChannel().sendMessage("No tracks are currently playing").queue();
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood pause** - This command is a toggle. It will either pause or unpause the current track";
	}

	@Override
	public String getCommandId() {
		return "pause";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_PLAYER;
	}
}
