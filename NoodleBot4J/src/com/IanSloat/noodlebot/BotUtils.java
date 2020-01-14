package com.IanSloat.noodlebot;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

/**
 * Set of helpful methods for processing input. Also contains a few bot-wide
 * constants to use. Does not contain a constructor. Contains only static
 * objects and methods.
 */
public class BotUtils {

	// Constants for use throughout the bot
	public static final String BOT_PREFIX = "noodt ";
	private static final Logger logger = LoggerFactory.getLogger(BotUtils.class);

	/**
	 * Checks for whether a given string contains any string from a provided string
	 * array
	 * 
	 * @param inputSentence   The input string to scan through
	 * @param Wordlist        The array of strings to check for
	 * @param isCaseSensitive Whether to do a case sensitive search
	 * @param insertSpaces    Whether to insert whitespace characters at the
	 *                        beginning and end of each string in the string array
	 * @return true if one of the strings in the Wordlist array was found in the
	 *         inputSentence string
	 */
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

	/**
	 * Checks for whether a given string contains any string from a provided string
	 * array
	 * 
	 * @param inputSentence   The input string to scan through
	 * @param Wordlist        The array of strings to check for
	 * @param isCaseSensitive Whether to do a case sensitive search
	 * @return true if one of the strings in the Wordlist array was found in the
	 *         inputSentence string
	 */
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

	/**
	 * Checks for whether a given string contains any string from a provided string
	 * array
	 * 
	 * @param inputSentence The input string to scan through
	 * @param Wordlist      The array of strings to check for
	 * @return true if one of the strings in the Wordlist array was found in the
	 *         inputSentence string
	 */
	public static boolean checkForWords(String inputSentence, String[] Wordlist) {
		boolean isTrue = false;
		for (String word : Wordlist)
			if (inputSentence.contains(word))
				isTrue = true;
		return isTrue;
	}

	/**
	 * Checks whether a list of strings contains any string from a provided string
	 * array
	 * 
	 * @param inputList The list of strings to scan through
	 * @param Wordlist  The array of strings to check for
	 * @return true if one of the strings in the Wordlist array was found in the
	 *         inputList string list
	 */
	public static boolean checkForWords(List<String> inputList, String[] Wordlist) {
		boolean isTrue = false;
		for (String word : Wordlist)
			if (inputList.contains(word))
				isTrue = true;
		return isTrue;
	}

	/**
	 * Trims the provided string and removes extra whitespace characters between
	 * each word
	 * 
	 * @param input The string to normalize
	 * @return A normalized string
	 */
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

	/**
	 * Capitalizes each word in a string and normalizes the string
	 * 
	 * @param input The string to capitalize each word in
	 * @return A modified string that has been normalized and had each word
	 *         capitalized
	 */
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

	/**
	 * Checks whether a given list contains any element from another provided list
	 * 
	 * @param list     The list to scan through
	 * @param elements The list of elements to find
	 * @return true if <code>list</code> contains any elements from
	 *         <code>elements</code>
	 */
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

	/**
	 * Checks whether an array of strings contains a specified string
	 * 
	 * @param array The array to scan through
	 * @param word  The string to search for
	 * @return true if <code>array</code> contains <code>word</code>
	 */
	public static boolean stringArrayContains(String[] array, String word) {
		boolean result = false;
		for (String w : array) {
			if (w.equals(word)) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Safely deletes a JDA message
	 * 
	 * @param message the message to delete
	 */
	public static void messageSafeDelete(Message message) {
		try {
			message.delete().queue();
		} catch (InsufficientPermissionException e) {
			logger.warn("Attempted to delete a message in #" + message.getChannel().getName() + '@'
					+ message.getGuild().getName() + "(id:" + message.getGuild().getId()
					+ ") but the required permission \"" + e.getPermission().getName()
					+ "\" is missing from the bot's role");
		} catch (Exception e) {
		}
	}

	/**
	 * Safely deletes a JDA message after a specified delay
	 * 
	 * @param message   the message to delete
	 * @param time      the amount of time to wait for
	 * @param timescale the unit of time for the specified amount
	 */
	public static void messageSafeDelete(Message message, long time, TimeUnit timescale) {
		try {
			message.delete().queueAfter(time, timescale);
		} catch (InsufficientPermissionException e) {
			logger.warn("Attempted to delete a message in #" + message.getChannel().getName() + '@'
					+ message.getGuild().getName() + "(id:" + message.getGuild().getId()
					+ ") but the required permission \"" + e.getPermission().getName()
					+ "\" is missing from the bot's role");
		} catch (Exception e) {
		}
	}

}
