package main.IanSloat.thicbot.tools;

public class ytdlOutputProcessor {
	
	private String jsonText;
	
	ytdlOutputProcessor(String jsonText) {
		this.jsonText = jsonText;
		
	}
	
	public String getUrl() {
		String output = "";
		int startIndex = jsonText.indexOf("https://r");
		int endIndex = jsonText.indexOf("\", \"", startIndex);
		output = jsonText.substring(startIndex, endIndex);
		return output;
	}
	
	public String getUploader() {
		String output = "";
		int startIndex = jsonText.indexOf("\"uploader\": \"") + 13;
		int endIndex = jsonText.indexOf("\", \"", startIndex);
		output = jsonText.substring(startIndex, endIndex);
		return output;
	}
	
	public String getVideoUrl() {
		String output = "";
		int startIndex = jsonText.indexOf("\"webpage_url\": \"") + 16;
		int endIndex = jsonText.indexOf("\", \"", startIndex);
		output = jsonText.substring(startIndex, endIndex);
		return output;
	}
	
	public String getDuration() {
		String output = "";
		int startIndex = jsonText.indexOf("\"duration\": ") + 12;
		int endIndex = jsonText.indexOf(", \"", startIndex);
		output = jsonText.substring(startIndex, endIndex);
		return output;
	}
	
}
