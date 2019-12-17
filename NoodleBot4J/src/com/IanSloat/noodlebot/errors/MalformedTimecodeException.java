package com.IanSloat.noodlebot.errors;

/**
 * Indicates that a {@linkplain com.IanSloat.noodlebot.tools.Timecode Timecode}
 * was not formatted properly during construction
 */
public class MalformedTimecodeException extends RuntimeException {

	private static final long serialVersionUID = 354898095228381949L;

	public MalformedTimecodeException(String BadTimeCode) {
		super("The provided timecode \"" + BadTimeCode + "\" is invalid");
	}

}
