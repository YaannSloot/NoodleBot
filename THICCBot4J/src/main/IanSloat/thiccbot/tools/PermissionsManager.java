package main.IanSloat.thiccbot.tools;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.thiccbot.ThiccBotMain;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class PermissionsManager {

	private static final Logger logger = LoggerFactory.getLogger(PermissionsManager.class);

	public final String CLEAR_COMMAND = "clear";
	public final String BY_FILTER = "filter";
	public final String GET_LOGIN = "adminlogin";
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
	public final String PLAYER_GLOBAL = "player";
	public final String MANAGE_GLOBAL = "management";
	public final String UTIL_GLOBAL = "utility";
	public final String MISC_GLOBAL = "misc";
	public final String DENY = "Deny";
	public final String ALLOW = "Allow";
	public final String ALLOW_GLOBAL = "GlobalAllow";
	public final String DENY_GLOBAL = "GlobalDeny";

	public static final String[] commandWords = { "player", "play", "volume", "skip", "jump", "stop", "pause",
			"queuemanage", "showqueue", "leave", "management", "clear", "filter", "set", "listsettings", "adminlogin",
			"permsettings", "utility", "info", "question", "wiki", "misc", "inspire", "vckick" };

	public static final String[] playerCommands = { "player", "play", "volume", "skip", "jump", "stop", "pause",
			"queuemanage", "showqueue", "leave" };

	public static final String[] managementCommands = { "management", "clear", "filter", "set", "listsettings",
			"adminlogin", "permsettings", "vckick" };

	public static final String[] utilityCommands = { "utility", "info", "question", "wiki" };

	public static final String[] miscCommands = { "misc", "inspire", "nsfw" };

	private Guild guild;
	private TBMLSettingsParser setParser;

	public PermissionsManager(Guild guild) {
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
		if (Arrays.asList(playerCommands).contains(command)) {
			regDirectory = "PlayerPermissions";
		} else if (Arrays.asList(managementCommands).contains(command)) {
			regDirectory = "ManagementPermissions";
		} else if (Arrays.asList(utilityCommands).contains(command)) {
			regDirectory = "UtilityPermissions";
		} else if (Arrays.asList(miscCommands).contains(command)) {
			regDirectory = "MiscPermissions";
		}
		return regDirectory;
	}

	public boolean authUsage(String command, Member user) {
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
		String User = user.getId();
		String regDirectory = "";
		boolean match = false;
		regDirectory = getCatagory(command);
		if (!(regDirectory.equals(""))) {
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
			for (Role role : user.getRoles()) {
				UserRoles.add(role.getId());
			}
			if (guild.getOwner().getId().equals(User)) {
				allow = true;
			} else if (ThiccBotMain.botOwner.getId().equals(User)) {
				allow = true;
			} else if (globalUserDeny.contains(User)) {
				allow = false;
				if (checkForElement(RoleAllow, UserRoles)) {
					allow = true;
					if (UserDeny.contains(User)) {
						allow = false;
					}
				} else if (UserAllow.contains(User)) {
					allow = true;
				}
			} else if (globalUserAllow.contains(User)) {
				allow = true;
				if (checkForElement(RoleDeny, UserRoles)) {
					allow = false;
					if (UserAllow.contains(User)) {
						allow = true;
					}
				} else if (UserDeny.contains(User)) {
					allow = false;
				}
			} else if (checkForElement(globalRoleDeny, UserRoles)) {
				allow = false;
				if (checkForElement(RoleAllow, UserRoles)) {
					allow = true;
					if (UserDeny.contains(User)) {
						allow = false;
					}
				} else if (UserAllow.contains(User)) {
					allow = true;
				}
			} else if (checkForElement(globalRoleAllow, UserRoles)) {
				allow = true;
				if (checkForElement(RoleDeny, UserRoles)) {
					allow = false;
					if (UserAllow.contains(User)) {
						allow = true;
					}
				} else if (UserDeny.contains(User)) {
					allow = false;
				}
			} else if (checkForElement(RoleDeny, UserRoles)) {
				allow = false;
				if (UserAllow.contains(User)) {
					allow = true;
				}
			} else if (checkForElement(RoleAllow, UserRoles)) {
				allow = true;
				if (UserDeny.contains(User)) {
					allow = false;
				}
			} else if (UserDeny.contains(User)) {
				allow = false;
			}
		}
		return allow;
	}

	public void SetPermission(String command, Member user, String permission) {
		String User = user.getId();
		String regDirectory = "";
		String deletePath = "";
		boolean match = false;
		regDirectory = getCatagory(command);
		if (!(regDirectory.equals(""))) {
			match = true;
		}
		if (match == true) {
			if (permission.equals(ALLOW) || permission.equals(DENY)) {
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + command + "/" + permission);
				if (permission.equals(ALLOW)) {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + command + "/" + DENY;
				} else {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + command + "/" + ALLOW;
				}
			} else {
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + permission);
				if (permission.equals(ALLOW_GLOBAL)) {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + DENY_GLOBAL;
				} else {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + ALLOW_GLOBAL;
				}
			}
			if (!(setParser.valExists("User", User))) {
				setParser.addVal("User", User);
			}
			setParser.setScopePath(deletePath);
			setParser.removeVal("User", User);
		}
	}

	public void SetPermission(String command, Role role, String permission) {
		String Role = role.getId();
		String regDirectory = "";
		String deletePath = "";
		boolean match = false;
		regDirectory = getCatagory(command);
		if (!(regDirectory.equals(""))) {
			match = true;
		}
		if (match == true) {
			if (permission.equals(ALLOW) || permission.equals(DENY)) {
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + command + "/" + permission);
				if (permission.equals(ALLOW)) {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + command + "/" + DENY;
				} else {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + command + "/" + ALLOW;
				}
			} else {
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + permission);
				if (permission.equals(ALLOW_GLOBAL)) {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + DENY_GLOBAL;
				} else {
					deletePath = "PermissionsRegistry/" + regDirectory + "/" + ALLOW_GLOBAL;
				}
			}
			if (!(setParser.valExists("Role", Role))) {
				setParser.addVal("Role", Role);
			}
			setParser.setScopePath(deletePath);
			setParser.removeVal("Role", Role);
		}
	}

	public void removePermission(String command, Member user, String permission) {
		String User = user.getId();
		String regDirectory = "";
		boolean match = false;
		regDirectory = getCatagory(command);
		if (!(regDirectory.equals(""))) {
			match = true;
		}
		if (match == true) {
			if (permission.equals(ALLOW) || permission.equals(DENY))
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + command + "/" + permission);
			else
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + permission);

			setParser.removeVal("User", User);
		}
	}

	public void removePermission(String command, Role role, String permission) {
		String Role = role.getId();
		String regDirectory = "";
		boolean match = false;
		regDirectory = getCatagory(command);
		if (!(regDirectory.equals(""))) {
			match = true;
		}
		if (match == true) {
			if (permission.equals(ALLOW) || permission.equals(DENY))
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + command + "/" + permission);
			else
				setParser.setScopePath("PermissionsRegistry/" + regDirectory + "/" + permission);

			setParser.removeVal("Role", Role);
		}
	}

	public void clearPermissions() {
		setParser.setScopePath("PermissionsRegistry");
		setParser.clearCurrentObj();
		initRegistry();
	}

	private boolean checkForElement(ArrayList<String> list, ArrayList<String> elements) {
		boolean value = false;
		for (String element : elements) {
			if (list.contains(element)) {
				value = true;
				break;
			}
		}
		return value;
	}

}
