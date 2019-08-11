package main.IanSloat.noodlebot.commands;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import main.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import main.IanSloat.noodlebot.overrides.DefaultAudioPlayerManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {

	public final static Map<Guild, PermissionsManager> permManagers = new HashMap<>();
	public final static Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
	public final static AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
	static final Logger logger = LoggerFactory.getLogger(Command.class);
	
	public static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild, TextChannel channel) {
		long guildId = guild.getIdLong();
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager, channel);
			musicManagers.put(guildId, musicManager);
		} else {
			musicManager.setPlayingMessageChannel(channel);
		}

		guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

		return musicManager;
	}
	
	public static synchronized PermissionsManager getPermissionsManager(Guild guild) {
		PermissionsManager permMgr = permManagers.get(guild);

		if (permMgr == null) {
			permMgr = new PermissionsManager(guild);
			permManagers.put(guild, permMgr);
		}

		return permMgr;
	}
	
	public abstract boolean CheckUsagePermission(Member user, PermissionsManager permMgr);
	
	public abstract boolean CheckForCommandMatch(Message command);
	
	public abstract void execute(MessageReceivedEvent event) throws NoMatchException;
	
}
