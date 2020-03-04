package com.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.io.IOException;
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

public class PermissionManagerCommand extends Command {

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
								"An error occurred when accessing the bot's guild permissions database. Please contact the bot owner to report this issue.")
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
