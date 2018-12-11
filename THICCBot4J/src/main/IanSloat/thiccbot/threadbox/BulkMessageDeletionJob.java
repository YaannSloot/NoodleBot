package main.IanSloat.thiccbot.threadbox;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

public class BulkMessageDeletionJob {

	private final static Map<Long, BulkMessageDeletionJob> activeJobs = new HashMap<>();
	private IChannel channel;
	private Instant endDate;
	private Thread jobThread;

	private BulkMessageDeletionJob(IChannel channel, Instant endDate) {
		this.channel = channel;
		this.endDate = endDate;
		jobThread = new Thread(new DeletionJobThread());
		activeJobs.put(channel.getLongID(), this);
	}

	public static BulkMessageDeletionJob getDeletionJobForChannel(IChannel channel, Instant endDate) {
		BulkMessageDeletionJob job = activeJobs.get(channel.getLongID());
		if (job == null) {
			job = new BulkMessageDeletionJob(channel, endDate);
		} else {
			job.endDate = endDate;
		}
		return job;
	}

	public synchronized void stopJob() {
		if (jobThread.isAlive()) {
			System.out.println("Hey its running");
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
			logger.info("A new message deletion job has been started");
			try {
				channel.sendMessage("Deleting messages older than one week.\nThis may take a while...");
				final Instant currentTime = endDate;
				List<IMessage> history = RequestBuffer.request(() -> {
					return channel.getMessageHistoryFrom(currentTime);
				}).get();
				long deletes = 0;
				IMessage status = channel.sendMessage("Status:");
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
				channel.sendMessage("Messages deleted");
				logger.info("Message deletion job has finished");
			} catch (InterruptedException e) {
				logger.info("Message deletion job was canceled");
			}
		}
	}

}
