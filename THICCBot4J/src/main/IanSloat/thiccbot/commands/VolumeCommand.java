package main.IanSloat.thiccbot.commands;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.RequestBuffer;

public class VolumeCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.VOLUME, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "volume ");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
		if (voiceChannel != null) {
			String volume = event.getMessage().getContent().substring((BotUtils.BOT_PREFIX + "volume ").length());
			GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
			TBMLSettingsParser setParser = setMgr.getTBMLParser();
			try {
				GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
				setParser.setScope("PlayerSettings");
				if (setParser.getFirstInValGroup("volumecap").equals("off")) {
					musicManager.scheduler.setVolume(Integer.parseInt(volume));
				} else {
					musicManager.scheduler.setVolume(Math.min(200, Math.max(0, Integer.parseInt(volume))));
				}
				musicManager.scheduler.updateStatus();
				event.getChannel().sendMessage("Set volume to " + Integer.parseInt(volume));
			} catch (java.lang.NumberFormatException e) {
				event.getChannel().sendMessage("Setting volume to... wait WHAT?!");
			}
		} else {
			event.getChannel().sendMessage("Not currently connected to any voice channels");
		}
	}
}
