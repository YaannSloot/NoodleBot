package com.IanSloat.noodlebot.events;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * A mapping of every JDA event to be processed during runtime
 */
public class Events extends ListenerAdapter {
	
	private Login loginEvent = new Login();
	private CommandController commandController = new CommandController();
	private VoiceHeartbeatListener voiceActivityListener = new VoiceHeartbeatListener();
	
	@Override
	public void onReady(ReadyEvent event) {
		loginEvent.BotLoginEvent(event);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		commandController.CommandEvent(event);
	}
	
	@Override
	public void onGenericGuildVoice(GenericGuildVoiceEvent event) {
		voiceActivityListener.VoiceHeartbeatEvent(event);
	}
	
}
