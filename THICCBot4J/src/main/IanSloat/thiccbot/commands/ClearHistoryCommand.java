package main.IanSloat.thiccbot.commands;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.threadbox.BulkMessageDeletionJob;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

public class ClearHistoryCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.CLEAR_COMMAND, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "clear message history");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if(!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		final Instant currentTime = Instant.now().minus(7, ChronoUnit.DAYS);
		BulkMessageDeletionJob job = BulkMessageDeletionJob.getDeletionJobForChannel(event.getChannel(),
				currentTime);
		job.startJob();
	}
}
