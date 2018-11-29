package main.IanSloat.thiccbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.PropertyConfigurator;

import sx.blah.discord.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.tools.GeoLocator;
import main.IanSloat.thiccbot.tools.MainConfigEditor;
import main.IanSloat.thiccbot.tools.RunScriptGenerator;

public class ThiccBotMain {

	static String questionIDs[] = { "what", "how", "why", "when", "who", "where" };
	static String waAppID;
	private static final Logger logger = LoggerFactory.getLogger(ThiccBotMain.class);
	static GeoLocator locator;

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
					FileWriter fileWriter = new FileWriter(logConfig);
					fileWriter.write("log4j.rootLogger=INFO, STDOUT\r\n");
					fileWriter.write("log4j.logger.deng=ERROR\r\n");
					fileWriter.write("log4j.appender.STDOUT=org.apache.log4j.ConsoleAppender\r\n");
					fileWriter.write("log4j.appender.STDOUT.layout=org.apache.log4j.PatternLayout\r\n");
					fileWriter.write(
							"log4j.appender.STDOUT.layout.ConversionPattern=%d{HH:mm:ss.SSS} [%p][%t][%c:%M] - %m%n\r\n");
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
		
		RunScriptGenerator scriptGen = new RunScriptGenerator();
		scriptGen.generate();
		
		MainConfigEditor cfgEdit = new MainConfigEditor();
		
		String token = cfgEdit.getToken();

		waAppID = cfgEdit.getAppID();
		
		locator = new GeoLocator(cfgEdit.getIP());

		IDiscordClient client;

		client = BotUtils.getBuiltDiscordClient(token);
		
		client.getDispatcher().registerListener(new Events());

		client.login();

		Scanner readLine = new Scanner(System.in);
		
		String command = "";
		
		while(!(command.equals("shutdown"))) {
			try {
			command = readLine.nextLine();
			} catch (java.util.NoSuchElementException e) {}
			
		}
		
		logger.info("Bot is shutting down...");
		
		readLine.close();
		
		System.exit(0);
		
	}

}
