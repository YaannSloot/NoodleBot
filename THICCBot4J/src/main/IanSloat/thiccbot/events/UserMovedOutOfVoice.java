package main.IanSloat.thiccbot.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.commands.Command;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;


public class UserMovedOutOfVoice {

	private final Logger logger = LoggerFactory.getLogger(UserMovedOutOfVoice.class);
	
	void UserMovedOutOfVoiceEvent(GuildVoiceMoveEvent event) {
		try {
			if (event.getGuild().getAudioManager().getConnectedChannel().getId().equals(event.getChannelLeft().getId())) {
				logger.info("User: " + event.getMember().getUser().getName() + "(id:" + event.getMember().getId() + ')'
						+ " moved out of connected voice channel on guild \"" + event.getGuild().getName() + "\"(id:"
						+ event.getGuild().getId() + "). Remaining users: "
						+ (event.getChannelLeft().getMembers().size() - 1));
				if (event.getChannelLeft().getMembers().size() == 1) {
					logger.info("No more users are currently connected. Left voice channel.");
					GuildMusicManager musicManager = Command.musicManagers.get(event.getGuild().getIdLong());
					if(musicManager != null) {
						musicManager.scheduler.stop();
					}
					event.getGuild().getAudioManager().closeAudioConnection();
				}
			}
		} catch (NullPointerException e) {
		}
	}
}
