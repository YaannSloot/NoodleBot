package main.IanSloat.noodlebot.events;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Events extends ListenerAdapter {
	
	private Login loginEvent = new Login();
	
	@Override
	public void onReady(ReadyEvent event) {
		loginEvent.BotLoginEvent(event);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		System.out.println(event.getMessage().getContentRaw());
	}
	
}
