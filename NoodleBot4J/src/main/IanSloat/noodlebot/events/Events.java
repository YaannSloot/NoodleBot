package main.IanSloat.noodlebot.events;

import java.util.ArrayList;
import java.util.List;

import main.IanSloat.noodlebot.services.LoggerService;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Events extends ListenerAdapter{

	static List<String> knownGuildIds = new ArrayList<String>();
	private Login loginEvent = new Login();
	private GuildJoin guildJoinEvent = new GuildJoin();
	private UserLeftVoice userLeftVoiceEvent = new UserLeftVoice();
	private GuildLeave guildLeaveEvent = new GuildLeave();
	private UserMovedOutOfVoice userMovedOutOfVoiceEvent = new UserMovedOutOfVoice();
	private CommandHandler commandHandler = new CommandHandler();
	private Disconnect disconnectEvent = new Disconnect();
	private Reconnected reconnectEvent = new Reconnected();
	private Resumed resumedEvent = new Resumed();
	private LoggerService loggerService = new LoggerService();

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		commandHandler.MessageReceivedEvent(event);
	}

	@Override
	public void onReady(ReadyEvent event) {
		loginEvent.BotLoginEvent(event);
	}

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		guildJoinEvent.GuildJoinEvent(event);
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		guildLeaveEvent.GuildLeaveEvent(event);
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		userLeftVoiceEvent.UserLeftVoiceEvent(event);
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		userMovedOutOfVoiceEvent.UserMovedOutOfVoiceEvent(event);
	}
	
	@Override
	public void onDisconnect(DisconnectEvent event) {
		disconnectEvent.DisconnectEvent(event);
	}
	
	@Override
	public void onReconnect(ReconnectedEvent event) {
		reconnectEvent.ReconnectEvent(event);
	}
	
	@Override 
	public void onResume(ResumedEvent event){
		resumedEvent.ResumedEvent(event);
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		
	}
	
	// Event forwarding
	@Override
	public void onGenericEvent(GenericEvent event) {
		loggerService.logEvent(event);
	}
	
	/*
	@Override
	public void onGenericMessageReaction(GenericMessageReactionEvent event) {
		String emoji = "";
		try {
			emoji = event.getReaction().getReactionEmote().getAsCodepoints();
		} catch (IllegalStateException e){
			emoji = event.getReaction().getReactionEmote().getName();
		}
		System.out.println(emoji);
	}*/
}
