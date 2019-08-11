package main.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.tools.Wikisearch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WikiCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage("wiki", user);
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
				boolean isFound = search.search(searchCommand);
				if (isFound) {
					EmbedBuilder messageEdit = new EmbedBuilder();
					messageEdit.setAuthor("Wikipedia", null, "http://thiccbot.site/boticons/wikipedia.png");
					messageEdit.setTitle(search.getTitle());
					messageEdit.appendDescription("[Page Link](" + search.getPageUrl() + ")");
					messageEdit.addField("Summary", search.getSummary(), false);
					if (!(search.getThumbnailUrl().equals(""))) {
						messageEdit.setImage(search.getThumbnailUrl());
					}
					messageEdit.setColor(Color.GRAY);
					final MessageEmbed MessageSent1 = messageEdit.build();
					sent.editMessage(MessageSent1).queue();
				} else {
					EmbedBuilder messageEdit = new EmbedBuilder();
					messageEdit.setTitle("No results found.");
					messageEdit.setColor(Color.RED);
					final MessageEmbed MessageSent1 = messageEdit.build();
					sent.editMessage(MessageSent1).queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
				}
			}
		});
		
	}

}
