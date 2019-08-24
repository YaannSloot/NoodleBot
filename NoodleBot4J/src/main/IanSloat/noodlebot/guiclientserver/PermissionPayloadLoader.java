package main.IanSloat.noodlebot.guiclientserver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import main.IanSloat.noodlebot.tools.PermissionsManager;
import main.IanSloat.noodlebot.commands.Command;
import main.IanSloat.noodlebot.commands.CommandRegistry;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.entities.Guild;

public class PermissionPayloadLoader {

	private Guild guild;
	
	private File tempFile;
	
	private NBMLSettingsParser parser;
	
	public PermissionPayloadLoader(Guild guild, File tempFile) {
		this.guild = guild;
		this.tempFile = tempFile;
		parser = new NBMLSettingsParser(tempFile);
	}
	
	private Map<String, Long> getUsersAndRoles(String path){
		Map<String, Long> Commands = new HashMap<>();
		String currentGlobalPath = path;
		parser.setScopePath(currentGlobalPath);
		List<String> objects = parser.GetObjectNames();
		objects.remove("GlobalAllow");
		objects.remove("GlobalDeny");
		parser.setScopePath(currentGlobalPath + "/GlobalAllow");
		if(parser.tallyValGroup("User") > 0) {
			Commands.put("global-users-allow", parser.tallyValGroup("User"));
		}
		if(parser.tallyValGroup("Role") > 0) {
			Commands.put("global-roles-allow", parser.tallyValGroup("Role"));
		}
		parser.setScopePath(currentGlobalPath + "/GlobalDeny");
		if(parser.tallyValGroup("User") > 0) {
			Commands.put("global-users-deny", parser.tallyValGroup("User"));
		}
		if(parser.tallyValGroup("Role") > 0) {
			Commands.put("global-roles-deny", parser.tallyValGroup("Role"));
		}
		for(String obj : objects) {
			parser.setScopePath(currentGlobalPath + '/' + obj + '/' + "Allow");
			if(parser.tallyValGroup("User") > 0) {
				Commands.put(obj + "-users-allow", parser.tallyValGroup("User"));
			}
			if(parser.tallyValGroup("Role") > 0) {
				Commands.put(obj + "-roles-allow", parser.tallyValGroup("Role"));
			}
			parser.setScopePath(currentGlobalPath + '/' + obj + '/' + "Deny");
			if(parser.tallyValGroup("User") > 0) {
				Commands.put(obj + "-users-deny", parser.tallyValGroup("User"));
			}
			if(parser.tallyValGroup("Role") > 0) {
				Commands.put(obj + "-roles-deny", parser.tallyValGroup("Role"));
			}
		}
		return Commands;
	}
	
	public JSONObject queryObjects() {
		JSONObject result = new JSONObject();
		Map<String, Long> maps = getUsersAndRoles("PermissionsRegistry/PlayerPermissions");
		result.put("player", maps);
		maps = getUsersAndRoles("PermissionsRegistry/ManagementPermissions");
		result.put("management", maps);
		maps = getUsersAndRoles("PermissionsRegistry/UtilityPermissions");
		result.put("utility", maps);
		maps = getUsersAndRoles("PermissionsRegistry/MiscPermissions");
		result.put("misc", maps);
		return result;
	}
	
	public JSONObject getIDs() {
		JSONObject result = new JSONObject();
		List<String> player = new ArrayList<String>();
		List<String> manage = new ArrayList<String>();
		List<String> util = new ArrayList<String>();
		List<String> misc = new ArrayList<String>();
		for(String command : CommandRegistry.getCommandIdsByCategory(Command.CATEGORY_PLAYER)) {
			player.add(command);
		}
		for(String command : CommandRegistry.getCommandIdsByCategory(Command.CATEGORY_MANAGEMENT)) {
			manage.add(command);
		}
		for(String command : CommandRegistry.getCommandIdsByCategory(Command.CATEGORY_UTILITY)) {
			util.add(command);
		}
		for(String command : CommandRegistry.getCommandIdsByCategory(Command.CATEGORY_MISC)) {
			misc.add(command);
		}
		String catagory = player.get(0);
		player.remove(0);
		result.put(catagory, player);
		catagory = manage.get(0);
		manage.remove(0);
		result.put(catagory, manage);
		catagory = util.get(0);
		util.remove(0);
		result.put(catagory, util);
		catagory = misc.get(0);
		misc.remove(0);
		result.put(catagory, misc);
		return result;
	}
	
}
