package main.IanSloat.thiccbot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetPermDefaultsCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.PERMMGR, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "apply default permissions");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if(!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		if (event.getGuild().getOwner().equals(event.getMember())) {
			PermissionsManager permMgr = getPermissionsManager(event.getGuild());
			permMgr.clearPermissions();
			List<Member> guildUsers = new ArrayList<Member>();
			List<Role> guildRoles = new ArrayList<Role>();
			guildUsers.addAll(event.getGuild().getMembers());
			guildRoles.addAll(event.getGuild().getRoles());
			guildUsers.remove(event.getGuild().getOwner());
			for (Member user : guildUsers) {
				if (user.hasPermission(Permission.ADMINISTRATOR) == false) {
					permMgr.SetPermission(permMgr.INFO, user, permMgr.DENY);
					permMgr.SetPermission(permMgr.MANAGE_GLOBAL, user, permMgr.DENY_GLOBAL);
				}
			}
			for (Role role : guildRoles) {
				if (!(role.getPermissions().contains(Permission.ADMINISTRATOR))) {
					permMgr.SetPermission(permMgr.INFO, role, permMgr.DENY);
					permMgr.SetPermission(permMgr.MANAGE_GLOBAL, role, permMgr.DENY_GLOBAL);
				}
			}
			event.getChannel().sendMessage("Permissions set successfully").queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
		} else {
			event.getChannel().sendMessage("You must be the owner of this server to set default permissions")
				.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
		}
	}
	
}
