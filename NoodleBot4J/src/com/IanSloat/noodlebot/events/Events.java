package com.IanSloat.noodlebot.events;

import java.io.IOException;

import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;
import com.IanSloat.noodlebot.controllers.settings.GuildSettingsController;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
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
	
	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		try {
			GuildSettingsController.initGuildSettingsFiles(event.getGuild());
			GuildPermissionsController.initGuildPermissionsFiles(event.getGuild());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
