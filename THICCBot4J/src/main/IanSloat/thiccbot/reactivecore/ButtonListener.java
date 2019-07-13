package main.IanSloat.thiccbot.reactivecore;

public interface ButtonListener {
	void onButtonClick(ButtonClickEvent event);
	Button getButton();
}
