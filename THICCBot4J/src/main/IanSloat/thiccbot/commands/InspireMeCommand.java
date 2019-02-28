package main.IanSloat.thiccbot.commands;

import java.sql.Date;
import java.text.SimpleDateFormat;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.InspirobotClient;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class InspireMeCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.INSPIRE_ME, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "inspire me");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		InspirobotClient iClient = new InspirobotClient();
		EmbedBuilder message = new EmbedBuilder();
		message.withImage(iClient.getNewImageUrl());
		message.withTitle("Here you go. Now start feeling inspired");
		message.withDesc("Brought to you by [InspiroBot\u2122](https://inspirobot.me/)");
		message.withFooterText("Image requested by " + event.getAuthor().getName() + " | "
				+ new SimpleDateFormat("MM/dd/yyyy").format(Date.from(event.getMessage().getTimestamp())));
		message.withColor(220, 20, 60);
		RequestBuffer.request(() -> event.getChannel().sendMessage(message.build()));
	}
}
