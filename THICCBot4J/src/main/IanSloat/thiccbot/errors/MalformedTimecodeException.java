package main.IanSloat.thiccbot.errors;

public class MalformedTimecodeException extends Exception {

	public MalformedTimecodeException (String BadTimeCode) {
		super("The provided timecode \"" + BadTimeCode + "\" is invalid");
	}
	
}
