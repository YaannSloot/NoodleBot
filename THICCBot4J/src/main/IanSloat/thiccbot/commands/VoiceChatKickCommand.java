package main.IanSloat.thiccbot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class VoiceChatKickCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage("vckick", user);
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
	}
}
