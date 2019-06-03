package main.IanSloat.thiccbot.commands;

import java.awt.Color;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.NSFWEngine;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Rule34Command extends Command{

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage("nsfw", user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "r34");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		TextChannel channel = event.getTextChannel();
		if(channel.isNSFW()) {
			EmbedBuilder response = new EmbedBuilder();
			String message = BotUtils.normalizeSentence(event.getMessage().getContentRaw());
			String[] words = message.split(" ");
			if(words.length > 2) {
				String search = "";
				for(int i = 2; i < words.length; i++) {
					search += words[i] + " ";
				}
				search = search.trim();
				search = search.replace(" ", "+");
				NSFWEngine r34 = new NSFWEngine();
				String imageUrl = r34.getR34ImageUrl(search, 100);
				if(!(imageUrl.equals(""))) {
					response.setTitle(":warning:NSFW:warning: R34 Image");
					response.addField("Search tags", search.replace("+", ", "), false);
					response.setImage(imageUrl);
					response.setFooter("Image requested by " + event.getAuthor().getName() + " | " + 
							new SimpleDateFormat("MM/dd/yyyy").format(Date.from(event.getMessage().getTimeCreated().toInstant()))
							, null);
					response.setColor(Color.ORANGE);
					channel.sendMessage(response.build()).queue();
				} else {
					response.setTitle("ERROR: No results found");
					response.setColor(Color.red);
					channel.sendMessage(response.build()).queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
				}
			} else {
				response.setTitle("ERROR: No search terms provided");
				response.setColor(Color.red);
				channel.sendMessage(response.build()).queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
			}
		} else {
			EmbedBuilder response = new EmbedBuilder();
			response.setTitle("ERROR: This is not an NSFW channel");
			response.setColor(Color.red);
			channel.sendMessage(response.build()).queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
		}
	}
	
}
