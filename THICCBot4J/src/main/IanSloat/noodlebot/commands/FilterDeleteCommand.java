package main.IanSloat.noodlebot.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.threadbox.FilterMessageDeletionJob;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class FilterDeleteCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.BY_FILTER, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return (command.getContentRaw().toLowerCase()
				.startsWith(BotUtils.BOT_PREFIX + "delete messages older than ")
				|| command.getContentRaw().toLowerCase()
						.startsWith(BotUtils.BOT_PREFIX + "delete messages from "));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		String ageString = event.getMessage().getContentRaw().toLowerCase()
				.replace(BotUtils.BOT_PREFIX + "delete messages older than ", "");
		ageString = ageString.replace(',', ' ');
		ageString = BotUtils.normalizeSentence(ageString);
		List<String> words = new ArrayList<String>();
		for (String word : ageString.split(" ")) {
			if (word.equals("day")) {
				word = "days";
			} else if (word.equals("week")) {
				word = "weeks";
			} else if (word.equals("month")) {
				word = "months";
			} else if (word.equals("year")) {
				word = "years";
			}
			words.add(word);
		}
		List<Member> usersMentioned = new ArrayList<Member>();
		List<Role> rolesMentioned = new ArrayList<Role>();
		if (words.contains("from")) {
			usersMentioned = event.getMessage().getMentionedMembers();
			rolesMentioned = event.getMessage().getMentionedRoles();
		}
		Calendar date = new GregorianCalendar();
		String[] ageWords = { "days", "weeks", "months", "years" };
		int days = 0;
		int weeks = 0;
		int months = 0;
		int years = 0;
		while (BotUtils.checkForWords(words, ageWords)) {
			if (words.contains("days") && words.indexOf("days") != 0) {
				try {
					int amount = Integer.parseInt(words.get(words.indexOf("days") - 1));
					words.remove(words.get(words.indexOf("days") - 1));
					words.remove("days");
					date.roll(Calendar.DAY_OF_YEAR, amount * -1);
					days += amount;
				} catch (NumberFormatException e) {
					words.remove("days");
				}
			} else if (words.contains("weeks") && words.indexOf("weeks") != 0) {
				try {
					int amount = Integer.parseInt(words.get(words.indexOf("weeks") - 1));
					words.remove(words.get(words.indexOf("weeks") - 1));
					words.remove("weeks");
					date.roll(Calendar.WEEK_OF_YEAR, amount * -1);
					weeks += amount;
				} catch (NumberFormatException e) {
					words.remove("weeks");
				}
			} else if (words.contains("months") && words.indexOf("months") != 0) {
				try {
					int amount = Integer.parseInt(words.get(words.indexOf("months") - 1));
					words.remove(words.get(words.indexOf("months") - 1));
					words.remove("months");
					date.roll(Calendar.MONTH, amount * -1);
					months += amount;
				} catch (NumberFormatException e) {
					words.remove("months");
				}
			} else if (words.contains("years") && words.indexOf("years") != 0) {
				try {
					int amount = Integer.parseInt(words.get(words.indexOf("years") - 1));
					words.remove(words.get(words.indexOf("years") - 1));
					words.remove("years");
					date.roll(Calendar.YEAR, amount * -1);
					years += amount;
				} catch (NumberFormatException e) {
					words.remove("years");
				}
			} else if (words.indexOf("days") == 0 || words.indexOf("weeks") == 0 || words.indexOf("months") == 0
					|| words.indexOf("years") == 0) {
				words.remove(0);
			}
			// System.out.println(String.join(" ", words));
		}
		if (days > 0 || weeks > 0 || months > 0 || years > 0 || usersMentioned.size() > 0
				|| rolesMentioned.size() > 0 || event.getMessage().mentionsEveryone()) {
			FilterMessageDeletionJob job = FilterMessageDeletionJob.getDeletionJobForChannel(event.getTextChannel());
			job.setAge(date.toInstant().atOffset(event.getMessage().getTimeCreated().getOffset()));
			if (usersMentioned.size() > 0 || rolesMentioned.size() > 0 || event.getMessage().mentionsEveryone()) {
				List<Member> users = new ArrayList<Member>();
				users.addAll(usersMentioned);
				if(event.getMessage().mentionsEveryone())
					users.addAll(event.getGuild().getMembers());
				for (Role role : rolesMentioned) {
					for (Member user : event.getGuild().getMembersWithRoles(role)) {
						if (!(users.contains(user))) {
							users.add(user);
						}
					}
				}
				job.deleteByUser(users, true);
			}
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
			final String dateString = dateFormatter.format(date.getTime());
			event.getChannel().sendMessage("Finding messages older than " + dateString).queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
			job.startJob();
		} else {
			event.getChannel().sendMessage(
					"Not sure what kind of calendar you are using,\n" + "but I cannot understand what you just said")
				.queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
		}
	}
}
