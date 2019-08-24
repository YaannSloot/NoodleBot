package main.IanSloat.noodlebot.commands;

import java.util.concurrent.TimeUnit;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class VolumeCommand extends Command {
	
	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
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
			NBMLSettingsParser setParser = setMgr.getTBMLParser();
			try {
				GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getTextChannel());
				setParser.setScope("PlayerSettings");
				if (setParser.getFirstInValGroup("volumecap").equals("off")) {
					musicManager.scheduler.setVolume(Integer.parseInt(volume));
				} else {
					musicManager.scheduler.setVolume(Math.min(200, Math.max(0, Integer.parseInt(volume))));
				}
				musicManager.scheduler.updateStatus();
				event.getChannel().sendMessage("Set volume to " + Integer.parseInt(volume)).queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
			} catch (java.lang.NumberFormatException e) {
				event.getChannel().sendMessage("Setting volume to... wait WHAT?!").queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		} else {
			event.getChannel().sendMessage("Not currently connected to any voice channels").queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood volume <0-200>** - Changes the player volume";
	}

	@Override
	public String getCommandId() {
		return "volume";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_PLAYER;
	}
}
