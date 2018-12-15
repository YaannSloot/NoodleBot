package main.IanSloat.thiccbot.threadbox;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

public class FilterMessageDeletionJob {

	private final Logger logger = LoggerFactory.getLogger(FilterMessageDeletionJob.class);

	private final static Map<Long, FilterMessageDeletionJob> activeJobs = new HashMap<>();
	private IChannel channel;
	private boolean deleteByLength = false;
	private boolean deleteByUser = false;
	private IUser user = null;
	private int length = 0;
	private Thread jobThread;
	private Instant age = null;

	private FilterMessageDeletionJob(IChannel channel) {
		this.channel = channel;
		jobThread = new Thread(new FilterMessageDeletionThread());
		activeJobs.put(channel.getLongID(), this);
	}

	public static FilterMessageDeletionJob getDeletionJobForChannel(IChannel channel, Instant endDate) {
		FilterMessageDeletionJob job = activeJobs.get(channel.getLongID());
		if (job == null) {
			job = new FilterMessageDeletionJob(channel);
		}
		return job;
	}

	public void deleteByUser(IUser user, boolean value) {
		deleteByUser = value;
		this.user = user;
	}

	public void deleteByLength(int length, boolean value) {
		deleteByLength = value;
		this.length = length;
	}

	public void setAge(Instant age) {
		this.age = age;
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
						jobThread = new Thread(new FilterMessageDeletionThread());
					} else {
						jobThread = new Thread(new FilterMessageDeletionThread());
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

	private class FilterMessageDeletionThread implements Runnable {
		public void run() {
			if (deleteByUser == true && user != null && deleteByLength == true && length != 0) {
				logger.info("Filtered message deletion job was started");
				List<IMessage> history = RequestBuffer.request(() -> {
					return channel.getMessageHistory(2001);
				}).get();
				if (history.size() == 2001) {
					channel.sendMessage("More than 2000 messages were detected."
							+ "\nThis command can only handle 2000 messages at a time."
							+ "\nTo delete more messages, run this command again when it finishes");
				}
				List<IMessage> historyNew = history;
				for (IMessage message : history) {
					if (!(message.getAuthor().equals(user)) && message.getContent().length() < length) {
						historyNew.remove(message);
					}
					if (age != null) {
						if (message.getTimestamp().isAfter(age)) {
							historyNew.remove(message);
						}
					}
				}
				history = historyNew;
				historyNew.clear();
				deleteMessages(history);
			} else if (deleteByUser == true && user != null) {
				logger.info("Filtered message deletion job was started");
				List<IMessage> history = RequestBuffer.request(() -> {
					return channel.getMessageHistory(2001);
				}).get();
				if (history.size() == 2001) {
					channel.sendMessage("More than 2000 messages were detected."
							+ "\nThis command can only handle 2000 messages at a time."
							+ "\nTo delete more messages, run this command again when it finishes");
				}
				List<IMessage> historyNew = history;
				for (IMessage message : history) {
					if (!(message.getAuthor().equals(user))) {
						historyNew.remove(message);
					}
					if (age != null) {
						if (message.getTimestamp().isAfter(age)) {
							historyNew.remove(message);
						}
					}
				}
				history = historyNew;
				historyNew.clear();
				deleteMessages(history);
			} else if (deleteByLength == true && length != 0) {
				logger.info("Filtered message deletion job was started");
				List<IMessage> history = RequestBuffer.request(() -> {
					return channel.getMessageHistory(2001);
				}).get();
				if (history.size() == 2001) {
					channel.sendMessage("More than 2000 messages were detected."
							+ "\nThis command can only handle 2000 messages at a time."
							+ "\nTo delete more messages, run this command again when it finishes");
				}
				List<IMessage> historyNew = history;
				for (IMessage message : history) {
					if (message.getContent().length() < length) {
						historyNew.remove(message);
					}
					if (age != null) {
						if (message.getTimestamp().isAfter(age)) {
							historyNew.remove(message);
						}
					}
				}
				history = historyNew;
				historyNew.clear();
				deleteMessages(history);
			} else {
				logger.error("Job was not configured properly");
			}
		}

		private void deleteMessages(List<IMessage> history) {
			try {
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
			} catch (InterruptedException e) {
				logger.info("Filtered message deletion job was canceled");
			}
		}

	}

}