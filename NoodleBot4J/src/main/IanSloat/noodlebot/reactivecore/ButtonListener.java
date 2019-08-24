package main.IanSloat.noodlebot.reactivecore;

public interface ButtonListener {
	void onButtonClick(ButtonClickEvent event);
	Button getButton();
}
