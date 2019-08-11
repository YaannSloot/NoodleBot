package main.IanSloat.noodlebot.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import main.IanSloat.noodlebot.errors.MalformedTimecodeException;

public class Timecode {
	
	private String rawCode;
	private long millis;

	public Timecode(String timecode) {
		this.rawCode = timecode;
		this.millis = 0;
	}

	private static double roundAvoid(double value, int places) {
		double scale = Math.pow(10, places);
		return Math.round(value * scale) / scale;
	}

	/**
	 * Decodes the raw timecode string and stores the time value in the timecode
	 * object
	 * 
	 * @throws MalformedTimecodeException if the formatting of the timecode is incorrect
	 * @throws NullPointerException if something really bad happens
	 * @throws NumberFormatException if the numbers in the timecode aren't actual numbers
	 */
	public void decode() throws MalformedTimecodeException, NullPointerException, NumberFormatException {
		List<String> numbers = Arrays.asList(rawCode.split(":"));
		Collections.reverse(numbers);
		if (Double.parseDouble(numbers.get(0)) > 60 && numbers.size() > 1) {
			throw new MalformedTimecodeException(rawCode);
		}
		millis = Math.round(roundAvoid(Double.parseDouble(numbers.get(0)), 3) * 1000);
		if (numbers.size() > 1) {
			boolean isFirst = true;
			switch (numbers.size()) {
			case 7:
				int years = Integer.parseInt(numbers.get(6));
				millis += (long) years * 31556952000L;
				isFirst = false;
			case 6:
				int months = Integer.parseInt(numbers.get(5));
				if (isFirst == false && months > 12) {
					throw new MalformedTimecodeException(rawCode);
				} else {
					millis += (long) months * 2592000000L;
				}
				isFirst = false;
			case 5:
				int weeks = Integer.parseInt(numbers.get(4));
				if (isFirst == false && weeks > 4) {
					throw new MalformedTimecodeException(rawCode);
				} else {
					millis += (long) weeks * 604800000L;
				}
				isFirst = false;
			case 4:
				int days = Integer.parseInt(numbers.get(3));
				if (isFirst == false && days > 7) {
					throw new MalformedTimecodeException(rawCode);
				} else {
					millis += (long) days * 86400000L;
				}
				isFirst = false;
			case 3:
				int hours = Integer.parseInt(numbers.get(2));
				if (isFirst == false && hours > 24) {
					throw new MalformedTimecodeException(rawCode);
				} else {
					millis += (long) hours * 3600000L;
				}
				isFirst = false;
			case 2:
				int minutes = Integer.parseInt(numbers.get(1));
				if (isFirst == false && minutes > 60) {
					throw new MalformedTimecodeException(rawCode);
				} else {
					millis += (long) minutes * 60000L;
				}
				break;
			default:
				throw new MalformedTimecodeException(rawCode);
			}
		}
	}

	public long getMillis() {
		return millis;
	}

	public double getSeconds() {
		return millis / 1000L;
	}
	
	public double getMinutes() {
		return millis / 60000L;
	}

	public double getHours() {
		return millis / 3600000L;
	}

	public double getDays() {
		return millis / 86400000L;
	}

	public double getWeeks() {
		return millis / 604800000L;
	}

	public double getMonths() {
		return millis / 2592000000L;
	}

	public double getYears() {
		return millis / 31556952000L;
	}

}
