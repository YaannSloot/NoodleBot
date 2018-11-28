package main.IanSloat.thiccbot;

import main.IanSloat.thiccbot.tools.GeoLocator;
import main.IanSloat.thiccbot.tools.WolframController;
import sx.blah.discord.api.*;

public class THICCBotMain {

	static String questionIDs[] = { "what", "how", "why", "when", "who", "where" };
	static String waAppID;

	public static void main(String[] args) {

		String token = args[0];

		waAppID = args[1];

		GeoLocator locator = new GeoLocator(new WolframController(waAppID).getServerIP());
		System.out.println(locator.getIPAddress());
		System.out.println(locator.getCity() + ',' + locator.getRegion() + ',' + locator.getCountry());

		IDiscordClient client;
		

		client = BotUtils.getBuiltDiscordClient(token);

		client.getDispatcher().registerListener(new Events());

		client.login();

	}

}
