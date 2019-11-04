package main.IanSloat.noodlebot.commands;

import java.awt.Color;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.jdaevents.GenericCommandErrorEvent;
import main.IanSloat.noodlebot.jdaevents.GenericCommandEvent;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class GetGuiPasswordCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "get gui login");
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
			if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				EmbedBuilder message = new EmbedBuilder();
				message.setTitle("Your server's login credentials");
				message.addField("Guild ID:", event.getGuild().getId(), false);
				GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
				NBMLSettingsParser setParser = setMgr.getNBMLParser();
				setParser.setScope(NBMLSettingsParser.DOCROOT);
				setParser.addObj("GuiSettings");
				setParser.setScope("GuiSettings");
				if (setParser.getFirstInValGroup("guipasswd").equals("")) {
					String passwd = "";
					for (int i = 0; i < 32; i++) {
						passwd += (char) (int) (Math.random() * 93 + 34);
					}
					setParser.addVal("guipasswd", passwd);
				}
				message.addField("Special Password:", setParser.getFirstInValGroup("guipasswd"), false);
				message.setColor(new Color(0, 255, 0));
				event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(message.build()).queue());
				event.getChannel().sendMessage("Sent you a private message with the login details").queue();
			} else {
				event.getChannel().sendMessage("You must be an administrator of this server to use gui management")
						.queue();
			}
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
		return "**nood get gui login** - Creates a guild password for the bot's gui manager";
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
