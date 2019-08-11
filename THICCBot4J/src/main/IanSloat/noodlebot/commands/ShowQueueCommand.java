package main.IanSloat.noodlebot.commands;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ShowQueueCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.SHOW_QUEUE, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "show queue");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getTextChannel());
		if (musicManager.scheduler.getPlaylist().size() > 0) {
			musicManager.scheduler.setPlaylistDisplay(true);
			musicManager.scheduler.updateStatus();
		} else {
			event.getChannel().sendMessage("Queue is currently empty").queue();
		}
	}
}
