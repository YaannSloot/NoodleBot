package main.IanSloat.thiccbot;
import sx.blah.discord.api.*;


public class THICCBotMain {

	static String questionIDs[] = { "what", "how", "why", "when", "who", "where" };
	static String waAppID;
	
	public static void main(String[] args) {
		
		String token = args[0];
		
		waAppID = args[1];
		
		IDiscordClient client;
		
		client = BotUtils.getBuiltDiscordClient(token);
		
		client.getDispatcher().registerListener(new Events());
		
		client.login();
	
	}

}
