package main.IanSloat.thiccbot.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.threadbox.FilterMessageDeletionJob;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

public class FilterDeleteCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.BY_FILTER, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return (command.getContent().toLowerCase()
				.startsWith(BotUtils.BOT_PREFIX + "delete messages older than ")
				|| command.getContent().toLowerCase()
						.startsWith(BotUtils.BOT_PREFIX + "delete messages from "));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		String ageString = event.getMessage().getContent().toLowerCase()
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
		List<IUser> usersMentioned = new ArrayList<IUser>();
		List<IRole> rolesMentioned = new ArrayList<IRole>();
		if (words.contains("from")) {
			usersMentioned = event.getMessage().getMentions();
			rolesMentioned = event.getMessage().getRoleMentions();
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
				|| rolesMentioned.size() > 0) {
			FilterMessageDeletionJob job = FilterMessageDeletionJob.getDeletionJobForChannel(event.getChannel());
			job.setAge(date.toInstant());
			job.deleteByLength(0, true);
			if (usersMentioned.size() > 0 || rolesMentioned.size() > 0) {
				List<IUser> users = new ArrayList<IUser>();
				users.addAll(usersMentioned);
				for (IRole role : rolesMentioned) {
					for (IUser user : event.getGuild().getUsersByRole(role)) {
						if (!(users.contains(user))) {
							users.add(user);
						}
					}
				}
				job.deleteByUser(users, true);
			}
			SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
			final String dateString = dateFormatter.format(date.getTime());
			RequestBuffer.request(() -> event.getChannel().sendMessage("Finding messages older than " + dateString));
			job.startJob();
		} else {
			RequestBuffer.request(() -> event.getChannel().sendMessage(
					"Not sure what kind of calendar you are using,\n" + "but I cannot understand what you just said"));
		}
	}
}
