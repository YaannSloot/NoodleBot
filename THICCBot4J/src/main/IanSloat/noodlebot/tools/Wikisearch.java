package main.IanSloat.noodlebot.tools;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wikisearch {

	private static final Logger logger = LoggerFactory.getLogger(Wikisearch.class);
	private String title = "";
	private int pageId;
	private String imageUrl = "";
	private String summary = "";
	private String pageUrl = "";
	
	public Wikisearch() {
		
	}
	
	public boolean search(String term) {
		boolean result = false;
		try {
			Document doc;
			doc = Jsoup.connect("https://en.wikipedia.org/w/api.php?action=query&srsearch=" + term + "&srprop&list=search&format=xml")
					.ignoreContentType(true)
					.get();
			JSONObject parsedResult = XML.toJSONObject(doc.toString());
			int hits = parsedResult.getJSONObject("api").getJSONObject("query").getJSONObject("searchinfo").getInt("totalhits");
			if(hits > 0) {
				JSONObject resultPage = parsedResult.getJSONObject("api").getJSONObject("query").getJSONObject("search").getJSONArray("p").getJSONObject(0);
				this.pageId = resultPage.getInt("pageid");
				this.title = resultPage.getString("title");
				doc = Jsoup.connect("https://en.wikipedia.org/w/api.php?action=query&pageids=" + pageId + "&prop=extracts|info|pageimages&pithumbsize=800&inprop=url&exintro&explaintext&exchars=1000&format=xml")
						.ignoreContentType(true)
						.get();
				parsedResult = XML.toJSONObject(doc.toString());
				resultPage = parsedResult.getJSONObject("api").getJSONObject("query").getJSONObject("pages").getJSONObject("page");
				this.summary = resultPage.getJSONObject("extract").getString("content");
				this.pageUrl = resultPage.getString("fullurl");
				try {
					this.imageUrl = resultPage.getJSONObject("thumbnail").getString("source");
				} catch (JSONException e) {
					imageUrl = "";
				}
				result = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public String getPageUrl() {
		return pageUrl;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public String getThumbnailUrl() {
		return imageUrl;
	}
	
	public String getTitle() {
		return title;
	}
	
}
