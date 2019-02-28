package main.IanSloat.thiccbot.commands;

import java.util.ArrayList;
import java.util.List;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.threadbox.MessageDeleteTools;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.PermissionUtils;
import sx.blah.discord.util.RequestBuffer;

public class SetPermissionCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.PERMMGR, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "permission");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		if (PermissionUtils.hasPermissions(event.getGuild(), event.getAuthor(), Permissions.ADMINISTRATOR)) {
			String command = BotUtils.normalizeSentence(event.getMessage().getContent().toLowerCase()
					.substring((BotUtils.BOT_PREFIX + "permission").length()));
			String[] words = command.split(" ");
			List<IUser> users = event.getMessage().getMentions();
			List<IRole> roles = event.getMessage().getRoleMentions();
			List<IRole> authorRoles = event.getAuthor().getRolesForGuild(event.getGuild());
			if (users.size() == 0 && roles.size() == 0) {
				RequestBuffer.request(() -> event.getMessage().delete());
				IMessage errorMsg = RequestBuffer.request(() -> {
					return event.getChannel()
							.sendMessage("You must specify at least one role or user to apply a permission to");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(errorMsg, 5000);
			} else if (users.size() == 1 && users.get(0).equals(event.getAuthor()) && roles.size() == 0) {
				RequestBuffer.request(() -> event.getMessage().delete());
				IMessage errorMsg = RequestBuffer.request(() -> {
					return event.getChannel().sendMessage("You cannot apply a permission to yourself");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(errorMsg, 5000);
			} else if (!(BotUtils.stringArrayContains(PermissionsManager.commandWords, words[0]))) {
				RequestBuffer.request(() -> event.getMessage().delete());
				IMessage errorMsg = RequestBuffer.request(() -> {
					return event.getChannel()
							.sendMessage("You must reference a valid command identifier or command group");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(errorMsg, 5000);
			} else if (!(BotUtils.stringArrayContains(new String[] { "allow", "deny" }, words[1]))) {
				RequestBuffer.request(() -> event.getMessage().delete());
				IMessage errorMsg = RequestBuffer.request(() -> {
					return event.getChannel().sendMessage(
							"You must explicitly state whether to allow or deny usage of this command or command group");
				}).get();
				MessageDeleteTools.DeleteAfterMillis(errorMsg, 5000);
			} else {
				List<IUser> userCopy = users;
				List<IRole> roleCopy = roles;
				for (int i = 0; i < userCopy.size(); i++) {
					if (!(PermissionUtils.isUserHigher(event.getGuild(), event.getAuthor(), userCopy.get(i)))) {
						users.remove(userCopy.get(i));
					}
				}
				for (int i = 0; i < roleCopy.size(); i++) {
					List<IRole> roleQuestion = new ArrayList<IRole>();
					roleQuestion.add(roleCopy.get(i));
					if (!(PermissionUtils.isUserHigher(event.getGuild(), event.getAuthor(), roleQuestion))) {
						roles.remove(roleCopy.get(i));
					}
				}
				users = userCopy;
				roles = roleCopy;
				if (users.size() == 0 && roles.size() == 0) {
					IMessage errorMsg = RequestBuffer.request(() -> {
						return event.getChannel().sendMessage(
								"The users/roles you mentioned all have higher positions than you and you cannot set their permissions");
					}).get();
					MessageDeleteTools.DeleteAfterMillis(errorMsg, 5000);
				} else {
					PermissionsManager permMgr = getPermissionsManager(event.getGuild());
					RequestBuffer.request(() -> event.getMessage().delete());
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
					for (IUser user : users) {
						permMgr.SetPermission(words[0], user, permission);
					}
					for (IRole role : roles) {
						permMgr.SetPermission(words[0], role, permission);
					}
					if (BotUtils.checkForElement(roles, authorRoles) && words[1].equals("deny")) {
						permMgr.SetPermission(words[0], event.getAuthor(), authorException);
					}
					if (users.contains(event.getAuthor()) && words[1].equals("deny")) {
						permMgr.SetPermission(words[0], event.getAuthor(), authorException);
					}
					IMessage completeMsg = RequestBuffer.request(() -> {
						return event.getChannel().sendMessage("Permissions set successfully");
					}).get();
					MessageDeleteTools.DeleteAfterMillis(completeMsg, 5000);
				}
			}
		} else {
			RequestBuffer.request(() -> event.getChannel()
					.sendMessage("You must be an administrator of this server to manage permissions"));
		}
	}

}
