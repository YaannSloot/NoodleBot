package main.IanSloat.noodlebot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.login.LoginException;

import org.apache.log4j.PropertyConfigurator;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.noodlebot.events.Events;
import main.IanSloat.noodlebot.gateway.GatewayServer;
import main.IanSloat.noodlebot.tools.RunScriptGenerator;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class NoodleBotMain {

	public static String questionIDs[] = { "what", "how", "why", "when", "who", "where", "simplify" };
	public static String waAppID;
	private static final Logger logger = LoggerFactory.getLogger(NoodleBotMain.class);
	public static WebSocketServer server;
	public static String versionNumber = "1.1.5";
	public static String botVersion = "noodlebot-v" + versionNumber + "_BETA";
	public static String devMsg = "NOW IN BETA!";
	private static File configFile = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "settings"
			+ BotUtils.PATH_SEPARATOR + "settings.bot");
	private static File configDir = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "settings");
	public static User botOwner;
	public static ShardManager shardmgr;
	
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
			
			System.out.println("\n   _  ______  ____  ___  __   _______  ____  ______\n" + 
					"  / |/ / __ \\/ __ \\/ _ \\/ /  / __/ _ )/ __ \\/_  __/\n" + 
					" /    / /_/ / /_/ / // / /__/ _// _  / /_/ / / /   \n" + 
					"/_/|_/\\____/\\____/____/____/___/____/\\____/ /_/ \n\n" + 
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
			
			NBMLSettingsParser setMgr;
			
			if (!(configDir.exists())) {
				configDir.mkdirs();
				logger.info("Bot settings directory not found. A new settings directory was created at "
						+ configDir.getAbsolutePath());
			}
			if (!(configFile.exists())) {
				logger.info("Bot settings file not found. Creating new file...");
				setMgr = new NBMLSettingsParser(configFile);
				logger.info("Bot settings file created successfully. Starting bot setup wizard...");
				System.out.println("\n\n\n\nWelcome to the NoodleBot setup wizard\n"
						+ "Before you start using your bot, you will have to provide a few details\n\n"
						+ "Please input your bots token");
				String token = lineReader.readLine(">");
				System.out.println("\nPlease input you WolframAlpha API AppID\n"
						+ "For more information on obtaining an AppID, please visit this link:\n"
						+ "https://products.wolframalpha.com/api/documentation/#obtaining-an-appid");
				String appID = lineReader.readLine(">");
				System.out.println("\nWriting settings to config file...");
				setMgr.addObj("StartupItems");
				setMgr.setScope("StartupItems");
				setMgr.addVal("TOKEN", token);
				setMgr.addVal("APPID", appID);
				System.out.println("Done.");
			}
			
			server = new GatewayServer(new InetSocketAddress("0.0.0.0", 8000), shardmgr);
			
			if(args.length > 0) {
				if(Arrays.asList(args).contains("useSSL")) {
					System.out.println("\n\nGateway is set to use SSL. Please input required passwords.\nInput the store password:");
					String sp = lineReader.readLine(">", '*');
					System.out.println("Input the key password");
					String kp = lineReader.readLine(">", '*');
					try {
						KeyStore ks = KeyStore.getInstance("JKS");
						File kf = new File("keystore.jks");
						if(!kf.exists()) {
							logger.error("Keystore file does not exist. Bot is shutting down...");
							System.exit(0);
						} else {
							try {
								ks.load(new FileInputStream(kf), sp.toCharArray());
								KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
								kmf.init(ks, kp.toCharArray());
								TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
								tmf.init(ks);
								SSLContext sslContext = SSLContext.getInstance("TLS");
								sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
								server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
							} catch (NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
								e.printStackTrace();
								System.exit(0);
							}
						}
					} catch (KeyStoreException e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
			}
			
			setMgr = new NBMLSettingsParser(configFile);
			
			setMgr.setScope(NBMLSettingsParser.DOCROOT);
			
			setMgr.setScope("StartupItems");
			
			waAppID = setMgr.getFirstInValGroup("APPID");

			logger.info("Loading shards...");
			
			shardmgr = new DefaultShardManagerBuilder(setMgr.getFirstInValGroup("TOKEN"))
					.setShardsTotal(-1)
					.addEventListeners(new Events())
					.build();
			
			botOwner = shardmgr.getShards().get(0).retrieveApplicationInfo().complete().getOwner();
			
			server.start();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
