package main.IanSloat.thiccbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.apache.log4j.PropertyConfigurator;
import org.java_websocket.server.WebSocketServer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.events.Events;
import main.IanSloat.thiccbot.guiclientserver.ClientServer;
import main.IanSloat.thiccbot.tools.GeoLocator;
import main.IanSloat.thiccbot.tools.RunScriptGenerator;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;

public class ThiccBotMain {

	public static String questionIDs[] = { "what", "how", "why", "when", "who", "where", "simplify" };
	public static String waAppID;
	private static final Logger logger = LoggerFactory.getLogger(ThiccBotMain.class);
	public static GeoLocator locator;
	public static WebSocketServer server;
	public static String versionNumber = "1.0.2";
	public static String botVersion = "thiccbot-v" + versionNumber + "_BETA";
	public static String devMsg = "NOW IN BETA!";
	private static File configFile = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "settings"
			+ BotUtils.PATH_SEPARATOR + "settings.bot");
	private static File configDir = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "settings");
	public static User botOwner;
	public static JDA client;
	
	//Value Overrides
	public static final int playerVolumeLimit =  2147483647;
	
	//Console
	public static Terminal terminal;
	
	//Line Reader
	public static LineReader lineReader;
	
	public static void main(String[] args) {

		try {
			
			terminal = TerminalBuilder.terminal();
			
			lineReader = LineReaderBuilder.builder()
					.terminal(terminal)
					.build();
			
			class ModifiedPrintStream extends PrintStream {

				public ModifiedPrintStream(OutputStream out) {
					super(out, true);
					// TODO Auto-generated constructor stub
				}
				
				@Override
				public void write(int b) {
					lineReader.printAbove("" + (char)b);
				}
				
				@Override
				public void write(byte[] b, int off, int len) {
					if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
			            throw new IndexOutOfBoundsException();
					
					String output = "";
					
					for (int i = 0 ; i < len ; i++) {
						output += (char)b[off + i];
					}
					
					lineReader.printAbove(output);
				}
				
				@Override
				public void write(byte[] b) throws IOException {
					String output = "";
					for(byte bt : b) {
						output += (char)bt;
					}
					lineReader.printAbove(output);
				}
				
			}
			
			PrintStream originalStream = System.out;
			
			System.setOut(new ModifiedPrintStream(originalStream));
			
			System.out.println("\n" + 
					"      ___           ___                       ___           ___           ___           ___           ___     \n" + 
					"     /\\  \\         /\\__\\          ___        /\\  \\         /\\  \\         /\\  \\         /\\  \\         /\\  \\    \n" + 
					"     \\:\\  \\       /:/  /         /\\  \\      /::\\  \\       /::\\  \\       /::\\  \\       /::\\  \\        \\:\\  \\   \n" + 
					"      \\:\\  \\     /:/__/          \\:\\  \\    /:/\\:\\  \\     /:/\\:\\  \\     /:/\\:\\  \\     /:/\\:\\  \\        \\:\\  \\  \n" + 
					"      /::\\  \\   /::\\  \\ ___      /::\\__\\  /:/  \\:\\  \\   /:/  \\:\\  \\   /::\\~\\:\\__\\   /:/  \\:\\  \\       /::\\  \\ \n" + 
					"     /:/\\:\\__\\ /:/\\:\\  /\\__\\  __/:/\\/__/ /:/__/ \\:\\__\\ /:/__/ \\:\\__\\ /:/\\:\\ \\:|__| /:/__/ \\:\\__\\     /:/\\:\\__\\\n" + 
					"    /:/  \\/__/ \\/__\\:\\/:/  / /\\/:/  /    \\:\\  \\  \\/__/ \\:\\  \\  \\/__/ \\:\\~\\:\\/:/  / \\:\\  \\ /:/  /    /:/  \\/__/\n" + 
					"   /:/  /           \\::/  /  \\::/__/      \\:\\  \\        \\:\\  \\        \\:\\ \\::/  /   \\:\\  /:/  /    /:/  /     \n" + 
					"   \\/__/            /:/  /    \\:\\__\\       \\:\\  \\        \\:\\  \\        \\:\\/:/  /     \\:\\/:/  /     \\/__/      \n" + 
					"                   /:/  /      \\/__/        \\:\\__\\        \\:\\__\\        \\::/__/       \\::/  /                 \n" + 
					"                   \\/__/                     \\/__/         \\/__/         ~~            \\/__/                  \n" + 
					"\n" + 
					"    ____  _______________                ___   \n" + 
					"   / __ )/ ____/_  __/   |     _   __   <  /   \n" + 
					"  / __  / __/   / / / /| |    | | / /   / /    \n" + 
					" / /_/ / /___  / / / ___ |    | |/ /   / /     \n" + 
					"/_____/_____/ /_/ /_/  |_|    |___/   /_/      \n" + 
					"                                               \n" + 
					"");
			
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
				logger.info("Bot settings file created successfully. Starting bot setup wizard...");
				System.out.println("\n\n\n\nWelcome to the ThiccBot setup wizard\n"
						+ "Before you start using your bot, you will have to provide a few details\n\n"
						+ "Please input your bots token");
				String token = lineReader.readLine(">");
				System.out.println("\nPlease input you WolframAlpha API AppID\n"
						+ "For more information on obtaining an AppID, please visit this link:\n"
						+ "https://products.wolframalpha.com/api/documentation/#obtaining-an-appid");
				String appID = lineReader.readLine(">");
				System.out.println("\nPlease input this machines public ip.\n"
						+ "You can find this out by searching \"what is my ip\" in google.\n"
						+ "If you don't want this server's location to be shown on the info command,\n"
						+ "just leave the line blank and hit enter");
				String ip = lineReader.readLine(">");
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

			client = new JDABuilder(setMgr.getFirstInValGroup("TOKEN"))
					.useSharding(0, 1)
					.build();
			
			client.addEventListener(new Events());
			
			botOwner = client.retrieveApplicationInfo().complete().getOwner();
			
			server = new ClientServer(new InetSocketAddress("0.0.0.0", 443), client);
			
			server.run();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
