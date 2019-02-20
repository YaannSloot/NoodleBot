package main.IanSloat.thiccbot.tools;

import java.io.IOException;

import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wikisearch {

	private static final Logger logger = LoggerFactory.getLogger(Wikisearch.class);
	private String title = "";
	private int pageId;
	private String imageUrl = "";
	private String summary = "";
	
	public static void main(String[] args) {
		Wikisearch search = new Wikisearch();
		search.search("trump");
	}
	
	public Wikisearch() {
		
	}
	
	public boolean search(String term) {
		boolean result = false;
		try {
			Document doc;
			doc = Jsoup.connect("https://en.wikipedia.org/w/api.php?action=query&srsearch=" + term + "&srprop&list=search&format=xml")
					.ignoreContentType(true)
					.get();
			System.out.println(doc.toString());
			JSONObject parsedResult = XML.toJSONObject(doc.toString());
			System.out.println("parsed: " + parsedResult.toString());
			int hits = parsedResult.getJSONObject("api").getJSONObject("query").getJSONObject("searchinfo").getInt("totalhits");
			System.out.println(hits);
			if(hits > 0) {
				JSONObject resultPage = parsedResult.getJSONObject("api").getJSONObject("query").getJSONObject("search").getJSONArray("p").getJSONObject(0);
				this.pageId = resultPage.getInt("pageid");
				this.title = resultPage.getString("title");
				System.out.println(pageId);
				System.out.println(title);
				result = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
}
