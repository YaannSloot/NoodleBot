package main.IanSloat.noodlebot.events;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.commands.*;
import main.IanSloat.noodlebot.reactivecore.ReactiveMessage;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.utils.AttachmentOption;

public class CommandHandler {

	private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	public static List<Command> activeCommands = CommandRegistry.CommandInstances;

	public void MessageReceivedEvent(MessageReceivedEvent event) {
		if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX) && event.isFromGuild()) {
			logger.info("Message recieved from: " + event.getAuthor().getName() + ", server="
					+ event.getGuild().getName() + ", Content=\"" + event.getMessage().getContentStripped() + "\"");

			boolean commandMatch = false;

			if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping")) {
				event.getChannel().sendMessage("Pong!")
						.queue(msg -> BotUtils.messageSafeDelete(msg, 5, TimeUnit.SECONDS));
				commandMatch = true;
				BotUtils.messageSafeDelete(event.getMessage());
			}
			
			if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
				event.getChannel().sendMessage("no u")
						.queue(msg -> BotUtils.messageSafeDelete(msg, 5, TimeUnit.SECONDS));
				commandMatch = true;
				BotUtils.messageSafeDelete(event.getMessage());
			}

			// Owner-only test commands
			if (event.getAuthor().equals(NoodleBotMain.botOwner)) {

				// Force owner to have admin perms (requires bot to have admin role)
				if (event.getMessage().getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "make me god")) {
					BotUtils.messageSafeDelete(event.getMessage());
					try {
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
					} catch (InsufficientPermissionException e) {
						logger.warn("A role attempted to be created but failed due to a lack of the \"" + e.getPermission().getName() + "\" permission");
					}
					commandMatch = true;
				}

				// Test the reactive message system
				else if (event.getMessage().getContentRaw().toLowerCase()
						.equals(BotUtils.BOT_PREFIX + "reactive test")) {
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
										+ message.integerTrackers.get(1) + "\n button 3: "
										+ message.integerTrackers.get(2),
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
										+ message.integerTrackers.get(1) + "\n button 3: "
										+ message.integerTrackers.get(2),
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
										+ message.integerTrackers.get(1) + "\n button 3: "
										+ message.integerTrackers.get(2),
								false);
						emb.setColor(Color.green);
						message.setMessageContent(emb.build());
						message.update();
					});
					message.activate();
					commandMatch = true;
				}

				// Test channel visibility
				else if (event.getMessage().getContentRaw().toLowerCase()
						.equals(BotUtils.BOT_PREFIX + "channel perm test")) {

					List<TextChannel> textChannels = event.getGuild().getTextChannels();
					List<VoiceChannel> voiceChannels = event.getGuild().getVoiceChannels();
					TextChannel defaultChannel = event.getGuild().getDefaultChannel();
					TextChannel outputChannel;
					int totalChannels = textChannels.size() + voiceChannels.size();
					int numberViewable = textChannels.stream()
							.filter(c -> event.getGuild().getSelfMember().hasPermission((GuildChannel) c,
									Permission.VIEW_CHANNEL))
							.collect(Collectors.toList()).size()
							+ voiceChannels
									.stream().filter(c -> event.getGuild().getSelfMember()
											.hasPermission((GuildChannel) c, Permission.VIEW_CHANNEL))
									.collect(Collectors.toList()).size();

					if (event.getGuild().getSelfMember().hasPermission((GuildChannel) event.getChannel(),
							Permission.MESSAGE_WRITE)) {
						outputChannel = event.getTextChannel();
					} else if (event.getGuild().getSelfMember().hasPermission((GuildChannel) defaultChannel,
							Permission.MESSAGE_WRITE)) {
						outputChannel = defaultChannel;
					} else {
						outputChannel = (TextChannel) event.getAuthor().openPrivateChannel().complete();
					}

					String defaultName = "";
					if (defaultChannel == null) {
						defaultName = "No channels are viewable by default";
					} else {
						defaultName = defaultChannel.getName() + " (id:" + defaultChannel.getId() + ")";
					}

					EmbedBuilder message = new EmbedBuilder();
					message.setTitle("Channel permission statistics | " + event.getGuild().getName());
					message.addField("Statistics summary:",
							"Total channels: " + totalChannels + "\n" + "Total bot can view: " + numberViewable + "\n"
									+ "Default text channel: " + defaultName + "\n" + "Total text channels: "
									+ textChannels.size() + "\n" + "Total voice channels: " + voiceChannels.size(),
							false);
					outputChannel.sendMessage(message.build()).queue();
					GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
					File settingsDirectory = setMgr.getSettingsDirectory();
					try {
						File tempFile = File.createTempFile("GuildChStats", ".txt", settingsDirectory);
						if (tempFile.exists()) {
							FileWriter fileOut = new FileWriter(tempFile);
							fileOut.write("Channel permission statistics | " + event.getGuild().getName() + "\n\n");
							fileOut.write("Statistics summary:\n");
							fileOut.write("Total channels: " + totalChannels + "\n");
							fileOut.write("Total bot can view: " + numberViewable + "\n");
							fileOut.write("Default text channel: " + defaultName + "\n");
							fileOut.write("Total text channels: " + textChannels.size() + "\n");
							fileOut.write("Total voice channels: " + voiceChannels.size() + "\n\nText channels:\n");
							for (TextChannel c : textChannels) {
								String line = c.getName() + " (id:" + c.getId() + ")";
								if (!event.getGuild().getSelfMember().hasPermission((GuildChannel) c,
										Permission.VIEW_CHANNEL)) {
									line = "(HIDDEN) " + line;
								}
								line += '\n';
								fileOut.write(line);
							}
							fileOut.write("\nVoice channels:\n");
							for (VoiceChannel c : voiceChannels) {
								String line = c.getName() + " (id:" + c.getId() + ")";
								if (!event.getGuild().getSelfMember().hasPermission((GuildChannel) c,
										Permission.VIEW_CHANNEL)) {
									line = "(HIDDEN) " + line;
								}
								line += '\n';
								fileOut.write(line);
							}
							fileOut.close();
							outputChannel.sendFile(tempFile, "GuildChannelStats.txt").complete();
							tempFile.delete();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					commandMatch = true;
				}
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
							BotUtils.messageSafeDelete(event.getMessage());
							event.getChannel().sendMessage("You do not have permission to use that command")
									.queue(msg -> BotUtils.messageSafeDelete(msg, 5, TimeUnit.SECONDS));
						}
						break;
					}
				}
			}

			if (!commandMatch) {
				int random = (int) (Math.random() * 5 + 1);
				if (random == 1) {
					BotUtils.messageSafeDelete(event.getMessage());
					event.getChannel().sendMessage("What?")
							.queue(msg -> BotUtils.messageSafeDelete(msg, 5, TimeUnit.SECONDS));
				} else if (random == 2) {
					BotUtils.messageSafeDelete(event.getMessage());
					event.getChannel().sendMessage("What are you saying?")
							.queue(msg -> BotUtils.messageSafeDelete(msg, 5, TimeUnit.SECONDS));
				} else if (random == 3) {
					BotUtils.messageSafeDelete(event.getMessage());
					event.getChannel().sendMessage("What language is that?")
							.queue(msg -> BotUtils.messageSafeDelete(msg, 5, TimeUnit.SECONDS));
				} else if (random == 4) {
					BotUtils.messageSafeDelete(event.getMessage());
					event.getChannel().sendMessage("Are you ok?")
							.queue(msg -> BotUtils.messageSafeDelete(msg, 5, TimeUnit.SECONDS));
				} else {
					BotUtils.messageSafeDelete(event.getMessage());
					event.getChannel().sendMessage("What you're saying makes no sense.")
							.queue(msg -> BotUtils.messageSafeDelete(msg, 5, TimeUnit.SECONDS));
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