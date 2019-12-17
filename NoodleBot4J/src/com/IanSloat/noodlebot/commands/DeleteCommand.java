package com.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.controllers.GuildPermissionsController;
import com.IanSloat.noodlebot.reactivecore.ReactiveMessage;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

// TODO Fix additional error conditions not being present as well as negative number bug
/**
 * Command for deleting messages in bulk. Causes severe ratelimiting for the
 * specific guild its operating in until it completes.
 */
public class DeleteCommand extends Command {

	private static final Map<Guild, ReactiveMessage> existingReactives = new HashMap<>();
	private static final Map<Guild, ExecutorService> existingQueues = new HashMap<>();

	// Creates a new executor for a specific guild
	private static synchronized ExecutorService getQueueForGuild(Guild guild) {
		ExecutorService result = existingQueues.get(guild);
		if (result == null) {
			result = Executors.newFixedThreadPool(1);
			existingQueues.put(guild, result);
		}
		return result;
	}

	@Override
	public boolean CheckUsagePermission(Member user) {
		return new GuildPermissionsController(user.getGuild()).canMemberUseCommand(user, this);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return (command.getContentRaw().startsWith(BotUtils.BOT_PREFIX + "delete messages")
				|| command.getContentRaw().equals(BotUtils.BOT_PREFIX + "stop deleting")) ? true : false;
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();

		try {
			if (event.getMessage().getContentRaw().startsWith(BotUtils.BOT_PREFIX + "delete messages")) {
				event.getMessage().delete().queue();
				String ageString = event.getMessage().getContentRaw().toLowerCase()
						.replace(BotUtils.BOT_PREFIX + "delete messages older than ", "");
				ageString = ageString.replace(',', ' ');
				ageString = BotUtils.normalizeSentence(ageString);
				List<String> words = Arrays.asList(ageString.split(" ")).stream()
						.map(w -> (Arrays.asList("day", "week", "month", "year").contains(w)) ? w += 's' : w)
						.collect(Collectors.toList());
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
				}
				if (days > 0 || weeks > 0 || months > 0 || years > 0 || usersMentioned.size() > 0
						|| rolesMentioned.size() > 0 || event.getMessage().mentionsEveryone()) {
					OffsetDateTime messageAge = date.toInstant()
							.atOffset(event.getMessage().getTimeCreated().getOffset());
					List<Member> users = new ArrayList<Member>();
					if (usersMentioned.size() > 0 || rolesMentioned.size() > 0
							|| event.getMessage().mentionsEveryone()) {
						users.addAll(usersMentioned);
						if (event.getMessage().mentionsEveryone())
							users.addAll(event.getGuild().getMembers());
						for (Role role : rolesMentioned) {
							for (Member user : event.getGuild().getMembersWithRoles(role)) {
								if (!(users.contains(user))) {
									users.add(user);
								}
							}
						}
					}
					if (existingQueues.get(event.getGuild()) != null) {
						if (existingQueues.get(event.getGuild()).shutdownNow().size() > 0)
							event.getChannel().sendMessage("Previous job was cancelled")
									.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
						existingQueues.replace(event.getGuild(), Executors.newFixedThreadPool(1));
						if (existingReactives.get(event.getGuild()) != null) {
							existingReactives.get(event.getGuild()).dispose();
							existingReactives.remove(event.getGuild());
						}
					}
					final String dateString = new SimpleDateFormat("MM/dd/yyyy").format(date.getTime());
					event.getChannel().sendMessage("Finding messages older than " + dateString)
							.queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
					List<Message> history;
					Stream<Message> HistoryIterator = event.getChannel().getIterableHistory().stream().limit(2000);
					HistoryIterator = HistoryIterator.filter(m -> m.getTimeCreated().isBefore(messageAge));

					if (users.size() > 0)
						HistoryIterator = HistoryIterator.filter(m -> isMessageWrittenByTargets(m, users));

					history = HistoryIterator.collect(Collectors.toList());
					if (history.size() > 0) {
						if (history.size() == 2000) {
							event.getChannel()
									.sendMessage("2000 messages were retrieved."
											+ "This may mean that this channel's history is longer than 2000 messages."
											+ "\nThis command can only handle 2000 messages at a time."
											+ "\nTo delete more messages, run this command again when it finishes")
									.queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
						}

						EmbedBuilder status = new EmbedBuilder();
						status.setTitle("Message deletion job | #" + event.getChannel().getName() + "@"
								+ event.getGuild().getName());
						status.addField("Deleting messages.", "This may take a while...", false);
						status.addField("Status", "0/" + history.size() + " messages deleted", false);
						status.setColor(Color.CYAN);

						ReactiveMessage message = new ReactiveMessage(event.getTextChannel());

						message.setMessageContent(status.build());

						ExecutorService taskQueue = getQueueForGuild(event.getGuild());

						message.addButton("U+274c", () -> {
							taskQueue.shutdownNow();
							existingQueues.replace(event.getGuild(), Executors.newFixedThreadPool(1));
							message.dispose();
						});

						final int count = history.size();

						message.activate(m -> {
							existingReactives.put(event.getGuild(), message);
							for (Message t : history) {
								taskQueue.submit(() -> {
									t.delete().complete();
									history.remove(t);
									EmbedBuilder stat = new EmbedBuilder();
									stat.setTitle("Message deletion job | #" + event.getChannel().getName() + "@"
											+ event.getGuild().getName());
									stat.addField("Deleting messages.", "This may take a while...", false);
									stat.addField("Status",
											+(count - history.size()) + "/" + count + " messages deleted", false);
									stat.setColor(Color.CYAN);
									m.editMessage(stat.build()).complete();
								});
							}
							taskQueue.submit(() -> {
								try {
									Thread.sleep(5000);
									message.dispose();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							});
						});

					}

				} else {
					event.getChannel().sendMessage("Age string provided is invalid")
							.queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
				}
			} else if (event.getMessage().getContentRaw().equals(BotUtils.BOT_PREFIX + "stop deleting")) {
				event.getMessage().delete().queue();
				if (existingQueues.get(event.getGuild()) != null) {
					if (existingQueues.get(event.getGuild()).shutdownNow().size() > 0)
						event.getChannel().sendMessage("Previous job was cancelled")
								.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					else
						event.getChannel().sendMessage("No jobs are running")
								.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					if (existingReactives.get(event.getGuild()) != null) {
						existingReactives.get(event.getGuild()).dispose();
						existingReactives.remove(event.getGuild());
					}
				}
			} else {
				event.getChannel().sendMessage("Too few arguements provided")
						.queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		} catch (InsufficientPermissionException e) {
			String permission = e.getPermission().getName();
			EmbedBuilder message = new EmbedBuilder();
			message.setTitle("Missing permission error | " + event.getGuild().getName());
			message.addField("Error message:", "Bot is missing required permission **" + permission
					+ "**. Please grant this permission to the bot's role or contact a guild administrator to apply this permission to the bot's role.",
					false);
			message.setColor(Color.red);
			event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(message.build()).queue());
			// TODO Add event for logger when it is complete
		}

	}

