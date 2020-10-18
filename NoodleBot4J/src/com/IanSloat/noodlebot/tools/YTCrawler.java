package com.IanSloat.noodlebot.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Experimental class used for generating better autoplay playlists from youtube
 * when using the music player. Currently has no use.
 */
public class YTCrawler {

	private String startUrl;
	private final String baseUrl = "https://www.youtube.com/watch?v=";

	public YTCrawler(String startUrl) {
		this.startUrl = startUrl;
	}

	public List<String> generatePlaylist(int amount) {
		List<String> result = new ArrayList<String>();
		result.add(getNextAutoplayUrl(startUrl));
		String currentUrl = result.get(0);
		for (int i = 1; i < amount; i++) {
			currentUrl = getNextAutoplayUrl(currentUrl);
			result.add(currentUrl);
		}
		return result;
	}

	public String getNextAutoplayUrl(String url) {
		String result = "";
		try {
			Document doc = Jsoup.connect(url).get();
			Elements docElements = doc.getElementsByTag("script");
			String docContents = "";
			for (Element e : docElements) {
				if (e.data().contains("autoplayVideo")) {
					docContents = e.data();
					break;
				}
			}
			String videoID = "";
			int targetIndex = docContents.indexOf("autoplayVideo");
			targetIndex = docContents.indexOf("videoId", targetIndex);
			videoID = docContents.substring(targetIndex, docContents.indexOf(',', targetIndex));
			videoID = videoID.replace("\"", "");
			videoID = videoID.replace("\\", "");
			videoID = videoID.replace("videoId:", "");
			result = baseUrl + videoID;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	//public void setVideoUrl()
	
	public List<String> doSearch(String search) {
		List<String> result = new ArrayList<String>();
		
		return result;
	}

}
