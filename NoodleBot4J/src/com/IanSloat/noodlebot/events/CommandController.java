package com.IanSloat.noodlebot.events;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.commands.Command;
import com.IanSloat.noodlebot.commands.DeleteCommand;
import com.IanSloat.noodlebot.commands.HelpCommand;
import com.IanSloat.noodlebot.commands.JumpCommand;
import com.IanSloat.noodlebot.commands.LeaveCommand;
import com.IanSloat.noodlebot.commands.ListSettingsCommand;
import com.IanSloat.noodlebot.commands.PauseCommand;
import com.IanSloat.noodlebot.commands.PlayCommand;
import com.IanSloat.noodlebot.commands.RemoveTrackCommand;
import com.IanSloat.noodlebot.commands.SetCommand;
import com.IanSloat.noodlebot.commands.ShowQueueCommand;
import com.IanSloat.noodlebot.commands.SkipCommand;
import com.IanSloat.noodlebot.commands.StatusCommand;
import com.IanSloat.noodlebot.commands.StopCommand;
import com.IanSloat.noodlebot.commands.VoiceChatKickCommand;
import com.IanSloat.noodlebot.commands.VolumeCommand;
import com.IanSloat.noodlebot.commands.WikiCommand;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Handles command execution
 */
public class CommandController {

	/**
	 * A list of each {@linkplain Command} to be used during runtime
	 */
	public final static List<Command> commandList = Arrays.asList(new HelpCommand(), new DeleteCommand(),
			new StatusCommand(), new ListSettingsCommand(), new PlayCommand(), new JumpCommand(), new LeaveCommand(),
			new PauseCommand(), new SetCommand(), new StopCommand(), new VolumeCommand(), new VoiceChatKickCommand(),
			new SkipCommand(), new ShowQueueCommand(), new WikiCommand(), new RemoveTrackCommand());

	public void CommandEvent(MessageReceivedEvent event) {

		// Message handling block for guild messages
		if (event.getMessage().isFromGuild()
				&& event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX)) {

			boolean didMatch = false;

			for (Command c : commandList) {
				if (c.CheckForCommandMatch(event.getMessage())) {
					if (c.CheckUsagePermission(event.getMember()))
						c.execute(event);
					else
						event.getChannel().sendMessage("You don't have permission to use that command")
								.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					didMatch = true;
					break;
				}
			}

			if (!didMatch)
				event.getChannel().sendMessage("That's not a valid command")
						.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));

		}
	}

}
