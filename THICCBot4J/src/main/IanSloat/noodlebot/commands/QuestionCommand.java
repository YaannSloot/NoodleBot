package main.IanSloat.noodlebot.commands;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.WolframController;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class QuestionCommand extends Command {
	
	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return (command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
				&& BotUtils.checkForWords(command.getContentRaw(), NoodleBotMain.questionIDs, false, true));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		logger.info(event.getMessage().getContentRaw().substring(BotUtils.BOT_PREFIX.length()));
		WolframController waClient = new WolframController(NoodleBotMain.waAppID);
		waClient.askQuestionAndSend(event.getMessage().getContentRaw().substring(BotUtils.BOT_PREFIX.length()),
				event.getTextChannel());
	}

	@Override
	public String getHelpSnippet() {
		return "**nood <question>** - Sends a question to WolframAlpha";
	}

	@Override
	public String getCommandId() {
		return "question";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_UTILITY;
	}
}
