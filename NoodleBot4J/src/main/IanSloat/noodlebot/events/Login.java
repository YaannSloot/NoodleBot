package main.IanSloat.noodlebot.events;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
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
			List<Guild> noSetGuilds = event.getJDA().getShardManager().getGuilds();
			noSetGuilds = noSetGuilds.stream().filter(g -> !(new File(g.getId()).exists()))
					.collect(Collectors.toList());
			try {
				if (!(new File("GuildSettings").exists())) {
					FileUtils.forceMkdir(new File("guilds"));
				}
				for (Guild d : noSetGuilds) {
					File newDir = new File("guilds/" + d.getId());
					FileUtils.forceMkdir(newDir);
					logger.info("Created new settings directory for guild " + d.getName() + "(" + d.getId()
							+ ") at path " + newDir.getPath());
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
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
