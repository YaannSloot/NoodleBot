package main.IanSloat.thiccbot.reactivecore;

public class Button implements ButtonListener{

	private ButtonAction action;
	private String emojiName;
	
	public Button(String emojiName, ButtonAction action) {
		this.emojiName = emojiName;
		this.action = action;
	}
	
	public String getEmojiName() {
		return this.emojiName;
	}

	@Override
	public void onButtonClick(String emojiName) {
		if(emojiName.equals(this.emojiName)) {
			action.execute();
		}
	}

	@Override
	public Button getButton() {
		return this;
	}
	
}
