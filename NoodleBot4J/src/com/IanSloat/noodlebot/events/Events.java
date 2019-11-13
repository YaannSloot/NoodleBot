package com.IanSloat.noodlebot.events;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

// TODO Finish writing events
public class Events extends ListenerAdapter {
	
	private Login loginEvent = new Login();
	private CommandController commandController = new CommandController();
	
	@Override
	public void onReady(ReadyEvent event) {
		loginEvent.BotLoginEvent(event);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		commandController.CommandEvent(event);
	}
	
}
