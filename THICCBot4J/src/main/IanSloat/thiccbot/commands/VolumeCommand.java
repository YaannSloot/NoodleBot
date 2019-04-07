package main.IanSloat.thiccbot.commands;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class VolumeCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.VOLUME, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "volume ");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		VoiceChannel voiceChannel = event.getGuild().getAudioManager().getConnectedChannel();
		if (voiceChannel != null) {
			String volume = event.getMessage().getContentRaw().substring((BotUtils.BOT_PREFIX + "volume ").length());
			GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
			TBMLSettingsParser setParser = setMgr.getTBMLParser();
			try {
				GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getTextChannel());
				setParser.setScope("PlayerSettings");
				if (setParser.getFirstInValGroup("volumecap").equals("off")) {
					musicManager.scheduler.setVolume(Integer.parseInt(volume));
				} else {
					musicManager.scheduler.setVolume(Math.min(200, Math.max(0, Integer.parseInt(volume))));
				}
				musicManager.scheduler.updateStatus();
				event.getChannel().sendMessage("Set volume to " + Integer.parseInt(volume)).queue();
			} catch (java.lang.NumberFormatException e) {
				event.getChannel().sendMessage("Setting volume to... wait WHAT?!").queue();
			}
		} else {
			event.getChannel().sendMessage("Not currently connected to any voice channels").queue();
		}
	}
}
