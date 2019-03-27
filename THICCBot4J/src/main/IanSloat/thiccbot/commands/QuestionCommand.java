package main.IanSloat.thiccbot.commands;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.ThiccBotMain;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.WolframController;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class QuestionCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.QUESTION, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return (command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
				&& BotUtils.checkForWords(command.getContentRaw(), ThiccBotMain.questionIDs, false, true));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		logger.info(event.getMessage().getContentRaw().substring(BotUtils.BOT_PREFIX.length()));
		WolframController waClient = new WolframController(ThiccBotMain.waAppID);
		waClient.askQuestionAndSend(event.getMessage().getContentRaw().substring(BotUtils.BOT_PREFIX.length()),
				event.getTextChannel());
	}
}
