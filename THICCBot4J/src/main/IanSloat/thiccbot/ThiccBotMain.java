package main.IanSloat.thiccbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.PropertyConfigurator;
import org.java_websocket.server.WebSocketServer;

import sx.blah.discord.api.*;
import sx.blah.discord.handle.obj.IUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.events.Events;
import main.IanSloat.thiccbot.guiclientserver.ClientServer;
import main.IanSloat.thiccbot.tools.GeoLocator;
import main.IanSloat.thiccbot.tools.RunScriptGenerator;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;

public class ThiccBotMain {

	public static String questionIDs[] = { "what", "how", "why", "when", "who", "where" };
	public static String waAppID;
	private static final Logger logger = LoggerFactory.getLogger(ThiccBotMain.class);
	public static GeoLocator locator;
	public static WebSocketServer server;
	public static String botVersion = "thiccbot-v0.8alpha";
	public static String devMsg = "Getting close to v1.0 alpha, just not quite there yet";
	private static File configFile = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "settings"
			+ BotUtils.PATH_SEPARATOR + "settings.bot");
	private static File configDir = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "settings");
	public static IUser botOwner;
	public static IDiscordClient client;
	
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
		
		TBMLSettingsParser setMgr;
		
		if (!(configDir.exists())) {
			configDir.mkdirs();
			logger.info("Bot settings directory not found. A new settings directory was created at "
					+ configDir.getAbsolutePath());
		}
		if (!(configFile.exists())) {
			logger.info("Bot settings file not found. Creating new file...");
			setMgr = new TBMLSettingsParser(configFile);
			Scanner readLine = new Scanner(System.in);
			logger.info("Bot settings file created successfully. Starting bot setup wizard...");
			System.out.println("\n\n\n\nWelcome to the ThiccBot setup wizard\n"
					+ "Before you start using your bot, you will have to provide a few details\n\n"
					+ "Please input your bots token");
			System.out.print(">");
			String token = readLine.nextLine();
			System.out.println("\nPlease input you WolframAlpha API AppID\n"
					+ "For more information on obtaining an AppID, please visit this link:\n"
					+ "https://products.wolframalpha.com/api/documentation/#obtaining-an-appid");
			System.out.print(">");
			String appID = readLine.nextLine();
			System.out.println("\nPlease input this machines public ip.\n"
					+ "You can find this out by searching \"what is my ip\" in google.\n"
					+ "If you don't want this server's location to be shown on the info command,\n"
					+ "just leave the line blank and hit enter");
			System.out.print(">");
			String ip = readLine.nextLine();
			readLine.close();
			System.out.println("\nWriting settings to config file...");
			setMgr.addObj("StartupItems");
			setMgr.setScope("StartupItems");
			setMgr.addVal("TOKEN", token);
			setMgr.addVal("APPID", appID);
			setMgr.addVal("IP", ip);
			System.out.println("Done.");
		}
		
		setMgr = new TBMLSettingsParser(configFile);
		
		setMgr.setScope(TBMLSettingsParser.DOCROOT);
		
		setMgr.setScope("StartupItems");
		
		waAppID = setMgr.getFirstInValGroup("APPID");
		
		locator = new GeoLocator(setMgr.getFirstInValGroup("IP"));

		client = BotUtils.getBuiltDiscordClient(setMgr.getFirstInValGroup("TOKEN"));
		
		client.getDispatcher().registerListener(new Events());

		client.login();
		
		botOwner = client.getApplicationOwner();
		
		server = new ClientServer(new InetSocketAddress("0.0.0.0", 443), client);
		
		server.run();
		
		class inputThread implements Runnable {

			@Override
			public void run() {
				String command = "";
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				while(!(command.equals("shutdown"))) {
					try {
						command = br.readLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				logger.info("Bot is shutting down...");
				
				System.exit(0);
				
			}
			
		}
		
		Thread commandReader = new Thread(new inputThread());
		
		commandReader.start();
		
	}
	
}
