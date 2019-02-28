package main.IanSloat.thiccbot.commands;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.overrides.DefaultAudioPlayerManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public abstract class Command {

	public final static Map<IGuild, PermissionsManager> permManagers = new HashMap<>();
	public final static Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
	public final static AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
	static final Logger logger = LoggerFactory.getLogger(Command.class);
	
	public static synchronized GuildMusicManager getGuildAudioPlayer(IGuild guild, IChannel channel) {
		long guildId = guild.getLongID();
		GuildMusicManager musicManager = musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager, channel);
			musicManagers.put(guildId, musicManager);
		} else {
			musicManager.setPlayingMessageChannel(channel);
		}

		guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

		return musicManager;
	}
	
	public static synchronized PermissionsManager getPermissionsManager(IGuild guild) {
		PermissionsManager permMgr = permManagers.get(guild);

		if (permMgr == null) {
			permMgr = new PermissionsManager(guild);
			permManagers.put(guild, permMgr);
		}

		return permMgr;
	}
	
	public abstract boolean CheckUsagePermission(IUser user, PermissionsManager permMgr);
	
	public abstract boolean CheckForCommandMatch(IMessage command);
	
	public abstract void execute(MessageReceivedEvent event) throws NoMatchException;
	
}
