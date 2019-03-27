package main.IanSloat.thiccbot.commands;

import java.awt.Color;
import java.util.concurrent.ExecutionException;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NewGuiPasswordCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.GET_LOGIN, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "get new gui login");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if(!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			EmbedBuilder message = new EmbedBuilder();
			message.addField("Guild ID:", event.getGuild().getId(), false);
			GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
			TBMLSettingsParser setParser = setMgr.getTBMLParser();
			setParser.setScope(TBMLSettingsParser.DOCROOT);
			setParser.addObj("GuiSettings");
			setParser.setScope("GuiSettings");
			String passwd = "";
			for (int i = 0; i < 32; i++) {
				passwd += (char) (int) (Math.random() * 93 + 34);
			}
			if (setParser.getFirstInValGroup("guipasswd").equals("")) {
				setParser.addVal("guipasswd", passwd);
				message.setTitle("Your server's login credentials");
			} else {
				setParser.setFirstInValGroup("guipasswd", passwd);
				message.setTitle("Your server's new login credentials");
			}
			message.addField("Special Password:", setParser.getFirstInValGroup("guipasswd"), false);
			message.setColor(new Color(0, 255, 0));
			try {
				event.getAuthor().openPrivateChannel().submit().get().sendMessage(message.build());
				event.getChannel().sendMessage("Sent you a private message with the login details").queue();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			event.getChannel().sendMessage("You must be an administrator of this server to use gui management").queue();
		}
	}
}