	@Override
	public String getHelpSnippet() {
		return "**" + BotUtils.BOT_PREFIX + "delete messages** - Deletes messages in bulk _(" + BotUtils.BOT_PREFIX
				+ " help " + getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "delete";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.MANAGEMENT;
	}

	private boolean isMessageWrittenByTargets(Message m, List<Member> targets) {
		boolean result = false;
		for (Member u : targets) {
			if (m.getAuthor().equals(u.getUser())) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public MessageEmbed getCommandHelpPage() {
		return new EmbedBuilder().setTitle("Bulk message delete command | More Info").setColor(Color.red)
				.setDescription(
						"**Syntax:**\n_nood delete messages_ (from <@ mentions>) (older than <time unit> <amount>)"
								+ "\n\n**Summary:**"
								+ "\nThis command deletes up to 2000 messages in bulk. Target messages can be restricted to both specific author and specific message age. The bulk deletion job can be canceled at any time either via the red X reaction or by typing \"nood stop deleting\"\n"
								+ "\n**Parameters:**"
								+ "\n@ mentions - Both users and roles allowed. Can be used to restrict the pre-job message scan to certain message authors."
								+ "\nExample:"
								+ "\nnood delete messages from @someuser @somerole - will delete messages from both @someuser and anyone with the @somerole role."
								+ "\n\nAge value - Specified by \"<time unit> <amount>\". Can specify multiple time unit/amount pairs for precise age."
								+ "\nValid time unit strings - day(s), week(s), month(s), year(s)" + "\nExample:"
								+ "\nnood delete messages older than 1 week 5 days - will delete messages older than 1 week 5 days")
				.build();
	}

}
