package com.IanSloat.noodlebot.errors;

public class MalformedTimecodeException extends Exception {

	private static final long serialVersionUID = 354898095228381949L;

	public MalformedTimecodeException (String BadTimeCode) {
		super("The provided timecode \"" + BadTimeCode + "\" is invalid");
	}
	
}
