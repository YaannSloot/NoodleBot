package com.IanSloat.noodlebot.controllers.wikipedia;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.IanSloat.noodlebot.BotUtils;

public class WikiResolver {
	
	public QueryResult queryEndpoint(WikiEndpoint endpoint, String query) {
		QueryResult result = null;
		try {
			Document doc;
			doc = Jsoup
					.connect(
							endpoint.getApiUrl() + "?action=query&srsearch=" + query + "&srprop&list=search&format=xml")
					.ignoreContentType(true).get();
			JSONObject parsedResult = XML.toJSONObject(doc.toString());
			boolean hasResults = false;
			if(parsedResult.getJSONObject("api").getJSONObject("query").has("search")) {
				if(parsedResult.getJSONObject("api").getJSONObject("query").get("search") instanceof JSONObject) {
					hasResults = parsedResult.getJSONObject("api").getJSONObject("query").getJSONObject("search").has("p");
				}
			}
			
			if (hasResults) {
				JSONObject resultPage = parsedResult.getJSONObject("api").getJSONObject("query").getJSONObject("search")
						.getJSONArray("p").getJSONObject(0);
				String title = resultPage.getString("title");
				doc = Jsoup.connect(endpoint.getApiUrl() + "?action=query&titles=" + title
						+ "&prop=extracts|info|pageimages&pithumbsize=800&inprop=url&exintro&explaintext&exchars=1200&format=xml")
						.ignoreContentType(true).get();
				parsedResult = XML.toJSONObject(doc.toString());
				resultPage = parsedResult.getJSONObject("api").getJSONObject("query").getJSONObject("pages")
						.getJSONObject("page");
				String summary = resultPage.getJSONObject("extract").getString("content").replace("....", ".");
				if(BotUtils.lastIndexOfRegex(summary, "([A-z].*?\\.{1})") != 0)
					summary = summary.substring(0, BotUtils.lastIndexOfRegex(summary, "([A-z].*?\\.{1})"));
				String pageUrl = resultPage.getString("fullurl");
				String imageUrl;
				try {
					imageUrl = resultPage.getJSONObject("thumbnail").getString("source");
				} catch (JSONException e) {
					imageUrl = "";
				}
				result = new QueryResult(title, summary, imageUrl, pageUrl);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
