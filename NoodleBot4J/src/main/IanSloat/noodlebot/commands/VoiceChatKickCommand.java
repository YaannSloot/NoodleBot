package main.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class VoiceChatKickCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "vckick");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		try {
			List<Member> members = new ArrayList<Member>();
			if (event.getMessage().mentionsEveryone()) {
				List<VoiceChannel> voiceChannels = event.getGuild().getVoiceChannels();
				if (voiceChannels.size() > 0) {
					for (VoiceChannel channel : voiceChannels) {
						List<Member> channelMembers = channel.getMembers();
						if (channelMembers.size() > 0) {
							members.addAll(channelMembers);
						}
					}
				}
			} else {
				List<Role> roles = event.getMessage().getMentionedRoles();
				List<VoiceChannel> voiceChannels = event.getGuild().getVoiceChannels();
				List<Member> firstlist = new ArrayList<Member>();
				firstlist.addAll(event.getMessage().getMentionedMembers());
				List<Member> tempFilter = new ArrayList<Member>();
				if (voiceChannels.size() > 0) {
					for (VoiceChannel channel : voiceChannels) {
						List<Member> channelMembers = channel.getMembers();
						if (channelMembers.size() > 0) {
							for (Member channelMember : channelMembers) {
								List<Role> memberRoles = channelMember.getRoles();
								if (memberRoles.size() > 0) {
									for (Role memberRole : memberRoles) {
										if (roles.contains(memberRole)) {
											firstlist.add(channelMember);
											break;
										}
									}
								}
							}
						}
					}
				}
				for (Member member : firstlist) {
					if (member.getVoiceState().inVoiceChannel()) {
						tempFilter.add(member);
					}
				}
				members = tempFilter;
				tempFilter = null;
				firstlist = null;
			}
			if (members.size() == 0) {
				event.getTextChannel().sendMessage("The members you mentioned are not in any voice channels")
						.queue((errorMsg) -> errorMsg.delete().queueAfter(5, TimeUnit.SECONDS));
			} else {
				event.getMessage().delete().queue();
				members.forEach((member) -> event.getGuild().kickVoiceMember(member).queue());
				event.getChannel().sendMessage("Kicked " + members.size() + " users from connected voice channels")
						.queue((msg) -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
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
		return "**nood vckick <@user(s)|@role(s)>** - Kicks any mentioned users or roles from whatever voice channel they are connected to. "
				+ "The person using this command does not need to be connected to any voice channels.";
	}

	@Override
	public String getCommandId() {
		return "vckick";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_MANAGEMENT;
	}
}
