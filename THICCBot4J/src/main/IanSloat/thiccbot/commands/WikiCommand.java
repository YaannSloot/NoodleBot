package main.IanSloat.thiccbot.commands;

import java.awt.Color;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.threadbox.MessageDeleteTools;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.Wikisearch;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class WikiCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage("wiki", user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "wiki ");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		EmbedBuilder message = new EmbedBuilder();
		Wikisearch search = new Wikisearch();
		String command = event.getMessage().getContent();
		command = BotUtils.normalizeSentence(command);
		command = command.substring((BotUtils.BOT_PREFIX + "wiki").length());
		message.withTitle("Searching...");
		message.withColor(Color.GREEN);
		final EmbedObject MessageSent = message.build();
		IMessage sent = RequestBuffer.request(() -> {
			return event.getChannel().sendMessage(MessageSent);
		}).get();
		boolean isFound = search.search(command);
		if(isFound) {
			message = new EmbedBuilder();
			message.withAuthorName("Wikipedia");
			message.withAuthorIcon("http://thiccbot.site/boticons/wikipedia.png");
			message.withTitle(search.getTitle());
			message.appendDesc("[Page Link](" + search.getPageUrl() + ")");
			message.appendField("Summary", search.getSummary(), false);
			if(!(search.getThumbnailUrl().equals(""))) {
				message.withImage(search.getThumbnailUrl());
			}
			message.withColor(Color.GRAY);
			final EmbedObject MessageSent1 = message.build();
			RequestBuffer.request(() -> {
				sent.edit(MessageSent1);
			});
		} else {
			message = new EmbedBuilder();
			message.withTitle("No results found.");
			message.withColor(Color.RED);
			final EmbedObject MessageSent1 = message.build();
			RequestBuffer.request(() -> {
				sent.edit(MessageSent1);
			});
			MessageDeleteTools.DeleteAfterMillis(sent, 5000);
		}
		
	}

}
