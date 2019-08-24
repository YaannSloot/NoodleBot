package main.IanSloat.noodlebot.threadbox;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
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
	private Thread jobThread;
	private OffsetDateTime age = null;

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

	public void setAge(OffsetDateTime age) {
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

	private boolean isMessageWrittenByTargets(Message m) {
		boolean result = false;
		for(Member u : users) {
			if(m.getAuthor().equals(u.getUser())) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	private class FilterMessageDeletionThread implements Runnable {
		public void run() {
			if ((deleteByUser && users != null) || age != null) {
				logger.info("Filtered message deletion job was started");
				List<Message> history; 
				Stream<Message> HistoryIterator = channel.getIterableHistory().stream().limit(2000);
				
				if (age != null) 
					HistoryIterator = HistoryIterator.filter(m -> m.getTimeCreated().isBefore(age));
				
				if (deleteByUser)
					HistoryIterator = HistoryIterator.filter(m -> isMessageWrittenByTargets(m));
					
				history = HistoryIterator.collect(Collectors.toList());
				
				if (history.size() == 2000) {
					channel.sendMessage("2000 messages were retrieved."
							+ "This may mean that this channel's history is longer than 2000 messages."
							+ "\nThis command can only handle 2000 messages at a time."
							+ "\nTo delete more messages, run this command again when it finishes").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
				}
				deleteMessages(history);
				logger.info("Filtered message deletion job finished");
			} else {
				logger.error("Job was not configured properly");
			}
		}

		private void deleteMessages(List<Message> history) {
			EmbedBuilder status = new EmbedBuilder();
			status.setTitle("Message deletion job | #" + channel.getName() + "@" + channel.getGuild().getName());
			status.addField("Deleting messages.", "This may take a while...", false);
			status.addField("Status", "0/" + history.size()+ " messages deleted", false);
			status.setColor(Color.CYAN);
			Message statmsg = channel.sendMessage(status.build()).complete();
			long deletes = 0;
			boolean isStopped = false;
			for (Message message : history) {
				if(Thread.interrupted()) {
					isStopped = true;
					break;
				}
				message.delete().complete();
				deletes++;
				status = new EmbedBuilder();
				status.setTitle("Message deletion job | #" + channel.getName() + "@" + channel.getGuild().getName());
				status.addField("Deleting messages.", "This may take a while...", false);
				status.addField("Status", deletes + "/" + history.size() + " messages deleted", false);
				status.setColor(Color.CYAN);
				statmsg.editMessage(status.build()).complete();
			}
			if(!isStopped) {
				status = new EmbedBuilder();
				status.setTitle("Message deletion job | #" + channel.getName() + "@" + channel.getGuild().getName());
				status.addField("Status", "Messages deleted", false);
				status.setColor(Color.CYAN);
				statmsg.editMessage(status.build()).queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
			} else {
				status = new EmbedBuilder();
				status.setTitle("Message deletion job | #" + channel.getName() + "@" + channel.getGuild().getName());
				status.addField("Status", "Job Canceled", false);
				status.setColor(Color.CYAN);
				statmsg.editMessage(status.build()).queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		}

	}

}
