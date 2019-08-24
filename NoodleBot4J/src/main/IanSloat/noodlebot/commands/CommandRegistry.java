package main.IanSloat.noodlebot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;

public class CommandRegistry {

	public static List<Command> CommandInstances = new ArrayList<Command>(Arrays.asList(new FilterDeleteCommand(),
			new GetGuiPasswordCommand(), new HelpCommand(), new InfoCommand(), new InspireMeCommand(),
			new JumpCommand(), new LeaveCommand(), new ListSettingsCommand(), new NewGuiPasswordCommand(),
			new PauseCommand(), new PermIDCommand(), new PlayCommand(), new QuestionCommand(), new RemoveTrackCommand(),
			new SetPermDefaultsCommand(), new SetPermissionCommand(), new SettingsCommand(), new ShowQueueCommand(),
			new SkipCommand(), new StopCommand(), new VolumeCommand(), new WikiCommand(), new VoiceChatKickCommand(),
			new Rule34Command()));

	public static List<String> getCommandIdsByCategory(String category) {
		List<String> result = new ArrayList<String>();
		CommandInstances.stream().filter(cmd -> cmd.getCommandCategory().equals(category)).collect(Collectors.toList())
				.forEach(cmd -> result.add(cmd.getCommandId()));
		return result;
	}

	public static List<String> getAllCommandIds() {
		List<String> result = new ArrayList<String>();
		CommandInstances.forEach(cmd -> result.add(cmd.getCommandId()));
		return result;
	}
	
	public static List<String> getCommandAndGlobalIds() {
		List<String> result = new ArrayList<String>();
		CommandInstances.forEach(cmd -> result.add(cmd.getCommandId()));
		result.add(Command.CATEGORY_MANAGEMENT);
		result.add(Command.CATEGORY_MISC);
		result.add(Command.CATEGORY_PLAYER);
		result.add(Command.CATEGORY_UTILITY);
		return result;
	}
	
	public static boolean checkIfUserHasAccessToCategory(PermissionsManager permMgr, Member user, String category) {
		boolean result = false;
		for(Command command : CommandInstances.stream().filter(cmd -> cmd.getCommandCategory().equals(category)).collect(Collectors.toList())) {
			result = command.CheckUsagePermission(user, permMgr);
		}
		return result;
	}
	
	public static List<String> getUserSpecificHelpListForCategory(PermissionsManager permMgr, Member user, String category) {
		List<String> result = new ArrayList<String>();
		for(Command command : CommandInstances.stream().filter(cmd -> cmd.getCommandCategory().equals(category)).collect(Collectors.toList())) {
			if(command.CheckUsagePermission(user, permMgr)) {
				result.add(command.getHelpSnippet());
			}
		}
		return result;
	}
	
}
