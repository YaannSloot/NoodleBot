package main.IanSloat.thiccbot.threadbox;

import java.util.ArrayList;

import sx.blah.discord.handle.obj.IVoiceChannel;

public class AutoLeaveCounter extends Thread{
	
	private IVoiceChannel voiceChannel;
	
	public void run() {
		try {
			Thread.sleep(60000);
			if(voiceChannel.getConnectedUsers().size() == 1) {
				voiceChannel.leave();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public AutoLeaveCounter(IVoiceChannel voiceChannel) {
		this.voiceChannel = voiceChannel;
	}
}
