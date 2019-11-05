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
import org.discordbots.api.client.DiscordBotListAPI;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;

import main.IanSloat.noodlebot.events.Events;
import main.IanSloat.noodlebot.gateway.GatewayServer;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class NoodleBotMain {

	public static String questionIDs[] = { "what", "how", "why", "when", "who", "where", "simplify" };
	public static String waAppID;
	private static final Logger logger = LoggerFactory.getLogger(NoodleBotMain.class);
	public static WebSocketServer server;
	public static String versionNumber = "2.0.0";
	public static String botVersion = "noodlebot-v" + versionNumber + "_BETA";
	public static String devMsg = "Working as expected";
	public static User botOwner;
	public static ShardManager shardmgr;
	public static DiscordBotListAPI dblEndpoint = null;
	public static EventListener eventListener = new Events();

	// Value Overrides
	public static final int playerVolumeLimit = 2147483647;

	// Console
	public static Terminal terminal;

	// Line Reader
	public static LineReader lineReader;

	public static void main(String[] args) {

		try {

			terminal = TerminalBuilder.terminal();

			lineReader = LineReaderBuilder.builder().terminal(terminal).build();

			class ModifiedPrintStream extends PrintStream {

				public ModifiedPrintStream(OutputStream out) {
					super(out, true);
					// TODO Auto-generated constructor stub
				}

				@Override
				public void write(int b) {
					lineReader.printAbove("" + (char) b);
				}

				@Override
				public void write(byte[] b, int off, int len) {
					if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
						throw new IndexOutOfBoundsException();

					String output = "";

					for (int i = 0; i < len; i++) {
						output += (char) b[off + i];
					}

					lineReader.printAbove(output);
				}

				@Override
				public void write(byte[] b) throws IOException {
					String output = "";
					for (byte bt : b) {
						output += (char) bt;
					}
					lineReader.printAbove(output);
				}

			}

			PrintStream originalStream = System.out;

			System.setOut(new ModifiedPrintStream(originalStream));

			System.out.println("\n   _  ______  ____  ___  __   _______  ____  ______\n"
					+ "  / |/ / __ \\/ __ \\/ _ \\/ /  / __/ _ )/ __ \\/_  __/\n"
					+ " /    / /_/ / /_/ / // / /__/ _// _  / /_/ / / /   \n"
					+ "/_/|_/\\____/\\____/____/____/___/____/\\____/ /_/ \n\n"
					+ "    ____  _______________            ___ \n" + "   / __ )/ ____/_  __/   |     _   _|__ \\\n"
					+ "  / __  / __/   / / / /| |    | | / /_/ /\n" + " / /_/ / /___  / / / ___ |    | |/ / __/ \n"
					+ "/_____/_____/ /_/ /_/  |_|    |___/____/ \n" + "                                         ");
			
			System.out.println("Running core file check...");

			File logConfig = new File("logging/log4j.properties");
			File logDir = new File("logging");
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

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
