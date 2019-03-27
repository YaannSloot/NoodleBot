package main.IanSloat.thiccbot.commands;

import java.awt.Color;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.Wikisearch;
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
		try {
			EmbedBuilder message = new EmbedBuilder();
			Wikisearch search = new Wikisearch();
			String command = event.getMessage().getContentStripped();
			command = BotUtils.normalizeSentence(command);
			command = command.substring((BotUtils.BOT_PREFIX + "wiki").length());
			message.setTitle("Searching...");
			message.setColor(Color.GREEN);
			final MessageEmbed MessageSent = message.build();
			Message sent = event.getChannel().sendMessage(MessageSent).submit().get();
			boolean isFound = search.search(command);
			if (isFound) {
				message = new EmbedBuilder();
				message.setAuthor("Wikipedia", null, "http://thiccbot.site/boticons/wikipedia.png");
				message.setTitle(search.getTitle());
				message.appendDescription("[Page Link](" + search.getPageUrl() + ")");
				message.addField("Summary", search.getSummary(), false);
				if (!(search.getThumbnailUrl().equals(""))) {
					message.setImage(search.getThumbnailUrl());
				}
				message.setColor(Color.GRAY);
				final MessageEmbed MessageSent1 = message.build();
				sent.editMessage(MessageSent1).queue();
			} else {
				message = new EmbedBuilder();
				message.setTitle("No results found.");
				message.setColor(Color.RED);
				final MessageEmbed MessageSent1 = message.build();
				sent.editMessage(MessageSent1).queue();
				sent.delete().queueAfter(5, TimeUnit.SECONDS);
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
