package main.IanSloat.thiccbot.threadbox;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class FilterMessageDeletionJob {

	private final Logger logger = LoggerFactory.getLogger(FilterMessageDeletionJob.class);

	private final static Map<Long, FilterMessageDeletionJob> activeJobs = new HashMap<>();
	private TextChannel channel;
	private boolean deleteByLength = false;
	private boolean deleteByUser = false;
	private List<Member> users = null;
	private int length = 0;
	private Thread jobThread;
	private Instant age = null;

	private FilterMessageDeletionJob(TextChannel channel) {
		this.channel = channel;
		jobThread = new Thread(new FilterMessageDeletionThread());
		activeJobs.put(channel.getIdLong(), this);
	}

	public static FilterMessageDeletionJob getDeletionJobForChannel(TextChannel channel) {
		FilterMessageDeletionJob job = activeJobs.get(channel.getIdLong());
		if (job == null) {
			job = new FilterMessageDeletionJob(channel);
		}
		return job;
	}

	public void deleteByUser(List<Member> users, boolean value) {
		deleteByUser = value;
		this.users = users;
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
		public void run() {/*
			if (deleteByUser == true && users != null && deleteByLength == true && length != 0) {
				logger.info("Filtered message deletion job was started");
				List<IMessage> history;
				if (age != null) {
					history = RequestBuffer.request(() -> {
						return channel.getMessageHistoryFrom(age, 2001);
					}).get();
				} else {
					history = RequestBuffer.request(() -> {
						return channel.getMessageHistory(2001);
					}).get();
				}
				if (history.size() == 2001) {
					channel.sendMessage("More than 2000 messages were detected."
							+ "\nThis command can only handle 2000 messages at a time."
							+ "\nTo delete more messages, run this command again when it finishes");
				}
				List<IMessage> historyNew = new ArrayList<IMessage>();
				historyNew.addAll(history);
				for (IMessage message : history) {
					if (!(users.contains(message.getAuthor())) && message.getContent().length() < length) {
						historyNew.remove(message);
					}
				}
				history = historyNew;
				historyNew.clear();
				deleteMessages(historyNew);
			} else if (deleteByUser == true && users != null) {
				logger.info("Filtered message deletion job was started");
				List<IMessage> history;
				if (age != null) {
					history = RequestBuffer.request(() -> {
						return channel.getMessageHistoryFrom(age, 2001);
					}).get();
				} else {
					history = RequestBuffer.request(() -> {
						return channel.getMessageHistory(2001);
					}).get();
				}
				if (history.size() == 2001) {
					channel.sendMessage("More than 2000 messages were detected."
							+ "\nThis command can only handle 2000 messages at a time."
							+ "\nTo delete more messages, run this command again when it finishes");
				}
				List<IMessage> historyNew = new ArrayList<IMessage>();
				historyNew.addAll(history);
				for (int i = 0; i < history.size(); i++) {
					if (!(users.contains(history.get(i).getAuthor()))) {
						historyNew.remove(history.get(i));
					}
				}
				deleteMessages(historyNew);
			} else if (deleteByLength == true && length != 0) {
				logger.info("Filtered message deletion job was started");
				List<IMessage> history;
				if (age != null) {
					history = RequestBuffer.request(() -> {
						return channel.getMessageHistoryFrom(age, 2001);
					}).get();
				} else {
					history = RequestBuffer.request(() -> {
						return channel.getMessageHistory(2001);
					}).get();
				}
				if (history.size() == 2001) {
					channel.sendMessage("More than 2000 messages were detected."
							+ "\nThis command can only handle 2000 messages at a time."
							+ "\nTo delete more messages, run this command again when it finishes");
				}
				List<IMessage> historyNew = new ArrayList<IMessage>();
				historyNew.addAll(history);
				for (IMessage message : history) {
					if (message.getContent().length() < length) {
						historyNew.remove(message);
					}
				}
				deleteMessages(historyNew);
			} else if (age != null) {
				List<IMessage> history = RequestBuffer.request(() -> {
					return channel.getMessageHistoryFrom(age, 2001);
				}).get();
				if (history.size() == 2001) {
					channel.sendMessage("More than 2000 messages were detected."
							+ "\nThis command can only handle 2000 messages at a time."
							+ "\nTo delete more messages, run this command again when it finishes");
				}
				deleteMessages(history);
			} else {
				logger.error("Job was not configured properly");
			}*/
		}

		private void deleteMessages(List<Message> history) {/*
			try {
				channel.sendMessage("Deleting messages.\nThis may take a while...");
				long deletes = 0;
				Message status = channel.sendMessage("Status: 0/" + history.size() + " messages deleted");
				for (Message message : history) {
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
			}*/
		}

	}

}
