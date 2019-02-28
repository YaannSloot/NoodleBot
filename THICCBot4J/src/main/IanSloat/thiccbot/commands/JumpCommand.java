package main.IanSloat.thiccbot.commands;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.errors.MalformedTimecodeException;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.threadbox.MessageDeleteTools;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.Timecode;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.RequestBuffer;

public class JumpCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage("jump", user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "jump to");
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
			String command = event.getMessage().getContent();
			command = command.replace(BotUtils.BOT_PREFIX + "jump to", "");
			command = BotUtils.normalizeSentence(command);
			command = command.replace(" ", "");
			Timecode timecode = new Timecode(command);
			try {
				timecode.decode();
				long currentLength = musicManager.player.getPlayingTrack().getDuration();
				if (timecode.getMillis() > currentLength) {
					musicManager.player.playTrack(null);
					musicManager.scheduler.nextTrack();
					IMessage commandMessage = RequestBuffer.request(() -> {
						return event.getChannel().sendMessage("Track was skipped");
					}).get();
					MessageDeleteTools.DeleteAfterMillis(commandMessage, 5000);
				} else {
					musicManager.player.getPlayingTrack().setPosition(timecode.getMillis());
					final String time = command;
					IMessage commandMessage = RequestBuffer.request(() -> {
						return event.getChannel().sendMessage("Set position to " + time);
					}).get();
					MessageDeleteTools.DeleteAfterMillis(commandMessage, 5000);
				}
			} catch (NumberFormatException | NullPointerException e) {
				IMessage commandMessage = RequestBuffer.request(() -> {
					return event.getChannel().sendMessage("Numbers only please");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(commandMessage, 5000);
			} catch (MalformedTimecodeException e) {
				IMessage commandMessage = RequestBuffer.request(() -> {
					return event.getChannel().sendMessage("That's not a valid timecode");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(commandMessage, 5000);
			}
		}
	}
}
