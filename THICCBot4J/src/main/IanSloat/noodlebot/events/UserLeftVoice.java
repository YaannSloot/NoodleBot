package main.IanSloat.noodlebot.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.noodlebot.commands.Command;
import main.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;


public class UserLeftVoice {
	
	private final Logger logger = LoggerFactory.getLogger(UserLeftVoice.class);
	
	public void UserLeftVoiceEvent(GuildVoiceLeaveEvent event) {
		try {
			if (event.getGuild().getAudioManager().getConnectedChannel().getId()
					.equals(event.getChannelLeft().getId())) {
				logger.info("User: " + event.getMember().getUser().getName() + "(id:" + event.getMember().getId() + ')'
						+ " disconnected from connected voice channel on guild \"" + event.getGuild().getName()
						+ "\"(id:" + event.getGuild().getId() + "). Remaining users: "
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
