package main.IanSloat.noodlebot.commands;

import java.awt.Color;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.WolframController;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

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
		try {
			event.getMessage().delete().queue();
			logger.info(event.getMessage().getContentRaw().substring(BotUtils.BOT_PREFIX.length()));
			WolframController waClient = new WolframController(NoodleBotMain.waAppID);
			waClient.askQuestionAndSend(event.getMessage().getContentRaw().substring(BotUtils.BOT_PREFIX.length()),
					event.getTextChannel());
		} catch (InsufficientPermissionException e) {
			String permission = e.getPermission().getName();
			EmbedBuilder message = new EmbedBuilder();
			message.setTitle("Missing permission error | " + event.getGuild().getName());
			message.addField("Error message:", "Bot is missing required permission **" + permission
					+ "**. Please grant this permission to the bot's role or contact a guild administrator to apply this permission to the bot's role.",
					false);
			message.setColor(Color.red);
			event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(message.build()).queue());
		}
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
