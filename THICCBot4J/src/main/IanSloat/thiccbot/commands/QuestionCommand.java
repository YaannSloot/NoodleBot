package main.IanSloat.thiccbot.commands;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.ThiccBotMain;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.WolframController;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

public class QuestionCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.QUESTION, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return (command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
				&& BotUtils.checkForWords(command.getContent(), ThiccBotMain.questionIDs, false, true));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		logger.info(event.getMessage().getContent().substring(BotUtils.BOT_PREFIX.length()));
		WolframController waClient = new WolframController(ThiccBotMain.waAppID);
		waClient.askQuestionAndSend(event.getMessage().getContent().substring(BotUtils.BOT_PREFIX.length()),
				event.getChannel());
	}
}
