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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;

public class GuildJoin {

	private final Logger logger = LoggerFactory.getLogger(GuildJoin.class);

	public void GuildJoinEvent(GuildJoinEvent event) {
		class JoinThread implements Runnable {
			public void run() {
				try {
					Thread.sleep(1000);
					if (!(Events.knownGuildIds.contains(event.getGuild().getId()))) {
						try {
							event.getGuild().getDefaultChannel().sendMessage(
									"Hello! Thanks for adding me to your server.\nFor a list of commands, type \"nood help\"")
									.queue();
						} catch (Exception e) {
							e.printStackTrace();
							event.getGuild().getOwner().getUser().openPrivateChannel()
									.queue(channel -> channel.sendMessage(
											"Hello! Thanks for adding me to your server.\nFor a list of commands, type \"nood help\"")
											.queue());
						}
						logger.info("Added to new guild. Guild: " + event.getGuild().getName() + "(id:"
								+ event.getGuild().getId() + ")");
						GuildSettingsManager sManager = new GuildSettingsManager(event.getGuild());
						sManager.CreateSettings();
						Events.knownGuildIds.add(event.getGuild().getId());
						PermissionsManager permMgr = Command.getPermissionsManager(event.getGuild());
						permMgr.clearPermissions();
						List<Role> guildRoles = new ArrayList<Role>();
						guildRoles.addAll(event.getGuild().getRoles());
						for (Role role : guildRoles) {
							if (!(role.getPermissions().contains(Permission.ADMINISTRATOR))) {
								permMgr.SetPermission(new InfoCommand().getCommandId(), role, permMgr.DENY);
								permMgr.SetPermission(permMgr.MANAGE_GLOBAL, role, permMgr.DENY_GLOBAL);
							}
						}
						permMgr.SetPermission(new InfoCommand().getCommandId(), event.getGuild().getPublicRole(), permMgr.DENY);
						permMgr.SetPermission(permMgr.MANAGE_GLOBAL, event.getGuild().getPublicRole(), permMgr.DENY_GLOBAL);
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

		new Thread(new JoinThread()).start();

	}

}
