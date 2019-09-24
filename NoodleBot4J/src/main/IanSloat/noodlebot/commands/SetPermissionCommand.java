package main.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.tools.HierarchyUtils;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class SetPermissionCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "permission");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		try {
			event.getMessage().delete().queue();
			if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				String command = BotUtils.normalizeSentence(event.getMessage().getContentStripped().toLowerCase()
						.substring((BotUtils.BOT_PREFIX + "permission").length()));
				String[] words = command.split(" ");
				List<Member> users = new ArrayList<Member>();
				users.addAll(event.getMessage().getMentionedMembers());
				List<Role> roles = new ArrayList<Role>();
				roles.addAll(event.getMessage().getMentionedRoles());
				List<Role> authorRoles = new ArrayList<Role>();
				authorRoles.addAll(event.getMember().getRoles());
				if (users.size() == 0 && roles.size() == 0) {
					event.getMessage().delete().queue();
					event.getChannel()
							.sendMessage("You must specify at least one role or user to apply a permission to")
							.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
				} else if (users.size() == 1 && users.get(0).equals(event.getMember()) && roles.size() == 0) {
					event.getMessage().delete().queue();
					event.getChannel().sendMessage("You cannot apply a permission to yourself")
							.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
				} else if (!(CommandRegistry.getCommandAndGlobalIds().contains(words[0]))) {
					event.getMessage().delete().queue();
					event.getChannel().sendMessage("You must reference a valid command identifier or command group")
							.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
				} else if (!(BotUtils.stringArrayContains(new String[] { "allow", "deny" }, words[1]))) {
					event.getMessage().delete().queue();
					event.getChannel().sendMessage(
							"You must explicitly state whether to allow or deny usage of this command or command group")
							.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
				} else {
					if (!event.getAuthor().equals(NoodleBotMain.botOwner)) {
						List<Member> userCopy = users;
						List<Role> roleCopy = roles;
						for (int i = 0; i < userCopy.size(); i++) {
							if (HierarchyUtils.isMemberLowerThan(event.getMember(), userCopy.get(i))) {
								users.remove(userCopy.get(i));
							}
						}
						for (int i = 0; i < roleCopy.size(); i++) {
							if (HierarchyUtils.isRoleLowerThan(HierarchyUtils.getMemberHighestRole(event.getMember()),
									roleCopy.get(i))) {
								roles.remove(roleCopy.get(i));
							}
						}
						users = userCopy;
						roles = roleCopy;
					}
					if (users.size() == 0 && roles.size() == 0) {
						event.getChannel().sendMessage(
								"The users/roles you mentioned all have higher positions than you and you cannot set their permissions")
								.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
					} else {
						PermissionsManager permMgr = getPermissionsManager(event.getGuild());
						event.getMessage().delete().queue();
						String permission = "";
						String authorException = "";
						if (BotUtils.stringArrayContains(new String[] { "player", "management", "utility", "misc" },
								words[0])) {
							if (words[1].equals("allow")) {
								permission = permMgr.ALLOW_GLOBAL;
							} else {
								permission = permMgr.DENY_GLOBAL;
								authorException = permMgr.ALLOW_GLOBAL;
							}
						} else {
							if (words[1].equals("allow")) {
								permission = permMgr.ALLOW;
							} else {
								permission = permMgr.DENY;
								authorException = permMgr.ALLOW;
							}
						}
						for (Member user : users) {
							permMgr.SetPermission(words[0], user, permission);
						}
						for (Role role : roles) {
							permMgr.SetPermission(words[0], role, permission);
						}
						if (BotUtils.checkForElement(roles, authorRoles) && words[1].equals("deny")) {
							permMgr.SetPermission(words[0], event.getMember(), authorException);
						}
						if (users.contains(event.getMember()) && words[1].equals("deny")) {
							permMgr.SetPermission(words[0], event.getMember(), authorException);
						}
						event.getChannel().sendMessage("Permissions set successfully")
								.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
					}
				}
			} else {
				event.getChannel().sendMessage("You must be an administrator of this server to manage permissions")
						.queue();
			}
		} catch (InsufficientPermissionException e) {
			String permission = e.getPermission().getName();
			EmbedBuilder message = new EmbedBuilder();
			message.setTitle("Missing permission error | " + event.getGuild().getName());
			message.addField("Error message:", "Bot is missing required permission **" + permission
					+ "**. Please grant this permission to the bot's role or contact a guild administrator to apply this permission to the bot's role.",
					false);
			message.setColor(Color.red);
			event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(message.build()).queue());
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood permission <command id/command group> <allow/deny> <@user(s) and/or @role(s)>** - sets a permission for a command/command catagory";
	}

	@Override
	public String getCommandId() {
		return "permsettings";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_MANAGEMENT;
	}

}
