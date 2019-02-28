package main.IanSloat.thiccbot.commands;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.threadbox.MessageDeleteTools;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.RequestBuffer;

public class StopCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.STOP, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "stop");
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
			musicManager.scheduler.stop();
			IMessage commandMessage = RequestBuffer.request(() -> {
				return event.getChannel().sendMessage("Stopped the current track");
			}).get();
			MessageDeleteTools.DeleteAfterMillis(commandMessage, 5000);
		} else {
			IMessage commandMessage = RequestBuffer.request(() -> {
				return event.getChannel().sendMessage("Not currently connected to any voice channels");
			}).get();
			MessageDeleteTools.DeleteAfterMillis(commandMessage, 5000);
		}
	}
}
