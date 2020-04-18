package com.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.IanSloat.noodlebot.BotUtils;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermissionsController;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class VoiceChatKickCommand implements Command {

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
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "vckick");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!CheckForCommandMatch(event.getMessage()))
			throw new NoMatchException();
		try {
			event.getMessage().delete().queue();
			if (event.getMessage().getMentionedMembers().size() > 0 || event.getMessage().getMentionedRoles().size() > 0
					|| event.getMessage().mentionsEveryone()) {
				List<Role> roles = event.getMessage().getMentionedRoles();
				List<Member> members = event.getMessage().getMentionedMembers();
				Set<Member> targets = new HashSet<Member>();
				targets.addAll(members);
				List<Member> vcMembers = new ArrayList<Member>();
				event.getGuild().getVoiceChannels().forEach(vc -> vcMembers.addAll(vc.getMembers()));
				if (event.getMessage().mentionsEveryone())
					targets.addAll(vcMembers);
				members = vcMembers.stream().filter(m -> {
					boolean result = false;
					for (Role r : m.getRoles()) {
						if (roles.contains(r)) {
							result = true;
							break;
						}
					}
					return result;
				}).collect(Collectors.toList());
				targets.addAll(members);
				targets = targets.stream().filter(m -> m.getVoiceState().inVoiceChannel()).collect(Collectors.toSet());
				targets.forEach(m -> event.getGuild().kickVoiceMember(m).queue());
				if (targets.size() > 0)
					event.getChannel().sendMessage(targets.size() + " users were kicked from voice")
							.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
				else
					event.getChannel().sendMessage("The members mentioned are not connected to any voice channels")
							.queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
			} else
				event.getChannel().sendMessage("You must @ mention a target to use this command")
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
				+ "vckick <user(s)|role(s)>** - Kicks all mentioned members from the voice channels they are connected to _("
				+ BotUtils.BOT_PREFIX + "help " + getCommandId() + ")_";
	}

	@Override
	public String getCommandId() {
		return "vckick";
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
