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

public class SetPermDefaultsCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.PERMMGR, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().equals(BotUtils.BOT_PREFIX + "apply default permissions");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if(!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		if (event.getGuild().getOwner().equals(event.getAuthor())) {
			PermissionsManager permMgr = getPermissionsManager(event.getGuild());
			permMgr.clearPermissions();
			List<IUser> guildUsers = new ArrayList<IUser>();
			List<IRole> guildRoles = new ArrayList<IRole>();
			guildUsers.addAll(event.getGuild().getUsers());
			guildRoles.addAll(event.getGuild().getRoles());
			guildUsers.remove(event.getGuild().getOwner());
			for (IUser user : guildUsers) {
				if (PermissionUtils.hasPermissions(event.getGuild(), user,
						Permissions.ADMINISTRATOR) == false) {
					permMgr.SetPermission(permMgr.INFO, user, permMgr.DENY);
					permMgr.SetPermission(permMgr.MANAGE_GLOBAL, user, permMgr.DENY_GLOBAL);
				}
			}
			for (IRole role : guildRoles) {
				if (!(role.getPermissions().contains(Permissions.ADMINISTRATOR))) {
					permMgr.SetPermission(permMgr.INFO, role, permMgr.DENY);
					permMgr.SetPermission(permMgr.MANAGE_GLOBAL, role, permMgr.DENY_GLOBAL);
				}
			}
			IMessage completeMsg = RequestBuffer.request(() -> {
				return event.getChannel().sendMessage("Permissions set successfully");
			}).get();
			MessageDeleteTools.DeleteAfterMillis(completeMsg, 5000);
		} else {
			RequestBuffer.request(() -> event.getChannel()
					.sendMessage("You must be the owner of this server to set default permissions"));
		}
	}
	
}
