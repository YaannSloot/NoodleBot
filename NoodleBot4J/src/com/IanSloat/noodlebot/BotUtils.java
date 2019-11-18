package com.IanSloat.noodlebot;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

// TODO Document methods
public class BotUtils {

	// Constants for use throughout the bot
	public static final String BOT_PREFIX = "noodv2 ";
	private static final Logger logger = LoggerFactory.getLogger(BotUtils.class);

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

	public static String capitalizeWords(String input) {
		String result = "";
		List<String> words = Arrays.asList(normalizeSentence(input).toLowerCase().split(" "));
		for (int i = 0; i < words.size(); i++) {
			result += ("" + words.get(i).charAt(0)).toUpperCase()
					+ ((words.get(i).length() > 1) ? words.get(i).substring(1) : "")
					+ ((i == words.size() - 1) ? "" : " ");
		}
		return result;
	}

	public static boolean checkForElement(List<?> list, List<?> elements) {
		boolean value = false;
		for (Object element : elements) {
			if (list.contains(element)) {
				value = true;
				break;
			}
		}
		return value;
	}

	public static boolean stringArrayContains(String[] array, String word) {
		boolean result = false;
		for (String w : array) {
			if (w.equals(word)) {
				result = true;
			}
		}
		return result;
	}

	public static void messageSafeDelete(Message message) {
		try {
			message.delete().queue();
		} catch (InsufficientPermissionException e) {
			logger.warn("Attempted to delete a message in #" + message.getChannel().getName() + '@'
					+ message.getGuild().getName() + "(id:" + message.getGuild().getId()
					+ ") but the required permission \"" + e.getPermission().getName()
					+ "\" is missing from the bot's role");
		}
	}

	public static void messageSafeDelete(Message message, long time, TimeUnit timescale) {
		try {
			message.delete().queueAfter(time, timescale);
		} catch (InsufficientPermissionException e) {
			logger.warn("Attempted to delete a message in #" + message.getChannel().getName() + '@'
					+ message.getGuild().getName() + "(id:" + message.getGuild().getId()
					+ ") but the required permission \"" + e.getPermission().getName()
					+ "\" is missing from the bot's role");
		}
	}

}
