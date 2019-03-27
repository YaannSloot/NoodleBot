package main.IanSloat.thiccbot.commands;

import java.awt.Color;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ListSettingsCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.LIST_SETTINGS, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return (command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "list settings")
				|| command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "settings"));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
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
		response.setColor(new Color(0, 200, 0));
		response.setTitle("Settings | " + event.getGuild().getName());
		response.addField("Voice channel settings",
				"Default volume = " + setParser.getFirstInValGroup("volume") + "\nAutoPlay = "
						+ setParser.getFirstInValGroup("autoplay") + "\nEnforce volume cap = "
						+ setParser.getFirstInValGroup("volumecap"),
				false);
		event.getChannel().sendMessage(response.build()).queue();
	}
}
