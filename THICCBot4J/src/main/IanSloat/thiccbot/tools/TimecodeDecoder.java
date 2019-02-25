package main.IanSloat.thiccbot.tools;

import main.IanSloat.thiccbot.errors.MalformedTimecodeException;

public class TimecodeDecoder {

	private String rawCode;
	private int seconds;
	
	public TimecodeDecoder (String timecode) {
		this.rawCode = timecode;
		this.seconds = 0;
	}
	
	public void decode() throws MalformedTimecodeException {
		throw new MalformedTimecodeException(rawCode);
	}
	
}
