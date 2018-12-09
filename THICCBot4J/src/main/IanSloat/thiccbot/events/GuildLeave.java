package main.IanSloat.thiccbot.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;

public class GuildLeave {
	
	private final Logger logger = LoggerFactory.getLogger(GuildLeave.class);
	
	public void GuildLeaveEvent(GuildLeaveEvent event) {
		Events.knownGuildIds.remove(event.getGuild().getStringID());
		GuildSettingsManager sManager = new GuildSettingsManager(event.getGuild());
		sManager.RemoveSettings();
		logger.info("Removed from guild " + event.getGuild().getStringID() + ". Removed settings files");
	}
}
