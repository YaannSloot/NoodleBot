package com.IanSloat.noodlebot.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// TODO Document methods in YTCrawler class
public class YTCrawler {

	public static void main(String[] args) {
		YTCrawler crawler = new YTCrawler("https://www.youtube.com/watch?v=6XUOo-2gSu4");
		List<String> videos = crawler.generatePlaylist(19);
		
		for(String url : videos) {
			System.out.println(url);
		}
		
	}

	private String startUrl;
	private final String baseUrl = "https://www.youtube.com/watch?v=";
	
	public YTCrawler(String startUrl) {
		this.startUrl = startUrl;
	}
	
	public List<String> generatePlaylist(int amount) {
		List<String> result = new ArrayList<String>();
		result.add(getNextAutoplayUrl(startUrl));
		System.out.println("Retrieved URL 1...");
		String currentUrl = result.get(0);
		for(int i = 1; i < amount; i++) {
			currentUrl = getNextAutoplayUrl(currentUrl);
			result.add(currentUrl);
			System.out.println("Retrieved URL "  + (1 + i) + "...");
		}
		return result;
	}

	public String getNextAutoplayUrl(String url) {
		String result = "";
		try {
			Document doc = Jsoup.connect(url).get();
			Elements docElements = doc.getElementsByTag("script");
			String docContents = "";
			for(Element e : docElements) {
				if(e.data().contains("autoplayVideo")) {
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
	
}
