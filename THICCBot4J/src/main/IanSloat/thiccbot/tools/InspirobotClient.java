package main.IanSloat.thiccbot.tools;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InspirobotClient {

	private static final Logger logger = LoggerFactory.getLogger(InspirobotClient.class);

	private String sessionID = "";

	public InspirobotClient() {
		try {
			Document doc;
			doc = Jsoup.connect("https://inspirobot.me/api?getSessionID=1").get();
			Elements tags = doc.getElementsByTag("body");
			sessionID = tags.get(0).text();
			logger.info("A new InspiroBot client initialized successfully");
		} catch (IOException e) {
			logger.error("Could not initialize new InspiroBot client");
			e.printStackTrace();
		}
	}

	public String getNewImageUrl() {
		String response = "";
		try {
			Document doc;
			doc = Jsoup.connect("https://inspirobot.me/api?generate=true").get();
			Elements tags = doc.getElementsByTag("body");
			response = tags.get(0).text();
		} catch (IOException e) {
			logger.error("Could not get new InspiroBot image");
			e.printStackTrace();
		}
		return response;
	}

}
