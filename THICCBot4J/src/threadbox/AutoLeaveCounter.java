package threadbox;

import java.util.ArrayList;

import sx.blah.discord.handle.obj.IVoiceChannel;

public class AutoLeaveCounter extends Thread{
	
	private static ArrayList<AutoLeaveCounter> counterList = new ArrayList<AutoLeaveCounter>();
	private IVoiceChannel voiceChannel;
	private boolean continueAutoLeave;
	
	public void run() {
		try {
			Thread.sleep(60000);
			if(continueAutoLeave == true) {
				voiceChannel.leave();
				counterList.remove(this);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stopCountDown() {
		continueAutoLeave = false;
		counterList.remove(this);
	}
	
	public AutoLeaveCounter(IVoiceChannel voiceChannel) {
		this.voiceChannel = voiceChannel;
		this.continueAutoLeave = true;
		counterList.add(this);
	}
	
	public IVoiceChannel getChannel() {
		return voiceChannel;
	}
	
	public static ArrayList<AutoLeaveCounter> getAllRunningCounters(){
		return counterList;
	}
	
}
