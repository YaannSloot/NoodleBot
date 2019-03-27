package main.IanSloat.thiccbot.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.tools.GuildSettingsManager;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;

public class GuildJoin {

	private final Logger logger = LoggerFactory.getLogger(GuildJoin.class);

	public void GuildJoinEvent(GuildJoinEvent event) {
		if (!(Events.knownGuildIds.contains(event.getGuild().getId()))) {
			event.getGuild().getTextChannels().get(0)
					.sendMessage(
							"Hello! Thanks for adding me to your server.\nFor a list of commands, type \"thicc help\"")
					.queue();
			logger.info("Added to new guild. Guild: " + event.getGuild().getName() + "(id:" + event.getGuild().getId()
					+ ")");
			GuildSettingsManager sManager = new GuildSettingsManager(event.getGuild());
			sManager.CreateSettings();
			Events.knownGuildIds.add(event.getGuild().getId());
		}
	}

}
