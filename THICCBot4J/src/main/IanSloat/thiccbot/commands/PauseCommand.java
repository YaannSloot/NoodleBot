package main.IanSloat.thiccbot.commands;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.RequestBuffer;

public class PauseCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage("pause", user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "pause");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
		if (voiceChannel != null) {
			GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getChannel());
			if (musicManager.scheduler.isPaused() == false) {
				musicManager.scheduler.pauseTrack();
				RequestBuffer.request(() -> {
					event.getChannel().sendMessage("Paused the current track");
				});
			} else {
				musicManager.scheduler.unpauseTrack();
				RequestBuffer.request(() -> {
					event.getChannel().sendMessage("Unpaused the current track");
				});
			}
		} else {
			RequestBuffer.request(() -> {
				event.getChannel().sendMessage("No tracks are currently playing");
			});
		}
	}
}
