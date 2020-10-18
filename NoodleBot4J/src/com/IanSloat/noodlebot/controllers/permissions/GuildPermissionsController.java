package com.IanSloat.noodlebot.controllers.permissions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.IanSloat.noodlebot.NoodleBotMain;
import com.IanSloat.noodlebot.commands.Command;
import com.IanSloat.noodlebot.controllers.permissions.GuildPermission.PermissionValue;
import com.IanSloat.noodlebot.tools.RoleUtils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

/**
 * Used to create and modify command permission files for Discord guilds. File
 * data is stored in JSON format
 */
public class GuildPermissionsController {

	private static Consumer<GuildPermissionsController> initBehavior;
	private static final File masterSettingsDirectory = new File("guilds");
	private final File settingsDirectory;
	private final File permissionsFile;
	private JSONObject permissionsRaw;
	private Guild guild;

	/**
	 * Creates a new permissions wrapper for accessing a particular guilds command
	 * permission settings
	 * 
	 * @param guild The guild to check permissions for
	 * @throws IOException
	 */
	public GuildPermissionsController(Guild guild) throws IOException {
		if (!masterSettingsDirectory.exists())
			FileUtils.forceMkdir(masterSettingsDirectory);
		settingsDirectory = new File(masterSettingsDirectory + "/" + guild.getId());
		permissionsFile = new File(settingsDirectory + "/permissions.json");
		if (!settingsDirectory.exists())
			FileUtils.forceMkdir(settingsDirectory);
		if (!permissionsFile.exists())
			permissionsFile.createNewFile();
		try {
			permissionsRaw = new JSONObject(FileUtils.readFileToString(permissionsFile, "UTF-8"));
		} catch (JSONException e) {
			permissionsRaw = new JSONObject();
			FileUtils.write(permissionsFile, permissionsRaw.toString(), "UTF-8");
		}
		this.guild = guild;
		initBehavior.accept(this);
	}

	/**
	 * Sets the global consumer to whatever is passed in the initBehavior parameter.
	 * This can be useful for adding initial values to each instance that is
	 * created.
	 * 
	 * @param initBehavior A consumer to be used each time a new instance of
	 *                     {@linkplain GuildPermissionsController} is created.
	 */
	public static void setInitBehavior(Consumer<GuildPermissionsController> initBehavior) {
		GuildPermissionsController.initBehavior = initBehavior;
	}

	/**
	 * Constructs a new instance of {@linkplain GuildPermissionsController} for the
	 * sole purpose of initializing the files. Nothing is returned, and the instance
	 * is discarded once the files and directories have been created.
	 * 
	 * @param guild The guild to create new permissions files for
	 * @throws IOException If something goes wrong when doing any file io
	 */
	public static void initGuildPermissionsFiles(Guild guild) throws IOException {
		new GuildPermissionsController(guild);
	}

	/**
	 * Adds the specified {@linkplain GuildPermission} object to the permissions
	 * cache. This does not modify the file. Instead this is a safe use method that
	 * has no effect on any retrieved permissions as long as the write method hasn't
	 * been called.
	 * 
	 * @param permission The permission to apply to the permissions cache. Note that
	 *                   only one permission can occupy a given key at any point in
	 *                   time so if a pre-existing permission has the same key as
	 *                   this one it will be overwritten.
	 * @return This {@linkplain GuildPermissionsController} instance. Useful for
	 *         chaining.
	 */
	public GuildPermissionsController setPermission(GuildPermission permission) {
		if (permission != null)
			permissionsRaw.put(permission.getKey(), permission.getObjectEntry());
		return this;
	}

	/**
	 * Removes the specified {@linkplain GuildPermission} object from the
	 * permissions cache. This does not modify the file. Instead this is a safe use
	 * method that has no effect on any retrieved permissions as long as the write
	 * method hasn't been called.
	 * 
	 * @param key The key that points to an existing {@linkplain GuildPermission}
	 * @return This {@linkplain GuildPermissionsController} instance. Useful for
	 *         chaining.
	 */
	public GuildPermissionsController removePermission(String key) {
		permissionsRaw.remove(key);
		return this;
	}

