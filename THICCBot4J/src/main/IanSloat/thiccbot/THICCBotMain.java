package main.IanSloat.thiccbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import main.IanSloat.thiccbot.tools.GeoLocator;
import main.IanSloat.thiccbot.tools.WolframController;
import sx.blah.discord.api.*;

public class THICCBotMain {

	static String questionIDs[] = { "what", "how", "why", "when", "who", "where" };
	static String waAppID;
	
	public static void main(String[] args) {

		File logConfig = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "logging" + BotUtils.PATH_SEPARATOR + "log4j.properties");
		File logDir = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "logging");
		if(!(logConfig.exists())) {
			try {
				if(!(logDir.exists())) {
					logDir.mkdirs();
				}
				logConfig.createNewFile();
				FileWriter fileWriter = new FileWriter(logConfig.getAbsolutePath());
				fileWriter.write("log4j.rootLogger=INFO, STDOUT\n");
				fileWriter.write("log4j.logger.deng=ERROR\n");
				fileWriter.write("log4j.appender.STDOUT=org.apache.log4j.ConsoleAppender\n");
				fileWriter.write("log4j.appender.STDOUT.layout=org.apache.log4j.PatternLayout\n");
				fileWriter.write("log4j.appender.STDOUT.layout.ConversionPattern=%d{HH:mm:ss.SSS} [%p][%t][%c:%M] - %m%n\n");
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String log4JPropertyFile = logConfig.getAbsolutePath();
		Properties p = new Properties();
		
		try {
		    p.load(new FileInputStream(log4JPropertyFile));
		    PropertyConfigurator.configure(p);
		} catch (IOException e) {
		    //DAMN! I'm not....

		}
		
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
