package main.IanSloat.thiccbot.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;

public class GuildJoin {

	private final Logger logger = LoggerFactory.getLogger(GuildJoin.class);
	
	public void GuildJoinEvent(GuildCreateEvent event) {
		try {
			if (!(Events.knownGuildIds.contains(event.getGuild().getStringID()))) {
				event.getGuild().getChannels().get(0).sendMessage(
						"Hello! Thanks for adding me to your server.\nFor a list of commands, type \"thicc help\"");
				logger.info("Added to new guild. Guild: " + event.getGuild().getName() + "(id:"
						+ event.getGuild().getStringID() + ")");
				GuildSettingsManager sManager = new GuildSettingsManager(event.getGuild());
				sManager.CreateSettings();
				Events.knownGuildIds.add(event.getGuild().getStringID());
			}
		} catch (sx.blah.discord.util.DiscordException e) {

		}
	}
	
}
