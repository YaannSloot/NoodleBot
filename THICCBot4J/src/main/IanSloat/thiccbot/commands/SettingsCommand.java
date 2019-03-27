package main.IanSloat.thiccbot.commands;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SettingsCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.SET_COMMAND, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "set ");
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
		String command = BotUtils.normalizeSentence(
				event.getMessage().getContentRaw().toLowerCase().substring((BotUtils.BOT_PREFIX + "set").length()));
		String[] words = command.split(" ");
		if (command.toLowerCase().startsWith("default volume ") || words[0].equals("volume")
				|| command.toLowerCase().startsWith("default volume to ")
				|| command.toLowerCase().startsWith("volume to ")) {
			try {
				int value;
				if (command.toLowerCase().startsWith("default volume ")
						|| command.toLowerCase().startsWith("volume to ")) {
					value = Integer.parseInt(words[2]);
				} else if (command.toLowerCase().startsWith("default volume to ")) {
					value = Integer.parseInt(words[3]);
				} else {
					value = Integer.parseInt(words[1]);
				}
				setParser.setScope(TBMLSettingsParser.DOCROOT);
				setParser.addObj("PlayerSettings");
				setParser.setScope("PlayerSettings");
				if (setParser.getFirstInValGroup("volume").equals("")) {
					setParser.addVal("volume", "100");
				}
				setParser.setFirstInValGroup("volume", Integer.toString(value));
				event.getChannel().sendMessage("Changed default volume to " + value);
			} catch (NumberFormatException e) {
				event.getChannel().sendMessage("The value provided is not valid for that setting");
			}
		} else if (command.toLowerCase().startsWith("autoplay ") && words.length >= 2) {
			setParser.setScope(TBMLSettingsParser.DOCROOT);
			setParser.addObj("PlayerSettings");
			setParser.setScope("PlayerSettings");
			if (setParser.getFirstInValGroup("autoplay").equals("")) {
				setParser.addVal("autoplay", "off");
			}
			if (words[1].toLowerCase().equals("on")) {
				setParser.setFirstInValGroup("autoplay", "on");
				event.getChannel().sendMessage("Set AutoPlay to \'on\'");
			} else if (words[1].toLowerCase().equals("off")) {
				setParser.setFirstInValGroup("autoplay", "off");
				event.getChannel().sendMessage("Set AutoPlay to \'off\'");
			} else {
				event.getChannel().sendMessage("The value provided is not valid for that setting");
			}
		} else if (command.toLowerCase().startsWith("volumecap ") && words.length >= 2) {
			setParser.setScope(TBMLSettingsParser.DOCROOT);
			setParser.addObj("PlayerSettings");
			setParser.setScope("PlayerSettings");
			if (setParser.getFirstInValGroup("volumecap").equals("")) {
				setParser.addVal("volumecap", "on");
			}
			if (words[1].toLowerCase().equals("on")) {
				setParser.setFirstInValGroup("volumecap", "on");
				event.getChannel().sendMessage("Set volume limit to \'on\'");
			} else if (words[1].toLowerCase().equals("off")) {
				setParser.setFirstInValGroup("volumecap", "off");
				event.getChannel().sendMessage("Set volume limit to \'off\'");
			} else {
				event.getChannel().sendMessage("The value provided is not valid for that setting");
			}
		} else {
			event.getChannel().sendMessage("That is not a valid setting");
		}
	}
}
