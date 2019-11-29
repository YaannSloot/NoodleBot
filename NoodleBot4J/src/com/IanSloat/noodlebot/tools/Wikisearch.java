package com.IanSloat.noodlebot.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.simmetrics.metrics.Levenshtein;

/**
 * Basic class used for browsing Wikipedia
 */
public class Wikisearch {

	private String title = "";
	private int pageId;
	private String imageUrl = "";
	private String summary = "";
	private String pageUrl = "";
	private boolean nsfwFlag = false;
	private double threshold = 0.7;

	/**
	 * Performs a page search on Wikipedia
	 * 
	 * @param term        The title to search for
	 * @param doNsfwCheck Whether to cross reference the search with kidzsearch for
	 *                    the purpose of censoring output
	 * @return True if the search was successful
	 */
	public boolean search(String term, boolean doNsfwCheck) {
		boolean result = false;
		try {
			Document doc;
			doc = Jsoup.connect("https://en.wikipedia.org/w/api.php?action=query&srsearch=" + term
					+ "&srprop&list=search&format=xml").ignoreContentType(true).get();
			JSONObject parsedResult = XML.toJSONObject(doc.toString());
			int hits = parsedResult.getJSONObject("api").getJSONObject("query").getJSONObject("searchinfo")
					.getInt("totalhits");
			if (hits > 0) {
				JSONObject resultPage = parsedResult.getJSONObject("api").getJSONObject("query").getJSONObject("search")
						.getJSONArray("p").getJSONObject(0);
				this.title = resultPage.getString("title");
				if (doNsfwCheck) {
					Document nsfwCrossCheck = Jsoup
							.connect("https://wiki.kidzsearch.com/w/api.php?action=query&srsearch=" + title
									+ "&srprop&list=search&format=xml")
							.ignoreContentType(true).get();
					JSONObject parsedCleanTitles = XML.toJSONObject(nsfwCrossCheck.toString());
					int cleanHits = parsedCleanTitles.getJSONObject("api").getJSONObject("query")
							.getJSONObject("searchinfo").getInt("totalhits");
					if (cleanHits > 0) {
						JSONArray cleanResults = new JSONArray();
						Object r = parsedCleanTitles.getJSONObject("api").getJSONObject("query").getJSONObject("search")
								.get("p");
						if (r instanceof JSONObject) {
							cleanResults.put(r);
						} else {
							cleanResults = (JSONArray) r;
						}
						List<String> cleanTitles = new ArrayList<String>();
						double highestDistance = 0.0;
						for (Object p : cleanResults) {
							if (p instanceof JSONObject) {
								if (((JSONObject) p).has("title")) {
									cleanTitles.add(((JSONObject) p).getString("title"));
								}
							}
						}
						Levenshtein distance = new Levenshtein();
						for (String t : cleanTitles) {
							double compute = distance.compare(title, t);
							if (compute > highestDistance) {
								highestDistance = compute;
							}
						}
						if (highestDistance < threshold) {
							nsfwFlag = true;
						}
					} else {
						nsfwFlag = true;
					}
				}
				this.pageId = resultPage.getInt("pageid");
				doc = Jsoup.connect("https://en.wikipedia.org/w/api.php?action=query&pageids=" + pageId
						+ "&prop=extracts|info|pageimages&pithumbsize=800&inprop=url&exintro&explaintext&exchars=1000&format=xml")
						.ignoreContentType(true).get();
				parsedResult = XML.toJSONObject(doc.toString());
				resultPage = parsedResult.getJSONObject("api").getJSONObject("query").getJSONObject("pages")
						.getJSONObject("page");
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
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Performs a page search on Wikipedia
	 * 
	 * @param term The title to search for
	 * @return True if the search was successful
	 */
	public boolean search(String term) {
		return search(term, false);
	}

	/**
	 * Retrieves the url of the page that was retrieved if the search was successful
	 * 
	 * @return The url of the page that was retrieved
	 */
	public String getPageUrl() {
		return pageUrl;
	}

	/**
	 * Retrieves an article summary of the page that was retrieved if the search was
	 * successful
	 * 
	 * @return The summary of the article that was retrieved
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * Retrieves the url of the page thumbnail if one exists
	 * 
	 * @return The url of the page thumbnail
	 */
	public String getThumbnailUrl() {
		return imageUrl;
	}

	/**
	 * Retrieves the title of the page that was retrieved if the search was
	 * successful
	 * 
	 * @return The title of the page that was retrieved
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * If the nsfw check was enabled, this is used to check whether the page was
	 * determined to be nsfw
	 * 
	 * @return True if the page was marked as nsfw
	 */
	public boolean isNSFW() {
		return nsfwFlag;
	}

}
