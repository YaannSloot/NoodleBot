package main.IanSloat.thiccbot.tools;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class PermissionsManager {

	private static final Logger logger = LoggerFactory.getLogger(PermissionsManager.class);

	public final String CLEAR_COMMAND = "clear";
	public final String BY_FILTER = "filter";
	public final String GET_LOGIN = "loginreq";
	public final String HELP = "help";
	public final String INFO = "info";
	public final String INSPIRE_ME = "inspire";
	public final String LEAVE = "leave";
	public final String LIST_SETTINGS = "listsettings";
	public final String PLAY = "play";
	public final String QUESTION = "question";
	public final String SET_COMMAND = "set";
	public final String SKIP = "skip";
	public final String STOP = "stop";
	public final String VOLUME = "volume";
	public final String SHOW_QUEUE = "showqueue";
	public final String PERMMGR = "permsettings";
	public final String DENY = "Deny";
	public final String ALLOW = "Allow";
	public final String ALLOW_GLOBAL = "GlobalAllow";
	public final String DENY_GLOBAL = "GlobalDeny";
	
	private IGuild guild;
	private TBMLSettingsParser setParser;

	public PermissionsManager(IGuild guild) {
		GuildSettingsManager setMgr = new GuildSettingsManager(guild);
		this.guild = guild;
		this.setParser = setMgr.getTBMLParser();
		this.setParser.setScope(TBMLSettingsParser.DOCROOT);
		initRegistry();
		regRoot();
	}

	private void regRoot() {
		this.setParser.setScope(TBMLSettingsParser.DOCROOT);
		this.setParser.addObj("PermissionsRegistry");
		this.setParser.setScope("PermissionsRegistry");
	}

	private void initRegistry() {
		this.setParser.setScope(TBMLSettingsParser.DOCROOT);
		this.setParser.addObj("PermissionsRegistry");
		this.setParser.setScope("PermissionsRegistry");
		this.setParser.addObj("PlayerPermissions");
		this.setParser.addObj("ManagementPermissions");
		this.setParser.addObj("UtilityPermissions");
		this.setParser.addObj("MiscPermissions");
	}
	
	private String getCatagory(String command) {
		String regDirectory = "";
		if (command.equals(PLAY) || command.equals(SKIP) || command.equals(STOP) || command.equals(VOLUME)
				|| command.equals(LEAVE) || command.equals(SHOW_QUEUE)) {
			regDirectory = "PlayerPermissions";
		} else if (command.equals(CLEAR_COMMAND) || command.equals(BY_FILTER) || command.equals(SET_COMMAND)
				|| command.equals(LIST_SETTINGS) || command.equals(GET_LOGIN)) {
			regDirectory = "ManagementPermissions";
		} else if (command.equals(INFO) || command.equals(HELP) || command.equals(QUESTION) || command.equals(PERMMGR)) {
			regDirectory = "UtilityPermissions";
		} else if (command.equals(INSPIRE_ME)) {
			regDirectory = "MiscPermissions";
		}
		return regDirectory;
	}

	public boolean authUsage(String command, IChannel channel, IUser user) {
		boolean allow = true;
		ArrayList<String> globalUserDeny;
		ArrayList<String> globalUserAllow;
		ArrayList<String> globalRoleDeny;
		ArrayList<String> globalRoleAllow;
		ArrayList<String> UserDeny;
		ArrayList<String> UserAllow;
		ArrayList<String> RoleDeny;
		ArrayList<String> RoleAllow;
		ArrayList<String> UserRoles = new ArrayList<String>();
		String User = user.getStringID();
		String regDirectory = "";
		boolean match = false;
		regDirectory = getCatagory(command);
		if(!(regDirectory.equals(""))) {
			match = true;
		}
		if (match == true) {
			setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/GlobalDeny");
			globalUserDeny = setParser.getValGroup("User");
			globalRoleDeny = setParser.getValGroup("Role");
			setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/GlobalAllow");
			globalUserAllow = setParser.getValGroup("User");
			globalRoleAllow = setParser.getValGroup("Role");
			setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + command + "/Deny");
			UserDeny = setParser.getValGroup("User");
			RoleDeny = setParser.getValGroup("Role");
			setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + command + "/Allow");
			UserAllow = setParser.getValGroup("User");
			RoleAllow = setParser.getValGroup("Role");
			for(IRole role : user.getRolesForGuild(guild)) {
				UserRoles.add(role.getStringID());
			}
			if(globalUserDeny.contains(User)) {
				allow = false;
				if(checkForElement(RoleAllow, UserRoles)) {
					allow = true;
					if(UserDeny.contains(User)) {
						allow = false;
					}
				} else if(UserAllow.contains(User)) {
					allow = true;
				}
			} else if(globalUserAllow.contains(User)) {
				allow = true;
				if(checkForElement(RoleDeny, UserRoles)) {
					allow = false;
					if(UserAllow.contains(User)) {
						allow = true;
					}
				} else if(UserDeny.contains(User)) {
					allow = false;
				}
			} else if(checkForElement(globalRoleDeny, UserRoles)) {
				allow = false;
				if(checkForElement(RoleAllow, UserRoles)) {
					allow = true;
					if(UserDeny.contains(User)) {
						allow = false;
					}
				} else if(UserAllow.contains(User)) {
					allow = true;
				}
			} else if(checkForElement(globalRoleAllow, UserRoles)) {
				allow = true;
				if(checkForElement(RoleDeny, UserRoles)) {
					allow = false;
					if(UserAllow.contains(User)) {
						allow = true;
					}
				} else if(UserDeny.contains(User)) {
					allow = false;
				}
			} else if(checkForElement(RoleDeny, UserRoles)) {
				allow = false;
				if(UserAllow.contains(User)) {
					allow = true;
				}
			} else if(checkForElement(RoleAllow, UserRoles)) {
				allow = true;
				if(UserDeny.contains(User)) {
					allow = false;
				}
			} else if(UserDeny.contains(User)) {
				allow = false;
			}
		}
		return allow;
	}
	
	public void SetPermission(String command, IUser user, String permission) {
		String User = user.getStringID();
		String regDirectory = "";
		String deletePath = "";
		boolean match = false;
		regDirectory = getCatagory(command);
		if(!(regDirectory.equals(""))) {
			match = true;
		}
		if (match == true) {
			if(permission.equals(ALLOW) || permission.equals(DENY)) {
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + command + "/" + permission);
				if(permission.equals(ALLOW)){
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + command + "/" + DENY;
				} else {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + command + "/" + ALLOW;
				}
			}
			else {
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + permission);
				if(permission.equals(ALLOW_GLOBAL)){
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + DENY_GLOBAL;
				} else {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + ALLOW_GLOBAL;
				}
			}
			if(!(setParser.valExists("User", User))) {
				setParser.addVal("User", User);
			}
			setParser.setScopePath(deletePath);
			setParser.removeVal("User", User);
		}
	}

	public void SetPermission(String command, IRole role, String permission) {
		String Role = role.getStringID();
		String regDirectory = "";
		String deletePath = "";
		boolean match = false;
		regDirectory = getCatagory(command);
		if(!(regDirectory.equals(""))) {
			match = true;
		}
		if (match == true) {
			if(permission.equals(ALLOW) || permission.equals(DENY)) {
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + command + "/" + permission);
				if(permission.equals(ALLOW)){
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + command + "/" + DENY;
				} else {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + command + "/" + ALLOW;
				}
			}
			else {
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + permission);
				if(permission.equals(ALLOW_GLOBAL)){
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + DENY_GLOBAL;
				} else {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + ALLOW_GLOBAL;
				}
			}
			if(!(setParser.valExists("Role", Role))) {
				setParser.addVal("Role", Role);
			}
			setParser.setScopePath(deletePath);
			setParser.removeVal("Role", Role);
		}
	}
	
	public void removePermission(String command, IUser user, String permission) {
		String User = user.getStringID();
		String regDirectory = "";
		boolean match = false;
		regDirectory = getCatagory(command);
		if(!(regDirectory.equals(""))) {
			match = true;
		}
		if (match == true) {
			if(permission.equals(ALLOW) || permission.equals(DENY))
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + command + "/" + permission);
			else
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + permission);
			
			setParser.removeVal("User", User);
		}
	}
	
	public void removePermission(String command, IRole role, String permission) {
		String Role = role.getStringID();
		String regDirectory = "";
		boolean match = false;
		regDirectory = getCatagory(command);
		if(!(regDirectory.equals(""))) {
			match = true;
		}
		if (match == true) {
			if(permission.equals(ALLOW) || permission.equals(DENY))
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + command + "/" + permission);
			else
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + permission);
			
			setParser.removeVal("Role", Role);
		}
	}
	
	private boolean checkForElement(ArrayList<String> list, ArrayList<String> elements) {
		boolean value = false;
		for(String element : elements) {
			if(list.contains(element)) {
				value = true;
				break;
			}
		}
		return value;
	}
	
}
