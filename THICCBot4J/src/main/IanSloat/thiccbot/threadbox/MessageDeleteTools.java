package main.IanSloat.thiccbot.threadbox;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer;

public class MessageDeleteTools {

	public static void DeleteAfterMillis(IMessage message, long millis) {
		class delete implements Runnable{
			public void run() {
				try {
					Thread.sleep(millis);
					RequestBuffer.request(() -> message.delete());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		Thread thread = new Thread(new delete());
		thread.start();
	}
	
}
