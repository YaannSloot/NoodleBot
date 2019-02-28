package main.IanSloat.thiccbot.commands;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class ListSettingsCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.LIST_SETTINGS, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return (command.getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "list settings")
				|| command.getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "settings"));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
		TBMLSettingsParser setParser = setMgr.getTBMLParser();
		setParser.setScope(TBMLSettingsParser.DOCROOT);
		setParser.addObj("PlayerSettings");
		setParser.setScope("PlayerSettings");
		if (setParser.getFirstInValGroup("volume").equals("")) {
			setParser.addVal("volume", "100");
		}
		if (setParser.getFirstInValGroup("autoplay").equals("")) {
			setParser.addVal("autoplay", "off");
		}
		if (setParser.getFirstInValGroup("volumecap").equals("")) {
			setParser.addVal("volumecap", "on");
		}
		EmbedBuilder response = new EmbedBuilder();
		response.withColor(0, 200, 0);
		response.withTitle("Settings | " + event.getGuild().getName());
		response.appendField("Voice channel settings",
				"Default volume = " + setParser.getFirstInValGroup("volume") + "\nAutoPlay = "
						+ setParser.getFirstInValGroup("autoplay") + "\nEnforce volume cap = "
						+ setParser.getFirstInValGroup("volumecap"),
				false);
		RequestBuffer.request(() -> event.getChannel().sendMessage(response.build()));
	}
}
