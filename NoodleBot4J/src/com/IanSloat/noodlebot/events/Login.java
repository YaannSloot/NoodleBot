package com.IanSloat.noodlebot.events;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.NoodleBotMain;
import com.IanSloat.noodlebot.controllers.settings.GuildSetting;
import com.IanSloat.noodlebot.controllers.settings.GuildSettings;
import com.IanSloat.noodlebot.controllers.settings.GuildSettingsController;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;

// TODO This may need to be documented as well
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
			GuildSettingsController.setInitBehavior(settings -> {
				GuildSettings settingsList = settings.getSettings();
				if (!settingsList.contains("volume"))
					settings.setSetting(new GuildSetting("volume", "100", "Default volume", "music", "range!0-200"));
				if (!settingsList.contains("autoplay"))
					settings.setSetting(new GuildSetting("autoplay", "on", "AutoPlay", "music", "off", "on"));
				if (!settingsList.contains("volcap"))
					settings.setSetting(new GuildSetting("volcap", "on", "Enforce volume cap", "music", "off", "on"));
				if (!settingsList.contains("logchannel"))
					settings.setSetting(new GuildSetting("logchannel", "disabled", "Logger channel", "logging",
							"type!TextChannel"));
				if (!settingsList.contains("logmentions"))
					settings.setSetting(new GuildSetting("logmentions", "false", "Use @ mentions on entries", "logging",
							"false", "true"));
				try {
					settings.writeSettings();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			event.getJDA().getShardManager().getGuilds().forEach(g -> {
				try {
					GuildSettingsController.initGuildSettingsFiles(g);
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
