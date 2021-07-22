package com.IanSloat.noodlebot.controllers.permissions;

import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

/**
 * Represents a permission entry for a command fount in a guild permissions
 * file. This acts as a wrapper for a specially formatted JSON object.
 */
public class GuildPermission {

	private String key;
	private Map<String, PermissionValue> users;
	private Map<String, PermissionValue> roles;

	public enum PermissionValue {
		ALLOW("allow"), DENY("deny");

		private String value;

		private PermissionValue(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}

	}

	/**
	 * Constructs a new instance of {@linkplain GuildPermission}
	 * 
	 * @param key   The key to associate with this entry
	 * @param users A map representing the user permissions for this entry
	 * @param roles A map representing the role permissions for this entry
	 */
	public GuildPermission(String key, Map<String, PermissionValue> users, Map<String, PermissionValue> roles) {
		this.key = key;
		this.users = users;
		this.roles = roles;
	}

	/**
	 * Retrieves the key associated with this permission entry
	 * 
	 * @return The key for this permission
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Retrieves the map representing the user permissions for this permissions
	 * entry
	 * 
	 * @return A map representing the user permissions for this permissions entry
	 */
	public Map<String, PermissionValue> getUserEntries() {
		return users;
	}

	/**
	 * Retrieves the map representing the role permissions for this permissions
	 * entry
	 * 
	 * @return A map representing the role permissions for this permissions entry
	 */
	public Map<String, PermissionValue> getRoleEntries() {
		return roles;
	}

	/**
	 * Retrieves the {@linkplain PermissionValue} for a specific {@linkplain Member}
	 * 
	 * @param user The Member to retrieve permission values for
	 * @return The Member's permission value or null if no entry exists
	 */
	public PermissionValue getUserEntry(Member user) {
		PermissionValue result = null;
		if (user != null)
			if (users.containsKey(user.getId()))
				result = users.get(user.getId());
		return result;
	}

	/**
	 * Retrieves the {@linkplain PermissionValue} for a specific {@linkplain Role}
	 * 
	 * @param role The Role to retrieve permission values for
	 * @return The Role's permission value or null if no entry exists
	 */
	public PermissionValue getRoleEntry(Role role) {
		PermissionValue result = null;
		if (role != null)
			if (roles.containsKey(role.getId()))
				result = roles.get(role.getId());
		return result;
	}

	/**
	 * Sets the permission entry for a specific user to a new value. If no entry
	 * exists a new one will be created
	 * 
	 * @param user  The user to set a permission value for
	 * @param value The value to set
	 * @return This {@linkplain GuildPermission} entry. Useful for chaining
	 */
	public GuildPermission setUserEntry(Member user, PermissionValue value) {
		if (user != null)
			users.put(user.getId(), value);
		return this;
	}

	/**
	 * Sets the permission entry for a specific role to a new value. If no entry
	 * exists a new one will be created
	 * 
	 * @param role  The role to set a permission value for
	 * @param value The value to set
	 * @return This {@linkplain GuildPermission} entry. Useful for chaining
	 */
	public GuildPermission setRoleEntry(Role role, PermissionValue value) {
		if (role != null)
			roles.put(role.getId(), value);
		return this;
	}

	/**
	 * Retrieves the JSON representation of this entry. The {@linkplain JSONObject}
	 * returned will be the same way that this entry appears in the permissions
	 * file.
	 * 
	 * @return A JSONObject representation of this entry
	 */
	public JSONObject getObjectEntry() {
		JSONObject result = new JSONObject();
		if (users.isEmpty())
			result.put("users", new JSONObject());
		else {
			JSONObject map = new JSONObject();
			for (Entry<String, PermissionValue> e : users.entrySet()) {
				map.put(e.getKey(), e.getValue().toString());
			}
			result.put("users", map);
		}
		if (roles.isEmpty())
			result.put("roles", new JSONObject());
		else {
			JSONObject map = new JSONObject();
			for (Entry<String, PermissionValue> e : roles.entrySet()) {
				map.put(e.getKey(), e.getValue().toString());
			}
			result.put("roles", map);
		}
		return result;
	}

}
