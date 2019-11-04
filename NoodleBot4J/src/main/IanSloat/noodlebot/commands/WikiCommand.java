package main.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.jdaevents.GenericCommandErrorEvent;
import main.IanSloat.noodlebot.jdaevents.GenericCommandEvent;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.Wikisearch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class WikiCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "wiki ");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		NoodleBotMain.eventListener.onEvent(new GenericCommandEvent(event.getJDA(), event.getResponseNumber(),
				event.getGuild(), this, event.getMessage().getContentRaw().toLowerCase(), event.getMember()));
		try {
			event.getMessage().delete().queue();
			EmbedBuilder message = new EmbedBuilder();
			Wikisearch search = new Wikisearch();

			String command = event.getMessage().getContentStripped();
			command = BotUtils.normalizeSentence(command);
			command = command.substring((BotUtils.BOT_PREFIX + "wiki").length());
			message.setTitle("Searching...");
			message.setColor(Color.GREEN);
			final MessageEmbed MessageSent = message.build();
			final String searchCommand = command;
			event.getChannel().sendMessage(MessageSent).queue(new Consumer<Message>() {

				@Override
				public void accept(Message sent) {
					boolean isFound = false;
					if (event.getTextChannel().isNSFW()) {
						isFound = search.search(searchCommand);
					} else {
						isFound = search.search(searchCommand, true);
					}
					if (isFound) {
						if (search.isNSFW() && !event.getTextChannel().isNSFW()) {
							EmbedBuilder messageEdit = new EmbedBuilder();
							messageEdit.setTitle("NSFW Article Warning");
							messageEdit.appendDescription(
									"The article you searched for contains nsfw content. A full article summary may not be appropriate to display in this current channel. If you would like to see the full article summary, please use the same command again in an nsfw enabled channel. An article link will still be provided [here]("
											+ search.getPageUrl() + ") if you wish to read it.");
							messageEdit.setColor(Color.red);
							final MessageEmbed MessageSent1 = messageEdit.build();
							sent.editMessage(MessageSent1).queue();
						} else {
							EmbedBuilder messageEdit = new EmbedBuilder();
							messageEdit.setAuthor("Wikipedia", null, "https://www.dropbox.com/s/f577bax9vawwfz0/wikipedia.png?dl=1");
							messageEdit.setTitle(search.getTitle());
							messageEdit.appendDescription("[Page Link](" + search.getPageUrl() + ")");
							messageEdit.addField("Summary", search.getSummary(), false);
							if (!(search.getThumbnailUrl().equals(""))) {
								messageEdit.setImage(search.getThumbnailUrl());
							}
							messageEdit.setColor(Color.GRAY);
							final MessageEmbed MessageSent1 = messageEdit.build();
							sent.editMessage(MessageSent1).queue();
						}
					} else {
						EmbedBuilder messageEdit = new EmbedBuilder();
						messageEdit.setTitle("No results found.");
						messageEdit.setColor(Color.RED);
						final MessageEmbed MessageSent1 = messageEdit.build();
						sent.editMessage(MessageSent1)
								.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
					}
				}
			});
		} catch (InsufficientPermissionException e) {
			String permission = e.getPermission().getName();
			EmbedBuilder message = new EmbedBuilder();
			message.setTitle("Missing permission error | " + event.getGuild().getName());
			message.addField("Error message:", "Bot is missing required permission **" + permission
					+ "**. Please grant this permission to the bot's role or contact a guild administrator to apply this permission to the bot's role.",
					false);
			message.setColor(Color.red);
			event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(message.build()).queue());
			NoodleBotMain.eventListener.onEvent(new GenericCommandErrorEvent(event.getJDA(),
					event.getResponseNumber(), event.getGuild(), this, event.getMessage().getContentRaw(),
					event.getMember(), "Command execution failed due to missing permission: " + permission));
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood wiki** - Looks up an article on Wikipedia";
	}

	@Override
	public String getCommandId() {
		return "wiki";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_UTILITY;
	}

}
