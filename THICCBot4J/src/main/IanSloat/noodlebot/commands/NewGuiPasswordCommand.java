package main.IanSloat.noodlebot.commands;

import java.awt.Color;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NewGuiPasswordCommand extends Command {
	
	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
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
			NBMLSettingsParser setParser = setMgr.getTBMLParser();
			setParser.setScope(NBMLSettingsParser.DOCROOT);
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
			event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(message.build()).queue());
			event.getChannel().sendMessage("Sent you a private message with the login details").queue();
		} else {
			event.getChannel().sendMessage("You must be an administrator of this server to use gui management").queue();
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood get new gui login** - Creates a new guild password for the bot's gui manager";
	}

	@Override
	public String getCommandId() {
		return "adminlogin";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_MANAGEMENT;
	}
}
