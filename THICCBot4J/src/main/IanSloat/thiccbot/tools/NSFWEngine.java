package main.IanSloat.thiccbot.tools;

import java.io.IOException;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class NSFWEngine {

	public String getR34ImageUrl(String tags, int resultAmount) {
		String result = "";
		try {
			Document doc;
			doc = Jsoup.connect("https://r34-json-api.herokuapp.com/posts?tags=" + tags + "&limit=" + resultAmount)
					.ignoreContentType(true)
					.get();
			String JSONString = doc.getElementsByTag("body").text();
			JSONArray parsedResult = new JSONArray(JSONString);
			if(parsedResult.length() > 0) {
				Random rand = new Random();
				JSONObject post = parsedResult.getJSONObject(rand.nextInt(parsedResult.length()));
				result = post.getString("file_url");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
}
