package main.IanSloat.thiccbot.events;

import sx.blah.discord.api.events.EventSubscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.overrides.DefaultAudioPlayerManager;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.impl.events.shard.LoginEvent;

public class Events {

	final static AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
	final static Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
	static List<String> knownGuildIds = new ArrayList<String>();
	private Login loginEvent = new Login();
	private GuildJoin guildJoinEvent = new GuildJoin();
	private UserLeftVoice userLeftVoiceEvent = new UserLeftVoice();
	private GuildLeave guildLeaveEvent = new GuildLeave();
	private UserMovedOutOfVoice userMovedOutOfVoiceEvent = new UserMovedOutOfVoice();
	private CommandHandler commandHandler = new CommandHandler();

	@EventSubscriber
	public void MessageReceivedEvent(MessageReceivedEvent event) {
		commandHandler.MessageReceivedEvent(event);
	}

	@EventSubscriber
	public void BotLoginEvent(LoginEvent event) {
		loginEvent.BotLoginEvent(event);
	}

	@EventSubscriber
	public void GuildJoinEvent(GuildCreateEvent event) {
		guildJoinEvent.GuildJoinEvent(event);
	}

	@EventSubscriber
	public void GuildLeaveEvent(GuildLeaveEvent event) {
		guildLeaveEvent.GuildLeaveEvent(event);
	}

	@EventSubscriber
	public void UserLeftVoiceEvent(UserVoiceChannelLeaveEvent event) {
		userLeftVoiceEvent.UserLeftVoiceEvent(event);
	}

	@EventSubscriber
	public void UserMovedOutOfVoiceEvent(UserVoiceChannelMoveEvent event) {
		userMovedOutOfVoiceEvent.UserMovedOutOfVoiceEvent(event);
	}
}
