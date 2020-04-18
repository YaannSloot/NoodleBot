package com.IanSloat.noodlebot.commands;

import java.awt.Color; 
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.NoodleBotMain;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermission;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermission.PermissionValue;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissions;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;
import com.IanSloat.noodlebot.events.CommandController;
import com.IanSloat.noodlebot.tools.RoleUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class PermissionManagerCommand implements Command {

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
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "permmngr");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();
		try {
			event.getMessage().delete().queue();
			List<String> words = new ArrayList<String>();
			words.addAll(Arrays
					.asList(BotUtils.normalizeSentence(event.getMessage().getContentRaw().toLowerCase()).split(" ")));
			int modeflag = -1;
			switch (words.get(2)) {
			case "set":
				modeflag = 0;
				break;
			case "-s":
				modeflag = 0;
				break;
			case "test":
				modeflag = 1;
				break;
			case "-t":
				modeflag = 1;
				break;
			case "get":
				modeflag = 2;
				break;
			case "-g":
				modeflag = 2;
				break;
			case "-r":
				modeflag = 3;
				break;
			case "remove":
				modeflag = 3;
				break;
			}
			if (modeflag > -1) {
				switch (modeflag) {
				case 0:
					String target = "";
					PermissionValue permVal;
					List<Member> targetUsers = event.getMessage().getMentionedMembers();
					List<Role> targetRoles = new ArrayList<Role>();
					targetRoles.addAll(event.getMessage().getMentionedRoles());
					if (event.getMessage().mentionsEveryone())
						targetRoles.add(event.getGuild().getPublicRole());
					String command = event.getMessage().getContentRaw();
					for (Member m : targetUsers) {
						command = command.replace(m.getAsMention(), "");
					}
					for (Role r : targetRoles) {
						command = command.replace(r.getAsMention(), "");
					}
					words = new ArrayList<String>();
					words.addAll(Arrays.asList(BotUtils.normalizeSentence(command.toLowerCase()).split(" ")));
					targetUsers = targetUsers.stream().filter(m -> RoleUtils.isMemberHigherThan(event.getMember(), m))
							.collect(Collectors.toList());
					targetRoles = targetRoles.stream().filter(
							r -> RoleUtils.isRoleHigherThan(RoleUtils.getMemberHighestRole(event.getMember()), r))
							.collect(Collectors.toList());
					try {
						GuildPermissionsController controller = new GuildPermissionsController(event.getGuild());
						if (words.size() == 5) {
							words.removeAll(words.subList(0, 2));
							if ((targetUsers.size() + targetRoles.size()) > 0) {
								if (words.contains("allow"))
									permVal = PermissionValue.ALLOW;
								else
									permVal = PermissionValue.DENY;
								List<String> commandIds = new ArrayList<String>();
								CommandController.commandList.forEach(c -> commandIds.add(c.getCommandId()));
								for (CommandCategory ct : CommandCategory.values()) {
									commandIds.add(ct.toString());
								}
								for (String id : commandIds) {
									if (words.contains(id)) {
										target = id;
										break;
									}
								}
								if (permVal != null && !target.equals("")) {
									GuildPermission perm = controller.getPermission(target);
									if (perm == null)
										perm = new GuildPermission(target, new HashMap<>(), new HashMap<>());
									for (Member m : targetUsers) {
										perm.setUserEntry(m, permVal);
									}
									for (Role r : targetRoles) {
										perm.setRoleEntry(r, permVal);
									}
									controller.setPermission(perm);
									controller.writePermissions();
									String output = "";
									if (targetRoles.size() > 0)
										output = targetRoles.size() + " role(s)";
									if (targetUsers.size() > 0) {
										if (output.equals(""))
											output = targetUsers.size() + " user(s)";
										else
											output = targetUsers.size() + " user(s) and " + output;
									}
									event.getChannel().sendMessage("Permission settings were modified for " + output)
											.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
								} else
									event.getChannel().sendMessage(
											"Invalid syntax. Please reference help page for info on how to use this command.")
											.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
							} else if ((event.getMessage().getMentionedMembers().size()
									+ event.getMessage().getMentionedRoles().size()) > 0)
								event.getChannel().sendMessage(
										"The members and/or roles mentioned are all in a higher position than you and cannot have their permissions modified")
										.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
							else
								event.getChannel()
										.sendMessage("You must mention at least one user/role to perform this action")
										.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
						} else if (words.size() == 4) {
							if (words.get(3).equals("default")) {
								if (event.getMember().isOwner() || event.getAuthor().equals(NoodleBotMain.botOwner)) {
									controller.getPermissions().forEach(p -> controller.removePermission(p));
									controller.writePermissions();
									GuildPermissionsController.initGuildPermissionsFiles(event.getGuild());
									event.getChannel().sendMessage("Permission settings are now set to defaults")
											.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
								} else
									event.getChannel().sendMessage("Only the server owner can reset permissions")
											.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
							} else
								event.getChannel().sendMessage(
										"Invalid syntax. Please reference help page for info on how to use this command.")
										.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
						} else
							event.getChannel().sendMessage(
									"Invalid syntax. Please reference help page for info on how to use this command.")
									.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					} catch (IOException e) {
						event.getChannel().sendMessage(
								"An error occurred while accessing the bot's guild permissions database. Please contact the bot owner to report this issue.")
								.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					}
					break;
				case 1:

					break;
				case 2:
					List<Member> targetUsers2 = event.getMessage().getMentionedMembers();
					List<Role> targetRoles2 = new ArrayList<Role>();
					targetRoles2.addAll(event.getMessage().getMentionedRoles());
					if (event.getMessage().mentionsEveryone())
						targetRoles2.add(event.getGuild().getPublicRole());
					String command2 = event.getMessage().getContentRaw();
					for (Member m : targetUsers2) {
						command2 = command2.replace(m.getAsMention(), "");
					}
					for (Role r : targetRoles2) {
						command2 = command2.replace(r.getAsMention(), "");
					}
					words = new ArrayList<String>();
					words.addAll(Arrays.asList(BotUtils.normalizeSentence(command2.toLowerCase()).split(" ")));
					try {
						GuildPermissionsController controller = new GuildPermissionsController(event.getGuild());
						if (words.size() == 4) {
							if (words.get(3).equals("all")) {
								String fileExport = "";
								fileExport += "Permission Settings Report | " + event.getGuild().getName()
										+ "\nRequested "
										+ new SimpleDateFormat("MM/dd/yyyy 'at' hh:mm a")
												.format(Date.from(event.getMessage().getTimeCreated().toInstant()))
										+ "\n\nCurrent Settings:\n";
								GuildPermissions perms = controller.getPermissions();
								String[] Categories = new String[CommandCategory.values().length - 1];
								for (int i = 0; i < Categories.length; i++) {
									Categories[i] = CommandCategory.values()[i + 1].toString();
								}
								for (String ct : Categories) {
									fileExport += "    Category \"" + ct + "\":\n";
									if (perms.contains(ct)) {
										GuildPermission ctPerm = perms.retrieveByKey(ct);
										if (ctPerm.getUserEntries().size() > 0 || ctPerm.getRoleEntries().size() > 0) {
											fileExport += "        Global Permissions:\n";
											if (ctPerm.getUserEntries().size() > 0) {
												fileExport += "            Users:\n";
												for (String user : ctPerm.getUserEntries().keySet()) {
													fileExport += "                ("
															+ ctPerm.getUserEntry(event.getGuild().getMemberById(user))
																	.toString().toUpperCase()
															+ ") "
															+ event.getGuild().getMemberById(user).getUser().getName()
															+ "(id:" + user + ")";
													if (event.getGuild().getMemberById(user).getNickname() != null)
														fileExport += " aka \""
																+ event.getGuild().getMemberById(user).getNickname()
																+ "\"\n";
													else
														fileExport += "\n";
												}
											}
											if (ctPerm.getRoleEntries().size() > 0) {
												fileExport += "            Roles:\n";
												for (String role : ctPerm.getRoleEntries().keySet()) {
													fileExport += "                ("
															+ ctPerm.getRoleEntry(event.getGuild().getRoleById(role))
																	.toString().toUpperCase()
															+ ") " + event.getGuild().getRoleById(role).getName()
															+ "(id:" + role + ")\n";
												}
											}
										} else
											fileExport += "        Global permissions list found but contains no entries\n";
									} else
										fileExport += "        No global permissions found for this category\n";
									boolean noneFound = true;
									for (GuildPermission perm : perms) {
										for (Command cmd : CommandController.commandList) {
											if (perm.getKey().equals(cmd.getCommandId())
													&& cmd.getCommandCategory().toString().equals(ct)) {
												noneFound = false;
												
												break;
											}
										}
									}
									if (noneFound)
										fileExport += "        No command-specific permissions found for this category\n";
								}
								fileExport += "\nRaw JSON data:\n";
								fileExport += controller.getRawCopy().toString(1);
								byte[] payload = new byte[fileExport.length()];
								for(int b = 0; b < fileExport.length(); b++) {
									payload[b] = (byte) fileExport.toCharArray()[b];
								}
								event.getAuthor().openPrivateChannel()
										.queue(c -> c.sendFile(payload, "Permission_Settings_Report.txt").queue());
							} else
								event.getChannel().sendMessage(
										"Invalid syntax. Please reference help page for info on how to use this command.")
										.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
						}
					} catch (IOException e) {
						event.getChannel().sendMessage(
								"An error occurred while accessing the bot's guild permissions database. Please contact the bot owner to report this issue.")
								.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
					}
					break;
				case 3:

					break;
				}
			} else
				event.getChannel()
						.sendMessage("Invalid syntax. Please reference help page for info on how to use this command.")
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
		return "**" + BotUtils.BOT_PREFIX
				+ "permmngr <mode flag> <args...>** - Used to view and manage permission settings for your guild _("
				+ BotUtils.BOT_PREFIX + "help " + getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "permmngr";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.MANAGEMENT;
	}

	@Override
	public MessageEmbed getCommandHelpPage() {
		// TODO Auto-generated method stub
		return null;
	}

}
