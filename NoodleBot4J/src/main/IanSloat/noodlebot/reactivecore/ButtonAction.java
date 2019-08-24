package main.IanSloat.noodlebot.reactivecore;

/**
 * An action performed by a message reaction button when clicked by a discord user
 * @author Ian Sloat
 *
 */
@FunctionalInterface
public interface ButtonAction {
	/**
	 * The action (method) to be performed
	 */
	void execute();
}
