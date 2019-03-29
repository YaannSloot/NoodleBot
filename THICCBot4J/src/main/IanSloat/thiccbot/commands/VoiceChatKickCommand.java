package main.IanSloat.thiccbot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
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
		List<Member> firstlist = event.getMessage().getMentionedMembers();
		List<Member> tempFilter = new ArrayList<Member>();
		for (Member member : firstlist) {
			if (member.getVoiceState().inVoiceChannel()) {
				tempFilter.add(member);
			}
		}
		final List<Member> members = tempFilter;
		tempFilter = null;
		firstlist = null;
		if (members.size() == 0) {
			event.getTextChannel().sendMessage("The members you mentioned are not in any voice channels")
					.queue((errorMsg) -> errorMsg.delete().queueAfter(5, TimeUnit.SECONDS));
		} else {
			event.getGuild().getController().createVoiceChannel("SH4D0WR34LM").queue(new Consumer<VoiceChannel>() {

				@Override
				public void accept(VoiceChannel shadowRealm) {
					class DeleteFuture implements Runnable {
						public void run() {
							members.forEach((member) -> event.getGuild().getController().moveVoiceMember(member, shadowRealm).complete());
							shadowRealm.delete().queue();
							event.getTextChannel().sendMessage("Kicked " + members.size() + " users from connected voice channels")
							.queue((completeMessage) -> completeMessage.delete().queueAfter(5, TimeUnit.SECONDS));
						}
					}
					new Thread(new DeleteFuture()).start();
				}
			});
		}
	}
}
