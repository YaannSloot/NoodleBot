package main.IanSloat.thiccbot.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.ThiccBotMain;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReconnectedEvent;

public class Reconnected {

	private final Logger logger = LoggerFactory.getLogger(Reconnected.class);
	
	public void ReconnectEvent(ReconnectedEvent event) {
		logger.info("Shard " + event.getJDA().getShardInfo().getShardString() + " has reconnected to the gateway successfully");
		event.getJDA().getShardManager().setPresence(OnlineStatus.ONLINE,
				Activity.playing(BotUtils.BOT_PREFIX + "help | Beta v" + ThiccBotMain.versionNumber));
	}
	
}
