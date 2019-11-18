package com.IanSloat.noodlebot.controllers;

import com.IanSloat.noodlebot.commands.Command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class GuildPermissionsController {

	private Guild guild;

	/**
	 * Creates a new permissions wrapper for accessing a particular guilds command
	 * permission settings
	 * 
	 * @param guild The guild to check permissions for
	 */
	public GuildPermissionsController(Guild guild) {
		this.guild = guild;
	}

	/**
	 * Checks if the specified guild member has permission to use a certain command
	 * 
	 * @param member  The guild member to check for
	 * @param command The command to check
	 * @return True if the member can use the command
	 */
	public boolean canMemberUseCommand(Member member, Command command) {
		// TODO Implement permission checking
		return true;
	}

	/**
	 * Retrieves the guild that this wrapper was created with
	 * 
	 * @return The guild this wrapper is referencing
	 */
	public Guild getGuild() {
		return guild;
	}

}
