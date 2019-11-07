package main.IanSloat.noodlebot.events;

import java.util.Arrays;
import java.util.List;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.commands.Command;
import main.IanSloat.noodlebot.commands.FilterDeleteCommand;
import main.IanSloat.noodlebot.commands.NoMatchException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

// TODO Implement and document
public class CommandController {

	// Active command instances
	private List<Command> commandList = Arrays.asList(new FilterDeleteCommand());

	public void CommandEvent(MessageReceivedEvent event) {

		// Message handling block for guild messages
		if (event.getMessage().isFromGuild() && event.getMessage().getContentRaw().startsWith(BotUtils.BOT_PREFIX)) {

			boolean didMatch = false;

			for (Command c : commandList) {
				if (c.CheckForCommandMatch(event.getMessage())) {
					if (c.CheckUsagePermission(event.getMember()))
						try {
							c.execute(event);
						} catch (NoMatchException e) {
							e.printStackTrace();
						}
					didMatch = true;
					break;
				}
			}
			
			if(!didMatch)
				event.getChannel().sendMessage("That's not a valid command").queue();

		}
	}

}
