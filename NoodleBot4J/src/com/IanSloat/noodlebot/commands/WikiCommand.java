package com.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.io.FileUtils;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.NoodleBotMain;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;
import com.IanSloat.noodlebot.controllers.wikipedia.QueryResult;
import com.IanSloat.noodlebot.controllers.wikipedia.WikiResolver;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class WikiCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user) {
		try {
			return new GuildPermissionsController(user.getGuild()).canMemberUseCommand(user, this);
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "wiki");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();
		try {
			event.getMessage().delete().queue();
			String query = BotUtils
					.normalizeSentence(
							event.getMessage().getContentRaw().substring((BotUtils.BOT_PREFIX + "wiki").length()))
					.trim();
			if (!query.equals("")) {
				EmbedBuilder message = new EmbedBuilder();
				message.setTitle("Searching...");
				message.setColor(Color.cyan);
				event.getChannel().sendMessage(message.build()).queue(m -> {
					WikiResolver resolver = new WikiResolver();
					QueryResult queryResult = resolver.queryEndpoint(NoodleBotMain.wikipediaEndpoint, query);
					if (queryResult != null) {
						EmbedBuilder messageFinal = new EmbedBuilder();
						if (BotUtils.checkForElement(
								Arrays.asList(BotUtils
										.normalizeSentence((queryResult.getTitle() + " " + queryResult.getSummary())
												.replace('.', ' ').toLowerCase())
										.split(" ")),
								Arrays.asList(NoodleBotMain.badWords.toArray(new String[0])))
								&& !event.getTextChannel().isNSFW()) {
							messageFinal.setColor(Color.red);
							messageFinal.setTitle("Content filter warning");
							messageFinal.appendDescription(
									"The article you searched for was flagged by Noodlebot's content filter database. This means that it detected content in said article that may be considered inappropriate or explicit to some. Noodlebot is therefore unable to post the article in text channels that don't have nsfw content enabled. If you would still like to read the article, a link will be provided [here]("
											+ queryResult.getPageUrl() + ").");
							messageFinal.setFooter("Article requested by " + event.getAuthor().getAsTag() + " on "
									+ new SimpleDateFormat("MM/dd/yyyy")
											.format(Date.from(event.getMessage().getTimeCreated().toInstant())));
							m.editMessage(messageFinal.build()).queue();
						} else {
							messageFinal.setColor(NoodleBotMain.wikipediaEndpoint.getDisplayColor());
							messageFinal.setAuthor("Wikipedia", null,
									"https://www.dropbox.com/s/f577bax9vawwfz0/wikipedia.png?dl=1");
							messageFinal.setTitle(queryResult.getTitle(), queryResult.getPageUrl());
							messageFinal.setDescription(queryResult.getSummary());
							messageFinal.setFooter("Article requested by " + event.getAuthor().getAsTag() + " on "
									+ new SimpleDateFormat("MM/dd/yyyy")
											.format(Date.from(event.getMessage().getTimeCreated().toInstant())));
							if (!queryResult.getThumbnailUrl().equals("")) {
								try {
									try {
										File temp = File.createTempFile("nbtmp", null);
										FileUtils.copyURLToFile(new URL(queryResult.getThumbnailUrl()), temp);
										Dimension dim = Imaging.getImageSize(temp);
										temp.delete();
										double ratio = dim.getWidth() / dim.getHeight();
										if (ratio > 1.75) {
											messageFinal.setImage(queryResult.getThumbnailUrl());
										} else {
											messageFinal.setThumbnail(queryResult.getThumbnailUrl());
										}
									} catch (ImageReadException e) {
										e.printStackTrace();
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							m.editMessage(messageFinal.build()).queue();
						}
					} else
						m.editMessage(new EmbedBuilder().setTitle("No results found").setColor(Color.red).build())
								.queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
				});

			} else
				event.getChannel().sendMessage("You must specify a query string when using this command")
						.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
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
		return "**" + BotUtils.BOT_PREFIX + "wiki <query>** - Searches Wikipedia for an article _("
				+ BotUtils.BOT_PREFIX + "help " + getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "wiki";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.UTILITY;
	}

	@Override
	public MessageEmbed getCommandHelpPage() {
		// TODO Auto-generated method stub
		return null;
	}

}
