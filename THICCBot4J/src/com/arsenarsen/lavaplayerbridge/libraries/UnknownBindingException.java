package com.arsenarsen.lavaplayerbridge.libraries;

/**
 * Represents a {@link Library} creation issue.
 */
public class UnknownBindingException extends Exception {
    private final Object library;

    /**
     * Constructs a new {@link UnknownBindingException}.
     */
    public UnknownBindingException(String s, Exception cause, Object library) {
        super(s, cause);
        this.library = library;
    }

    /**
     * Gets the library that could not be bound to a {@link Library} object.
     * @return The library that could not be bound to a {@link Library} object.
     */
    public Object getLibrary() {
        return library;
    }
}
