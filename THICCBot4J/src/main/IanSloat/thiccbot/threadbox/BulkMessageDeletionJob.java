package main.IanSloat.thiccbot.threadbox;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.TextChannel;

public class BulkMessageDeletionJob {

	private final static Map<Long, BulkMessageDeletionJob> activeJobs = new HashMap<>();
	private TextChannel channel;
	private Instant endDate;
	private Thread jobThread;

	private BulkMessageDeletionJob(TextChannel channel, Instant endDate) {
		this.channel = channel;
		this.endDate = endDate;
		jobThread = new Thread(new DeletionJobThread());
		activeJobs.put(channel.getIdLong(), this);
	}

	public static BulkMessageDeletionJob getDeletionJobForChannel(TextChannel channel, Instant endDate) {
		BulkMessageDeletionJob job = activeJobs.get(channel.getIdLong());
		if (job == null) {
			job = new BulkMessageDeletionJob(channel, endDate);
		} else {
			job.endDate = endDate;
		}
		return job;
	}

	public synchronized void stopJob() {
		if (jobThread.isAlive()) {
			jobThread.interrupt();
		}
	}

	public synchronized void startJob() {
		stopJob();
		Thread cleanup = new Thread(new Runnable() {
			public void run() {
				try {
					if (jobThread.isAlive()) {
						jobThread.join();
						jobThread = new Thread(new DeletionJobThread());
					} else {
						jobThread = new Thread(new DeletionJobThread());
					}
					jobThread.start();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		cleanup.start();
	}

	private class DeletionJobThread implements Runnable {

		private final Logger logger = LoggerFactory.getLogger(BulkMessageDeletionJob.class);

		public void run() {
			/*logger.info("A new message deletion job has been started");channel.getHistory().
			try {channel.getHistoryAfter(channel., limit)
				List<Message> history = channel.getMessageHistoryFrom(endDate, 2001);
				if (history.size() > 0) {
					if (history.size() == 2001) {
						channel.sendMessage("More than 2000 messages were detected."
								+ "\nThis command can only handle 2000 messages at a time."
								+ "\nTo delete more messages, run this command again when it finishes");
					}
					channel.sendMessage("Deleting messages.\nThis may take a while...");
					long deletes = 0;
					IMessage status = channel.sendMessage("Status: 0/" + history.size() + " messages deleted");
					for (IMessage message : history) {
						boolean deleteSuccess = false;
						while (deleteSuccess == false) {
							try {
								message.delete();
								deleteSuccess = true;
								deletes++;
								final long deletesCopy = deletes;
								status.edit("Status: " + deletesCopy + '/' + history.size() + " messages deleted");
							} catch (RateLimitException e) {
								Thread.sleep(100);
							}
						}
					}
					RequestBuffer.request(() -> status.edit("Messages deleted"));
				} else {
					channel.sendMessage("No messages found in the timeframe specified");
				}
				logger.info("Message deletion job has finished");
			} catch (InterruptedException e) {
				logger.info("Message deletion job was canceled");
			}*/
		}
	}

}
