package main.IanSloat.thiccbot.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.ThiccBotMain;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ResumedEvent;

public class Resumed {

	private final Logger logger = LoggerFactory.getLogger(Resumed.class);
	
	public void ResumedEvent(ResumedEvent event) {
		logger.info("Shard " + event.getJDA().getShardInfo().getShardString() + " has resumed its session successfully");
		event.getJDA().getShardManager().setPresence(OnlineStatus.ONLINE,
				Activity.playing(BotUtils.BOT_PREFIX + "help | Beta v" + ThiccBotMain.versionNumber));
	}
	
}
