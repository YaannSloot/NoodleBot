package main.IanSloat.thiccbot.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.BotUtils;

public class RunScriptGenerator {

	private static final Logger logger = LoggerFactory.getLogger(RunScriptGenerator.class);

	public RunScriptGenerator() {

	}

	public void generate() {
		File script;
		boolean makeBat = false;
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			script = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "startbot.bat");
			makeBat = true;
		} else {
			script = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "startbot.sh");
		}
		if (!(script.exists())) {
			try {
				script.createNewFile();
				script.setExecutable(true);
				try {
					FileWriter fileWriter = new FileWriter(script);
					if (makeBat == true) {
						fileWriter.write("cls\r\n@echo off\r\necho Starting bot...\r\njava -jar thiccbot.jar");
					} else {
						fileWriter.write("#!/bin/bash\r\necho \"Starting bot...\"\r\njava -jar thiccbot.jar");
					}
					fileWriter.close();
					System.exit(0);
				} catch (IOException e) {
					System.exit(0);
				}
			} catch (IOException e) {
				System.exit(0);
			}
		} else {
			logger.info("Run script already exists so no new files were created");
		}
	}

}
