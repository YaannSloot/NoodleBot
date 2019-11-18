package com.IanSloat.noodlebot.events;

import java.util.Arrays;
import java.util.List;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.commands.Command;
import com.IanSloat.noodlebot.commands.FilterDeleteCommand;
import com.IanSloat.noodlebot.commands.HelpCommand;
import com.IanSloat.noodlebot.commands.ListSettingsCommand;
import com.IanSloat.noodlebot.commands.StatusCommand;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

// TODO Implement and document
public class CommandController {

	// Active command instances
	public static List<Command> commandList = Arrays.asList(new HelpCommand(), new FilterDeleteCommand(),
			new StatusCommand(), new ListSettingsCommand());

	public void CommandEvent(MessageReceivedEvent event) {

		// Message handling block for guild messages
		if (event.getMessage().isFromGuild() && event.getMessage().getContentRaw().startsWith(BotUtils.BOT_PREFIX)) {

			boolean didMatch = false;

			for (Command c : commandList) {
				if (c.CheckForCommandMatch(event.getMessage())) {
					if (c.CheckUsagePermission(event.getMember()))
						c.execute(event);
					didMatch = true;
					break;
				}
			}

			if (!didMatch)
				event.getChannel().sendMessage("That's not a valid command").queue();

		}
	}

}
