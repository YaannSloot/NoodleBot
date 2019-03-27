package main.IanSloat.thiccbot.commands;

import java.awt.Color;
import java.sql.Date;
import java.text.SimpleDateFormat;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.InspirobotClient;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class InspireMeCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.INSPIRE_ME, user);
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
		event.getMessage().delete().queue();
		InspirobotClient iClient = new InspirobotClient();
		EmbedBuilder message = new EmbedBuilder();
		message.setImage(iClient.getNewImageUrl());
		message.setTitle("Here you go. Now start feeling inspired");
		message.setDescription("Brought to you by [InspiroBot\u2122](https://inspirobot.me/)");
		message.setFooter("Image requested by " + event.getAuthor().getName() + " | "
				+ new SimpleDateFormat("MM/dd/yyyy").format(Date.from(event.getMessage().getTimeCreated().toInstant())), null);
		message.setColor(new Color(220, 20, 60));
		event.getChannel().sendMessage(message.build()).queue();
	}
}