	/**
	 * Removes the specified {@linkplain GuildPermission} object from the
	 * permissions cache. This does not modify the file. Instead this is a safe use
	 * method that has no effect on any retrieved permissions as long as the write
	 * method hasn't been called.
	 * 
	 * @param permission The permission that contains a pre-existing key desired to
	 *                   be removed
	 * @return This {@linkplain GuildPermissionsController} instance. Useful for
	 *         chaining.
	 */
	public GuildPermissionsController removePermission(GuildPermission permission) {
		permissionsRaw.remove(permission.getKey());
		return this;
	}

	/**
	 * Retrieves the guild associated with this controller
	 * 
	 * @return the guild associated with this controller
	 */
	public Guild getGuild() {
		return guild;
	}

	/**
	 * Performs a fresh read on the permissions file and attempts to retrieve the
	 * desired permission that the specified key points to. This has no effect on
	 * the current state of the permissions cache.
	 * 
	 * @param key The key that points to the desired permission.
	 * @return An instance of {@linkplain GuildPermission} that represents a
	 *         permission entry, or null if either no permission could be found that
	 *         matches that key or if a file io error occurred.
	 */
	public GuildPermission getPermission(String key) {
		GuildPermission result = null;
		try {
			JSONObject permissions = new JSONObject(FileUtils.readFileToString(permissionsFile, "UTF-8"));
			if (permissions.has(key))
				if (permissions.get(key) instanceof JSONObject) {
					Map<String, PermissionValue> users = new HashMap<>();
					Map<String, PermissionValue> roles = new HashMap<>();
					if (permissions.getJSONObject(key).has("users")) {
						if (permissions.getJSONObject(key).get("users") instanceof JSONObject) {
							JSONObject usersRaw = permissions.getJSONObject(key).getJSONObject("users");
							for (String k : usersRaw.keySet()) {
								if (usersRaw.optString(k).equals("allow") || usersRaw.optString(k).equals("deny")) {
									if (usersRaw.optString(k).equals("allow"))
										users.put(k, PermissionValue.ALLOW);
									else
										users.put(k, PermissionValue.DENY);
								}
							}
						}
					}
					if (permissions.getJSONObject(key).has("roles")) {
						if (permissions.getJSONObject(key).get("roles") instanceof JSONObject) {
							JSONObject rolesRaw = permissions.getJSONObject(key).getJSONObject("roles");
							for (String k : rolesRaw.keySet()) {
								if (rolesRaw.optString(k).equals("allow") || rolesRaw.optString(k).equals("deny")) {
									if (rolesRaw.optString(k).equals("allow"))
										roles.put(k, PermissionValue.ALLOW);
									else
										roles.put(k, PermissionValue.DENY);
								}
							}
						}
					}
					result = new GuildPermission(key, users, roles);
				}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Performs a fresh read on the permissions file and attempts to retrieve every
	 * valid permission entry. The permissions are then placed into a
	 * {@linkplain GuildPermissions} list, which is a modified
	 * {@linkplain ArrayList}. This has no effect on the current state of the
	 * permissions cache.
	 * 
	 * @return A {@linkplain GuildPermissions} list containing all valid permissions
	 *         entries found in the permissions file.
	 */
	public GuildPermissions getPermissions() {
		GuildPermissions result = new GuildPermissions();
		try {
			JSONObject permissions = new JSONObject(FileUtils.readFileToString(permissionsFile, "UTF-8"));
			for (String key : permissions.keySet()) {
				if (permissions.get(key) instanceof JSONObject) {
					Map<String, PermissionValue> users = new HashMap<>();
					Map<String, PermissionValue> roles = new HashMap<>();
					if (permissions.getJSONObject(key).has("users")) {
						if (permissions.getJSONObject(key).get("users") instanceof JSONObject) {
							JSONObject usersRaw = permissions.getJSONObject(key).getJSONObject("users");
							for (String k : usersRaw.keySet()) {
								if (usersRaw.optString(k).equals("allow") || usersRaw.optString(k).equals("deny")) {
									if (usersRaw.optString(k).equals("allow"))
										users.put(k, PermissionValue.ALLOW);
									else
										users.put(k, PermissionValue.DENY);
								}
							}
						}
					}
					if (permissions.getJSONObject(key).has("roles")) {
						if (permissions.getJSONObject(key).get("roles") instanceof JSONObject) {
							JSONObject rolesRaw = permissions.getJSONObject(key).getJSONObject("roles");
							for (String k : rolesRaw.keySet()) {
								if (rolesRaw.optString(k).equals("allow") || rolesRaw.optString(k).equals("deny")) {
									if (rolesRaw.optString(k).equals("allow"))
										roles.put(k, PermissionValue.ALLOW);
									else
										roles.put(k, PermissionValue.DENY);
								}
							}
						}
					}
					result.add(new GuildPermission(key, users, roles));
				}
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Writes what is currently contained in the permissions cache to the
	 * permissions file. The permissions are written in JSON format.
	 * 
	 * @throws IOException If something goes wrong when doing any file io
	 */
	public void writePermissions() throws IOException {
		FileUtils.write(permissionsFile, permissionsRaw.toString(), "UTF-8");
	}

	public JSONObject getRawCopy() {
		return permissionsRaw;
	}
	
	/**
	 * Checks if the specified guild member has permission to use a certain command
	 * 
	 * @param member  The guild member to check for
	 * @param command The command to check
	 * @return True if the member can use the command
	 */
	public boolean canMemberUseCommand(Member member, Command command) {
		boolean allow = true;
		GuildPermissions permissions = getPermissions();
		int fallthrough = 0;
		switch (fallthrough) {
		case 0:
			if (member.getGuild().retrieveOwner().complete().getId().equals(member.getId())) {
				allow = true;
				break;
			}
		case 1:
			if (NoodleBotMain.botOwner.getId().equals(member.getId())) {
				allow = true;
				break;
			}
		case 2:
			if (permissions.retrieveByKey(command.getCommandCategory().toString()) != null) {
				if (permissions.retrieveByKey(command.getCommandCategory().toString())
						.getRoleEntry(RoleUtils.getMemberHighestRole(member)) != null) {
					if (permissions.retrieveByKey(command.getCommandCategory().toString())
							.getRoleEntry(RoleUtils.getMemberHighestRole(member)).equals(PermissionValue.ALLOW))
						allow = true;
					else
						allow = false;
				}
				if (permissions.retrieveByKey(command.getCommandCategory().toString()).getUserEntry(member) != null) {
					if (permissions.retrieveByKey(command.getCommandCategory().toString()).getUserEntry(member)
							.equals(PermissionValue.ALLOW))
						allow = true;
					else
						allow = false;
				}
			}
			if (permissions.retrieveByKey(command.getCommandId()) != null) {
				if (permissions.retrieveByKey(command.getCommandId())
						.getRoleEntry(RoleUtils.getMemberHighestRole(member)) != null) {
					if (permissions.retrieveByKey(command.getCommandId())
							.getRoleEntry(RoleUtils.getMemberHighestRole(member)).equals(PermissionValue.ALLOW))
						allow = true;
					else
						allow = false;
				}
				if (permissions.retrieveByKey(command.getCommandId()).getUserEntry(member) != null) {
					if (permissions.retrieveByKey(command.getCommandId()).getUserEntry(member)
							.equals(PermissionValue.ALLOW))
						allow = true;
					else
						allow = false;
				}
			}
		}
		return allow;
	}

}
