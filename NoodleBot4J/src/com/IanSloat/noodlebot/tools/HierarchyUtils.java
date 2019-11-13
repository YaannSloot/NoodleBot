package com.IanSloat.noodlebot.tools;

import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

// TODO Document class
public class HierarchyUtils {
	
	public static Role getMemberHighestRole(Member member) {
		Role result = null;
		List<Role> memberRoles = member.getRoles();
		if(memberRoles.size() > 0) {
			result = memberRoles.get(0);
		} else {
			result = member.getGuild().getPublicRole();
		}
		return result;
	}
	
	public static Role getMemberLowestRole(Member member) {
		Role result = null;
		List<Role> memberRoles = member.getRoles();
		if(memberRoles.size() > 0) {
			result = memberRoles.get(memberRoles.size() -1);
		} else {
			result = member.getGuild().getPublicRole();
		}
		return result;
	}

	public static boolean isMemberHigherThan(Member user1, Member user2) {
		boolean result = false;
		if(user1.getGuild().equals(user2.getGuild())) {
			if(getMemberHighestRole(user1).getPosition() > getMemberHighestRole(user2).getPosition()) {
				result = true;
			}
		}
		return result;
	}
	
	public static boolean isMemberLowerThan(Member user1, Member user2) {
		boolean result = false;
		if(user1.getGuild().equals(user2.getGuild())) {
			if(getMemberHighestRole(user1).getPosition() < getMemberHighestRole(user2).getPosition()) {
				result = true;
			}
		}
		return result;
	}
	
	public static boolean isRoleHigherThan(Role role1, Role role2) {
		boolean result = false;
		if(role1.getGuild().equals(role2.getGuild())) {
			if(role1.getPosition() > role2.getPosition()) {
				result = true;
			}
		}
		return result;
	}
	
	public static boolean isRoleLowerThan(Role role1, Role role2) {
		boolean result = false;
		if(role1.getGuild().equals(role2.getGuild())) {
			if(role1.getPosition() < role2.getPosition()) {
				result = true;
			}
		}
		return result;
	}
	
}
