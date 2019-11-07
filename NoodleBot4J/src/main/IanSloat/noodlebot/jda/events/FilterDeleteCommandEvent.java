package main.IanSloat.noodlebot.jda.events;

import java.util.List;

import main.IanSloat.noodlebot.commands.Command;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;

// TODO Document class
public class FilterDeleteCommandEvent extends GenericCommandEvent {

	private List<Member> targetMembers;
	private List<Role> targetRoles;
	private List<Message> targetMessages;

	public FilterDeleteCommandEvent(JDA api, long responseNumber, Guild guild, Command command, String input,
			Member commandIssuer, List<Member> targetMembers, List<Role> targetRoles, List<Message> targetMessages) {
		super(api, responseNumber, guild, command, input, commandIssuer);
		this.targetMembers = targetMembers;
		this.targetRoles = targetRoles;
		this.targetMessages = targetMessages;
	}

	public List<Member> getTargetMembers() {
		return targetMembers;
	}

	public List<Role> getTargetRoles() {
		return targetRoles;
	}

	public List<Message> getTargetMessages() {
		return targetMessages;
	}

}
