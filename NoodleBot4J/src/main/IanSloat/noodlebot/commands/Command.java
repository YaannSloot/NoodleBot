package main.IanSloat.noodlebot.commands;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import main.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import main.IanSloat.noodlebot.overrides.DefaultAudioPlayerManager;
import main.IanSloat.noodlebot.wrappers.GuildPermissionsWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

// TODO Document this class
public abstract class Command {

	public final static Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
	public final static AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
	static final Logger logger = LoggerFactory.getLogger(Command.class);
	
	// Known command categories
	public final static String CATEGORY_PLAYER = "player";
	public final static String CATEGORY_MANAGEMENT = "management";
	public final static String CATEGORY_UTILITY = "utility";
	public final static String CATEGORY_MISC = "misc";
	
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
	
	public abstract boolean CheckUsagePermission(Member user, GuildPermissionsWrapper PermObject);
	
	public abstract boolean CheckForCommandMatch(Message command);
	
	public abstract void execute(MessageReceivedEvent event) throws NoMatchException;
	
	public abstract String getHelpSnippet();
	
	public abstract String getCommandId();
	
	public abstract String getCommandCategory();
	
}
