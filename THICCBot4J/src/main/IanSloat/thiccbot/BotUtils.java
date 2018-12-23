package main.IanSloat.thiccbot;

import java.util.List;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

public class BotUtils {

	// Constants for use throughout the bot
	public static String BOT_PREFIX = "thicc ";
	public static String PATH_SEPARATOR = System.getProperty("file.separator");

	// Handles the creation and getting of a IDiscordClient object for a token
	static IDiscordClient getBuiltDiscordClient(String token) {

		// The ClientBuilder object is where you will attach your params for configuring
		// the instance of your bot.
		// Such as withToken, setDaemon etc
		return new ClientBuilder().withToken(token).withRecommendedShardCount().build();
	}

	// Helpful input processing methods
	public static boolean checkForWords(String inputSentence, String[] Wordlist, boolean isCaseSensitive,
			boolean insertSpaces) {
		boolean isTrue = false;
		if (!isCaseSensitive) {
			inputSentence = inputSentence.toLowerCase();
			for (int i = 0; i < Wordlist.length; i++)
				Wordlist[i] = Wordlist[i].toLowerCase();
		}
		for (String word : Wordlist) {
			if (insertSpaces)
				word = " " + word + " ";
			if (inputSentence.contains(word))
				isTrue = true;
		}
		return isTrue;
	}

	public static boolean checkForWords(String inputSentence, String[] Wordlist, boolean isCaseSensitive) {
		boolean isTrue = false;
		if (!isCaseSensitive) {
			inputSentence = inputSentence.toLowerCase();
			for (int i = 0; i < Wordlist.length; i++)
				Wordlist[i] = Wordlist[i].toLowerCase();
		}
		for (String word : Wordlist)
			if (inputSentence.contains(word))
				isTrue = true;
		return isTrue;
	}

	public static boolean checkForWords(String inputSentence, String[] Wordlist) {
		boolean isTrue = false;
		for (String word : Wordlist)
			if (inputSentence.contains(word))
				isTrue = true;
		return isTrue;
	}
	
	public static boolean checkForWords(List<String> inputList, String[] Wordlist) {
		boolean isTrue = false;
		for (String word : Wordlist)
			if (inputList.contains(word))
				isTrue = true;
		return isTrue;
	}

	public static String normalizeSentence(String input) {
		String output = "";
		if (input.length() != 0) {
			input = input.trim();
			char previousChar = input.charAt(0);
			for (char c : input.toCharArray()) {
				if (previousChar == ' ') {
					if (c != previousChar) {
						output += c;
					}
				} else {
					output += c;
				}
				previousChar = c;
			}
		}
		return output;
	}

}
