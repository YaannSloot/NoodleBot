package com.IanSloat.noodlebot.events;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.NoodleBotMain;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;
import com.IanSloat.noodlebot.controllers.settings.GuildSettingsController;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;

/**
 * Handles {@linkplain ReadyEvent}s fired when a shard logs in. Makes sure all
 * shards have a synchronous start once the bot has completely logged in. All
 * guild setting directories will be initialized as well during synchronization.
 */
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
			event.getJDA().getShardManager().getGuilds().forEach(g -> {
				try {
					GuildSettingsController.initGuildSettingsFiles(g);
					GuildPermissionsController.initGuildPermissionsFiles(g);
				} catch (IOException e) {
					logger.error(
							"Failed to init settings directory for guild " + g.getName() + "(id:" + g.getId() + ")");
					e.printStackTrace();
					System.exit(1);
				}
			});
			logger.info("Guild settings file check complete.");
			for (JDA shard : event.getJDA().getShardManager().getShards()) {
				synchronized (shard) {
					shard.notify();
				}
			}
		}

		logger.info("Shard " + event.getJDA().getShardInfo().getShardId() + " is ready.");

	}
}
