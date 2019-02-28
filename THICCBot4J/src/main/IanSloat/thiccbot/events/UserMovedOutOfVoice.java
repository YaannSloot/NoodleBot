package main.IanSloat.thiccbot.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.commands.Command;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;

public class UserMovedOutOfVoice {

	private final Logger logger = LoggerFactory.getLogger(UserMovedOutOfVoice.class);
	
	void UserMovedOutOfVoiceEvent(UserVoiceChannelMoveEvent event) {
		try {
			if (event.getGuild().getConnectedVoiceChannel().getStringID().equals(event.getOldChannel().getStringID())) {
				logger.info("User: " + event.getUser().getName() + "(id:" + event.getUser().getStringID() + ')'
						+ " moved out of connected voice channel on guild \"" + event.getGuild().getName() + "\"(id:"
						+ event.getGuild().getLongID() + "). Remaining users: "
						+ (event.getOldChannel().getConnectedUsers().size() - 1));
				if (event.getOldChannel().getConnectedUsers().size() == 1) {
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
