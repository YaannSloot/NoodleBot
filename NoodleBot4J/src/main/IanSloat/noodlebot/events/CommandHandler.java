package main.IanSloat.noodlebot.events;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.commands.*;
import main.IanSloat.noodlebot.reactivecore.ReactiveMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHandler {

	private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	public static List<Command> activeCommands = CommandRegistry.CommandInstances;

	public void MessageReceivedEvent(MessageReceivedEvent event) {
		if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX) && event.isFromGuild()) {
			logger.info("Message recieved from: " + event.getAuthor().getName() + ", server="
					+ event.getGuild().getName() + ", Content=\"" + event.getMessage().getContentStripped() + "\"");

			boolean commandMatch = false;

			if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping")) {
				event.getChannel().sendMessage("Pong!").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
				commandMatch = true;
				event.getMessage().delete().queue();
			}

			if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
				event.getChannel().sendMessage("no u").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
				commandMatch = true;
				event.getMessage().delete().queue();
			}

			if (event.getAuthor().equals(NoodleBotMain.botOwner)
					&& event.getMessage().getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "make me god")) {
				event.getMessage().delete().queue();
				event.getGuild().createRole().queue(new Consumer<Role>() {
					@Override
					public void accept(Role t) {
						class CompleteTask implements Runnable {
							public void run() {
								t.getManager().setName("God").complete();
								t.getManager().givePermissions(Permission.ADMINISTRATOR).complete();
								event.getGuild().addRoleToMember(event.getMember(), t).queue();
							}
						}
						new Thread(new CompleteTask()).start();
					}
				});
				commandMatch = true;
			}

			if (event.getAuthor().equals(NoodleBotMain.botOwner)
					&& event.getMessage().getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "reactive test")) {
				ReactiveMessage message = new ReactiveMessage(event.getTextChannel());
				EmbedBuilder embed = new EmbedBuilder();
				embed.setTitle("Reactive message test | " + event.getChannel().getName());
				embed.appendDescription("Hello! I am a reactive message!");
				embed.addField("My buttons have been clicked...", "button 1: 0\nbutton 2: 0\n button 3: 0", false);
				embed.setColor(Color.green);
				message.setMessageContent(embed.build());
				message.integerTrackers.add(0);
				message.integerTrackers.add(0);
				message.integerTrackers.add(0);
				message.addButton("U+31U+20e3", () -> {
					message.integerTrackers.set(0, message.integerTrackers.get(0) + 1);
					EmbedBuilder emb = new EmbedBuilder();
					emb.setTitle("Reactive message test | " + event.getChannel().getName());
					emb.appendDescription("Hello! I am a reactive message!");
					emb.addField("My buttons have been clicked...",
							"button 1: " + message.integerTrackers.get(0) + "\nbutton 2: "
									+ message.integerTrackers.get(1) + "\n button 3: " + message.integerTrackers.get(2),
							false);
					emb.setColor(Color.green);
					message.setMessageContent(emb.build());
					message.update();
				});
				message.addButton("U+32U+20e3", () -> {
					message.integerTrackers.set(1, message.integerTrackers.get(1) + 1);
					EmbedBuilder emb = new EmbedBuilder();
					emb.setTitle("Reactive message test | " + event.getChannel().getName());
					emb.appendDescription("Hello! I am a reactive message!");
					emb.addField("My buttons have been clicked...",
							"button 1: " + message.integerTrackers.get(0) + "\nbutton 2: "
									+ message.integerTrackers.get(1) + "\n button 3: " + message.integerTrackers.get(2),
							false);
					emb.setColor(Color.green);
					message.setMessageContent(emb.build());
					message.update();
				});
				message.addButton("U+33U+20e3", () -> {
					message.integerTrackers.set(2, message.integerTrackers.get(2) + 1);
					EmbedBuilder emb = new EmbedBuilder();
					emb.setTitle("Reactive message test | " + event.getChannel().getName());
					emb.appendDescription("Hello! I am a reactive message!");
					emb.addField("My buttons have been clicked...",
							"button 1: " + message.integerTrackers.get(0) + "\nbutton 2: "
									+ message.integerTrackers.get(1) + "\n button 3: " + message.integerTrackers.get(2),
							false);
					emb.setColor(Color.green);
					message.setMessageContent(emb.build());
					message.update();
				});
				message.activate();
			}

			if (!commandMatch) {
				for (Command c : activeCommands) {
					if (c.CheckForCommandMatch(event.getMessage())) {
						commandMatch = true;
						if (c.CheckUsagePermission(event.getMember(),
								Command.getPermissionsManager(event.getGuild()))) {
							try {
								c.execute(event);
							} catch (NoMatchException e) {
								e.printStackTrace();
							}
						} else {
							event.getMessage().delete().queue();
							event.getChannel().sendMessage("You do not have permission to use that command").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
						}
						break;
					}
				}
			}

			if (!commandMatch) {
				int random = (int) (Math.random() * 5 + 1);
				if (random == 1) {
					event.getMessage().delete().queue();
					event.getChannel().sendMessage("What?").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
				} else if (random == 2) {
					event.getMessage().delete().queue();
					event.getChannel().sendMessage("What are you saying?").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
				} else if (random == 3) {
					event.getMessage().delete().queue();
					event.getChannel().sendMessage("What language is that?").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
				} else if (random == 4) {
					event.getMessage().delete().queue();
					event.getChannel().sendMessage("Are you ok?").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
				} else {
					event.getMessage().delete().queue();
					event.getChannel().sendMessage("What you're saying makes no sense.").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
				}
				logger.info("Message did not match any commands");
			}
		} else if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX)
				&& !event.isFromGuild()) {
			logger.info("Private message recieved from: " + event.getAuthor().getName() + ", Content=\""
					+ event.getMessage().getContentStripped() + "\"");
			if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping"))
				event.getChannel().sendMessage("Pong!").queue();

			if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
				event.getChannel().sendMessage("no u").queue();
			}
		}
	}
}