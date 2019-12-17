package com.IanSloat.noodlebot.tools;

import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

/**
 * Used to retrieve general information on guild roles
 */
public class RoleUtils {

	/**
	 * Retrieves a guild member's highest role in a guild
	 * 
	 * @param member The guild member to check
	 * @return The guild member's highest role
	 */
	public static Role getMemberHighestRole(Member member) {
		Role result = null;
		List<Role> memberRoles = member.getRoles();
		if (memberRoles.size() > 0) {
			result = memberRoles.get(0);
		} else {
			result = member.getGuild().getPublicRole();
		}
		return result;
	}

	/**
	 * Retrieves a guild member's lowest role in a guild
	 * 
	 * @param member The guild member to check
	 * @return The guild member's lowest role
	 */
	public static Role getMemberLowestRole(Member member) {
		Role result = null;
		List<Role> memberRoles = member.getRoles();
		if (memberRoles.size() > 0) {
			result = memberRoles.get(memberRoles.size() - 1);
		} else {
			result = member.getGuild().getPublicRole();
		}
		return result;
	}

	/**
	 * Checks if a certain user is higher in the role hierarchy than another user
	 * 
	 * @param user1 The user to check
	 * @param user2 The user to compare the first user with
	 * @return True if user1 is higher than user2
	 */
	public static boolean isMemberHigherThan(Member user1, Member user2) {
		boolean result = false;
		if (user1.getGuild().equals(user2.getGuild())) {
			if (getMemberHighestRole(user1).getPosition() > getMemberHighestRole(user2).getPosition()) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Checks if a certain user is lower in the role hierarchy than another user
	 * 
	 * @param user1 The user to check
	 * @param user2 The user to compare the first user with
	 * @return True if user1 is lower than user2
	 */
	public static boolean isMemberLowerThan(Member user1, Member user2) {
		boolean result = false;
		if (user1.getGuild().equals(user2.getGuild())) {
			if (getMemberHighestRole(user1).getPosition() < getMemberHighestRole(user2).getPosition()) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Checks if a certain role is higher in the role hierarchy than another role
	 * 
	 * @param role1 The role to check
	 * @param role2 The role to compare the first role with
	 * @return True if role1 is higher than role2
	 */
	public static boolean isRoleHigherThan(Role role1, Role role2) {
		boolean result = false;
		if (role1.getGuild().equals(role2.getGuild())) {
			if (role1.getPosition() > role2.getPosition()) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Checks if a certain role is lower in the role hierarchy than another role
	 * 
	 * @param role1 The role to check
	 * @param role2 The role to compare the first role with
	 * @return True if role1 is lower than role2
	 */
	public static boolean isRoleLowerThan(Role role1, Role role2) {
		boolean result = false;
		if (role1.getGuild().equals(role2.getGuild())) {
			if (role1.getPosition() < role2.getPosition()) {
				result = true;
			}
		}
		return result;
	}

}
