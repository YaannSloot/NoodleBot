package com.IanSloat.noodlebot.commands;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import com.IanSloat.noodlebot.overrides.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

// TODO Document this class
public abstract class Command {

	public final static Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
	public final static AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
	static final Logger logger = LoggerFactory.getLogger(Command.class);
	
	// Known command categories
	public enum CommandCategory{
		GENERAL("general"),
		PLAYER("player"),
		MANAGEMENT("management"),
		UTILITY("utility"),
		MISC("misc");
		
		private String id;
		
		CommandCategory(String id){
			this.id = id;
		}
		
		public String toString() {
			return id;
		}
		
	}
	
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
	
	public abstract boolean CheckUsagePermission(Member user);
	
	public abstract boolean CheckForCommandMatch(Message command);
	
	public abstract void execute(MessageReceivedEvent event) throws NoMatchException;
	
	public abstract String getHelpSnippet();
	
	public abstract String getCommandId();
	
	public abstract CommandCategory getCommandCategory();
	
	public abstract MessageEmbed getCommandHelpPage();
	
}
