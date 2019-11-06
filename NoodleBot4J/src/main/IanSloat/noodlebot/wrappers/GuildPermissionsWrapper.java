package main.IanSloat.noodlebot.wrappers;

import main.IanSloat.noodlebot.commands.Command;
import net.dv8tion.jda.api.entities.Member;

public class GuildPermissionsWrapper {

	/**
	 * Checks if the specified guild member has permission to use a certain command
	 * @param member The guild member to check for
	 * @param command The command to check
	 * @return True if the member can use the command
	 */
	public boolean canMemberUseCommand(Member member, Command command) {
		// TODO Implement permission checking
		return true;
	}
	
}
