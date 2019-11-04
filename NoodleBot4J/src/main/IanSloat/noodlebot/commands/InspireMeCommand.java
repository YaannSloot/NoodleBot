package main.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.sql.Date;
import java.text.SimpleDateFormat;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.jdaevents.GenericCommandErrorEvent;
import main.IanSloat.noodlebot.jdaevents.GenericCommandEvent;
import main.IanSloat.noodlebot.tools.InspirobotClient;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class InspireMeCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "inspire me");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		NoodleBotMain.eventListener.onEvent(new GenericCommandEvent(event.getJDA(), event.getResponseNumber(),
				event.getGuild(), this, event.getMessage().getContentRaw().toLowerCase(), event.getMember()));
		try {
			event.getMessage().delete().queue();
			InspirobotClient iClient = new InspirobotClient();
			EmbedBuilder message = new EmbedBuilder();
			message.setImage(iClient.getNewImageUrl());
			message.setTitle("Here you go. Now start feeling inspired");
			message.setDescription("Brought to you by [InspiroBot\u2122](https://inspirobot.me/)");
			message.setFooter(
					"Image requested by " + event.getAuthor().getName() + " | " + new SimpleDateFormat("MM/dd/yyyy")
							.format(Date.from(event.getMessage().getTimeCreated().toInstant())),
					null);
			message.setColor(new Color(220, 20, 60));
			event.getChannel().sendMessage(message.build()).queue();
		} catch (InsufficientPermissionException e) {
			String permission = e.getPermission().getName();
			EmbedBuilder message = new EmbedBuilder();
			message.setTitle("Missing permission error | " + event.getGuild().getName());
			message.addField("Error message:", "Bot is missing required permission **" + permission
					+ "**. Please grant this permission to the bot's role or contact a guild administrator to apply this permission to the bot's role.",
					false);
			message.setColor(Color.red);
			event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(message.build()).queue());
			NoodleBotMain.eventListener.onEvent(new GenericCommandErrorEvent(event.getJDA(),
					event.getResponseNumber(), event.getGuild(), this, event.getMessage().getContentRaw(),
					event.getMember(), "Command execution failed due to missing permission: " + permission));
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood inspire me** - Shows an inspirational image from InspiroBot\u2122";
	}

	@Override
	public String getCommandId() {
		return "inspire";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_MISC;
	}
}
