package com.IanSloat.noodlebot.controllers.wikipedia;

import java.awt.Color;

public class WikiEndpoint {

	private String apiURL;
	private Color displayColor;
	
	public WikiEndpoint(String apiURL, Color displayColor) {
		this.apiURL = apiURL;
		this.displayColor = displayColor;
	}
	
	public String getApiUrl() {
		return apiURL;
	}
	
	public Color getDisplayColor() {
		return displayColor;
	}
	
}
