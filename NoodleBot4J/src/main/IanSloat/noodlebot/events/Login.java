package main.IanSloat.noodlebot.events;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;

public class Login {

	private final Logger logger = LoggerFactory.getLogger(Login.class);

	public void BotLoginEvent(ReadyEvent event) {
		logger.info("Shard " + event.getJDA().getShardInfo().getShardId() + " has started.");
		event.getJDA().getPresence().setPresence(OnlineStatus.ONLINE,
				Activity.playing(BotUtils.BOT_PREFIX + "help | Beta v" + NoodleBotMain.versionNumber));

		if (event.getJDA().getShardInfo().getShardId() < event.getJDA().getShardManager().getShardsTotal() - 1) {
			try {
				synchronized (event.getJDA()) {
					logger.info("Shard " + event.getJDA().getShardInfo().getShardId()
							+ " is waiting for other shards to start...");
					event.getJDA().wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("All shards have logged in. Performing guild settings file check...");
			
			for (JDA shard : event.getJDA().getShardManager().getShards()) {
				synchronized (shard) {
					shard.notify();
				}
			}
		}

		logger.info("Shard " + event.getJDA().getShardInfo().getShardId() + " is ready.");

	}
}
