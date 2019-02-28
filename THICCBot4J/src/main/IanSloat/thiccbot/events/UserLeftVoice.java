package main.IanSloat.thiccbot.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.commands.Command;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;

public class UserLeftVoice {
	
	private final Logger logger = LoggerFactory.getLogger(UserLeftVoice.class);
	
	public void UserLeftVoiceEvent(UserVoiceChannelLeaveEvent event) {
		try {
			if (event.getGuild().getConnectedVoiceChannel().getStringID()
					.equals(event.getVoiceChannel().getStringID())) {
				logger.info("User: " + event.getUser().getName() + "(id:" + event.getUser().getStringID() + ')'
						+ " disconnected from connected voice channel on guild \"" + event.getGuild().getName()
						+ "\"(id:" + event.getGuild().getLongID() + "). Remaining users: "
						+ (event.getVoiceChannel().getConnectedUsers().size() - 1));
				if (event.getVoiceChannel().getConnectedUsers().size() == 1) {
					logger.info("No more users are currently connected. Left voice channel.");
					GuildMusicManager musicManager = Command.musicManagers.get(event.getGuild().getLongID());
					if(musicManager != null) {
						musicManager.scheduler.stop();
					}
					event.getGuild().getConnectedVoiceChannel().leave();
				}
			}
		} catch (NullPointerException e) {
		}
	}
}
