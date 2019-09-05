package main.IanSloat.noodlebot.events;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.noodlebot.commands.Command;
import main.IanSloat.noodlebot.commands.InfoCommand;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;

public class GuildJoin {

	private final Logger logger = LoggerFactory.getLogger(GuildJoin.class);

	public void GuildJoinEvent(GuildJoinEvent event) {
		if (!(Events.knownGuildIds.contains(event.getGuild().getId()))) {
			try {
				event.getGuild().getTextChannels().get(0)
						.sendMessage(
								"Hello! Thanks for adding me to your server.\nFor a list of commands, type \"nood help\"")
						.queue();
				logger.info("Added to new guild. Guild: " + event.getGuild().getName() + "(id:" + event.getGuild().getId()
						+ ")");
			} catch (Exception e){
				e.printStackTrace();
			}
			
			GuildSettingsManager sManager = new GuildSettingsManager(event.getGuild());
			sManager.CreateSettings();
			Events.knownGuildIds.add(event.getGuild().getId());
			PermissionsManager permMgr = Command.getPermissionsManager(event.getGuild());
			permMgr.clearPermissions();
			List<Member> guildUsers = new ArrayList<Member>();
			List<Role> guildRoles = new ArrayList<Role>();
			guildUsers.addAll(event.getGuild().getMembers());
			guildRoles.addAll(event.getGuild().getRoles());
			guildUsers.remove(event.getGuild().getOwner());
			for (Member user : guildUsers) {
				if (user.hasPermission(Permission.ADMINISTRATOR) == false) {
					permMgr.SetPermission(new InfoCommand().getCommandId(), user, permMgr.DENY);
					permMgr.SetPermission(permMgr.MANAGE_GLOBAL, user, permMgr.DENY_GLOBAL);
				}
			}
			for (Role role : guildRoles) {
				if (!(role.getPermissions().contains(Permission.ADMINISTRATOR))) {
					permMgr.SetPermission(new InfoCommand().getCommandId(), role, permMgr.DENY);
					permMgr.SetPermission(permMgr.MANAGE_GLOBAL, role, permMgr.DENY_GLOBAL);
				}
			}
		}
	}

}
