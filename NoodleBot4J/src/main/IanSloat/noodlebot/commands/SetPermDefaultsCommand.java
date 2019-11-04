package main.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.jdaevents.GenericCommandErrorEvent;
import main.IanSloat.noodlebot.jdaevents.GenericCommandEvent;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class SetPermDefaultsCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().equals(BotUtils.BOT_PREFIX + "apply default permissions");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		NoodleBotMain.eventListener.onEvent(new GenericCommandEvent(event.getJDA(), event.getResponseNumber(),
				event.getGuild(), this, event.getMessage().getContentRaw().toLowerCase(), event.getMember()));
		try {
			event.getMessage().delete().queue();
			if (event.getGuild().getOwner().equals(event.getMember())
					|| NoodleBotMain.botOwner.equals(event.getAuthor())) {
				PermissionsManager permMgr = getPermissionsManager(event.getGuild());
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
				event.getChannel().sendMessage("Permissions set successfully")
						.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
			} else {
				event.getChannel().sendMessage("You must be the owner of this server to set default permissions")
						.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
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
			NoodleBotMain.eventListener.onEvent(new GenericCommandErrorEvent(event.getJDA(),
					event.getResponseNumber(), event.getGuild(), this, event.getMessage().getContentRaw(),
					event.getMember(), "Command execution failed due to missing permission: " + permission));
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood apply default permissions** - Sets the recommended default permissions for your server";
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
