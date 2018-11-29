package main.IanSloat.thiccbot.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.BotUtils;

public class MainConfigEditor {

	private File configFile = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "settings"
			+ BotUtils.PATH_SEPARATOR + "settings.cfg");
	private File configDir = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "settings");
	private static final Logger logger = LoggerFactory.getLogger(MainConfigEditor.class);

	public MainConfigEditor() {
		if (!(configDir.exists())) {
			configDir.mkdirs();
			logger.info("Bot settings directory not found. A new settings directory was created at "
					+ configDir.getAbsolutePath());
		}
		if (!(configFile.exists())) {
			logger.info("Bot settings file not found. Creating new file...");
			try {
				configFile.createNewFile();
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
				token = "token=" + token;
				appID = "appid=" + appID;
				ip = "ip=" + ip;
				try {
					FileWriter fileWriter = new FileWriter(configFile);
					fileWriter.write(token + '\n');
					fileWriter.write(appID + '\n');
					fileWriter.write(ip);
					fileWriter.close();
					System.out.println("Done.");
				} catch (IOException e) {
					logger.error("Unable to write to settings file. Bot is exiting...");
					System.exit(0);
				}
			} catch (IOException e) {
				logger.error("Unable to create settings file. Bot is exiting...");
				System.exit(0);
			}
		}
	}

	private ArrayList<String> getConfigFileLines() {
		ArrayList<String> lines = new ArrayList<String>();
		try {
			FileReader fileReader = new FileReader(configFile);
			int ch;
			String line = "";
			while ((ch = fileReader.read()) != -1) {
				if ((char) ch == '\n') {
					lines.add(line);
					line = "";
				} else {
					line += (char) ch;
				}
			}
			if (line.length() > 0) {
				lines.add(line);
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			logger.error("Could find config file");
		} catch (IOException e) {
			logger.error("Could not read token from config file");
		}
		return lines;
	}
	
	private void writeSettings(ArrayList<String> settings) {
		try {
			configFile.delete();
			configFile.createNewFile();
			FileWriter fileWriter = new FileWriter(configFile);
			for(String line : settings) {
				fileWriter.write(line + '\n');
			}
			fileWriter.close();
		} catch (IOException e) {
			logger.error("Unable to write to config file");
		}
	}

	public String getToken() {
		String token = null;
		for(String line : getConfigFileLines()) {
			if(line.contains("token=")) {
				token = line.replace("token=", "");
			}
		}
		return token;
	}
	
	public String getAppID() {
		String appID = null;
		for(String line : getConfigFileLines()) {
			if(line.contains("appid=")) {
				appID = line.replace("appid=", "");
			}
		}
		return appID;
	}
	
	public String getIP() {
		String ip = null;
		for(String line : getConfigFileLines()) {
			if(line.contains("ip=")) {
				ip = line.replace("ip=", "");
			}
		}
		return ip;
	}
	
	public void setToken(String newToken) {
		ArrayList<String> settings = getConfigFileLines();
		for(int i = 0; i < settings.size(); i++) {
			if(settings.get(i).contains("token=")) {
				settings.remove(i);
				settings.add(i, "token=" + newToken);
				break;
			}
		}
		writeSettings(settings);
	}
	
	public void setAppID(String newAppID) {
		ArrayList<String> settings = getConfigFileLines();
		for(int i = 0; i < settings.size(); i++) {
			if(settings.get(i).contains("appid=")) {
				settings.remove(i);
				settings.add(i, "appid=" + newAppID);
				break;
			}
		}
		writeSettings(settings);
	}
	
	public void setIP(String newIP) {
		ArrayList<String> settings = getConfigFileLines();
		for(int i = 0; i < settings.size(); i++) {
			if(settings.get(i).contains("ip=")) {
				settings.remove(i);
				settings.add(i, "ip=" + newIP);
				break;
			}
		}
		writeSettings(settings);
	}

	public void printCfgFile() {
		for(String line : getConfigFileLines()) {
			System.out.println(": " + line);
		}
	}
	
}
