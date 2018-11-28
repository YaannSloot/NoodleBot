package main.IanSloat.thiccbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import sx.blah.discord.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class THICCBotMain {

	static String questionIDs[] = { "what", "how", "why", "when", "who", "where" };
	static String waAppID;
	private static final Logger logger = LoggerFactory.getLogger(THICCBotMain.class);

	public static void main(String[] args) {

		File logConfig = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "logging"
				+ BotUtils.PATH_SEPARATOR + "log4j.properties");
		File logDir = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "logging");
		if (!(logConfig.exists())) {
			try {
				System.out.println("No log4j.properties file found. Creating new log4j.properties file");
				if (!(logDir.exists())) {
					logDir.mkdirs();
					System.out.println("Created " + logDir.getAbsolutePath() + " directory successfully");
				}
				logConfig.createNewFile();
				System.out.println("Created log4j.properties file successfully");
				try {
					FileWriter fileWriter = new FileWriter(logConfig.getAbsolutePath());
					fileWriter.write("log4j.rootLogger=INFO, STDOUT\n");
					fileWriter.write("log4j.logger.deng=ERROR\n");
					fileWriter.write("log4j.appender.STDOUT=org.apache.log4j.ConsoleAppender\n");
					fileWriter.write("log4j.appender.STDOUT.layout=org.apache.log4j.PatternLayout\n");
					fileWriter.write(
							"log4j.appender.STDOUT.layout.ConversionPattern=%d{HH:mm:ss.SSS} [%p][%t][%c:%M] - %m%n\n");
					fileWriter.close();
					System.out.println("Wrote default settings to log4j.properties file successfully");
				} catch (IOException e) {
					System.out.println("ERROR: Could not write to log4j.properties file");
				}
			} catch (IOException e) {
				System.out.println("ERROR: Could not create log4j.properties file");
			}
		}
		String log4JPropertyFile = logConfig.getAbsolutePath();
		Properties p = new Properties();

		try {
			System.out.println("Loading log4j.properties file");
			p.load(new FileInputStream(log4JPropertyFile));
			PropertyConfigurator.configure(p);
			logger.info("Successfully loaded log4j.properties file");
		} catch (IOException e) {
			System.out.println("ERROR: Could not set " + logConfig.getAbsolutePath() + " as properties file");
		}

		String token = args[0];

		waAppID = args[1];

		IDiscordClient client;

		client = BotUtils.getBuiltDiscordClient(token);

		client.getDispatcher().registerListener(new Events());

		client.login();

	}

}
