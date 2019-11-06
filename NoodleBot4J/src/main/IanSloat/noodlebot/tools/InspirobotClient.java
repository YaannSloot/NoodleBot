package main.IanSloat.noodlebot.tools;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO Document class
public class InspirobotClient {

	private static final Logger logger = LoggerFactory.getLogger(InspirobotClient.class);

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
