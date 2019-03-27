package main.IanSloat.thiccbot.commands;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.threadbox.BulkMessageDeletionJob;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ClearHistoryCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.CLEAR_COMMAND, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "clear message history");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if(!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		final Instant currentTime = Instant.now().minus(7, ChronoUnit.DAYS);
		BulkMessageDeletionJob job = BulkMessageDeletionJob.getDeletionJobForChannel(event.getTextChannel(),
				currentTime);
		job.startJob();
	}
}
