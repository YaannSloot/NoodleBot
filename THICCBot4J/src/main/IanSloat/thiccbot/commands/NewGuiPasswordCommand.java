package main.IanSloat.thiccbot.commands;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.RequestBuffer;

public class NewGuiPasswordCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.GET_LOGIN, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "get new gui login");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if(!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		if (PermissionUtils.hasPermissions(event.getGuild(), event.getAuthor(), Permissions.ADMINISTRATOR)) {
			EmbedBuilder message = new EmbedBuilder();
			message.appendField("Guild ID:", event.getGuild().getStringID(), false);
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
				message.withTitle("Your server's login credentials");
			} else {
				setParser.setFirstInValGroup("guipasswd", passwd);
				message.withTitle("Your server's new login credentials");
			}
			message.appendField("Special Password:", setParser.getFirstInValGroup("guipasswd"), false);
			message.withColor(0, 255, 0);
			RequestBuffer.request(() -> event.getAuthor().getOrCreatePMChannel().sendMessage(message.build()));
			RequestBuffer.request(
					() -> event.getChannel().sendMessage("Sent you a private message with the login details"));
		} else {
			RequestBuffer.request(() -> event.getChannel()
					.sendMessage("You must be an administrator of this server to use gui management"));
		}
	}
}
