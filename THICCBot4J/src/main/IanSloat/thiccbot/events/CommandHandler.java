package main.IanSloat.thiccbot.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.commands.*;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.RequestBuffer;

public class CommandHandler {

	private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	private List<Command> commands = new ArrayList<Command>(Arrays.asList(new ClearHistoryCommand(),
			new FilterDeleteCommand(), new GetGuiPasswordCommand(), new HelpCommand(), new InfoCommand(),
			new InspireMeCommand(), new JumpCommand(), new LeaveCommand(), new ListSettingsCommand(),
			new NewGuiPasswordCommand(), new PauseCommand(), new PermIDCommand(), new PlayCommand(),
			new QuestionCommand(), new RemoveTrackCommand(), new SetPermDefaultsCommand(), new SetPermissionCommand(),
			new SettingsCommand(), new ShowQueueCommand(), new SkipCommand(), new StopCommand(), new VolumeCommand()));

	public void MessageReceivedEvent(MessageReceivedEvent event) {
		if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
				&& event.getChannel().isPrivate() == false) {
			logger.info("Message recieved from: " + event.getAuthor().getName() + ", server="
					+ event.getGuild().getName() + ", Content=\"" + event.getMessage() + "\"");

			boolean commandMatch = false;

			if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping")) {
				event.getChannel().sendMessage("Pong!");
				commandMatch = true;
			}

			if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
				event.getChannel().sendMessage("no u");
				commandMatch = true;
			}

			if (!commandMatch) {
				for (Command c : commands) {
					if (c.CheckForCommandMatch(event.getMessage())) {
						commandMatch = true;
						if(c.CheckUsagePermission(event.getAuthor(), Command.getPermissionsManager(event.getGuild()))) {
							try {
								c.execute(event);
							} catch (NoMatchException e) {
								e.printStackTrace();
							}
						} else {
							RequestBuffer.request(() -> {
								event.getChannel().sendMessage("You do not have permission to use that command");
							});
						}
						break;
					}
				}
			}

			if (!commandMatch) {
				int random = (int) (Math.random() * 5 + 1);
				if (random == 1) {
					event.getChannel().sendMessage("What?");
				} else if (random == 2) {
					event.getChannel().sendMessage("What are you saying?");
				} else if (random == 3) {
					event.getChannel().sendMessage("What language is that?");
				} else if (random == 4) {
					event.getChannel().sendMessage("Are you ok?");
				} else {
					event.getChannel().sendMessage("What you're saying makes no sense.");
				}
				logger.info("Message did not match any commands");
			}
		} else if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
				&& event.getChannel().isPrivate()) {
			logger.info("Private message recieved from: " + event.getAuthor().getName() + ", Content=\""
					+ event.getMessage() + "\"");
			if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping"))
				event.getChannel().sendMessage("Pong!");

			if (event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
				event.getChannel().sendMessage("no u");
			}
		}
	}
}