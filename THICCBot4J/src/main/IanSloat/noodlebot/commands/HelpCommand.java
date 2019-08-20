package main.IanSloat.noodlebot.commands;

import java.awt.Color;
import java.util.List;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand extends Command {
	
	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return true;
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "help");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if(!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		EmbedBuilder message = new EmbedBuilder();
		message.setTitle(
				"Available commands | " + event.getMember().getUser().getName() + " | " + event.getGuild().getName());
		message.addField("**General Commands**", "**nood help** - Lists available commands", false);
		message.setColor(Color.RED);
		PermissionsManager permMgr = getPermissionsManager(event.getGuild());
		if (CommandRegistry.checkIfUserHasAccessToCategory(permMgr, event.getMember(), CATEGORY_PLAYER)) {
			String hlpMsg = "";
			String title = "**Player commands**";
			List<String> help = CommandRegistry.getUserSpecificHelpListForCategory(permMgr, event.getMember(), CATEGORY_PLAYER);
			for(String hlp : help) {
				if((hlpMsg + hlp + "\n").length() > 1024) {
					message.addField(title, hlpMsg, false);
					EmbedBuilder copy = message;
					event.getMember().getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(copy.build()).queue());
					message = new EmbedBuilder();
					message.setColor(Color.RED);
					title = "**Player commands continued**";
					hlpMsg = "";
				}
				hlpMsg += hlp + "\n";
			}
			message.addField(title, hlpMsg, false);
		}
		if(!message.isValidLength(AccountType.BOT)) {
			EmbedBuilder copy = message;
			event.getMember().getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(copy.build()).queue());
			message = new EmbedBuilder();
			message.setColor(Color.RED);
		}
		if (CommandRegistry.checkIfUserHasAccessToCategory(permMgr, event.getMember(), CATEGORY_MANAGEMENT)) {
			String hlpMsg = "";
			String title = "**Server management commands**";
			List<String> help = CommandRegistry.getUserSpecificHelpListForCategory(permMgr, event.getMember(), CATEGORY_MANAGEMENT);
			for(String hlp : help) {
				if((hlpMsg + hlp + "\n").length() > 1024) {
					message.addField(title, hlpMsg, false);
					EmbedBuilder copy = message;
					event.getMember().getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(copy.build()).queue());
					message = new EmbedBuilder();
					message.setColor(Color.RED);
					title = "**Server management commands continued**";
					hlpMsg = "";
				}
				hlpMsg += hlp + "\n";
			}
			message.addField(title, hlpMsg, false);
		}
		if(!message.isValidLength(AccountType.BOT)) {
			EmbedBuilder copy = message;
			event.getMember().getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(copy.build()).queue());
			message = new EmbedBuilder();
			message.setColor(Color.RED);
		}
		if (CommandRegistry.checkIfUserHasAccessToCategory(permMgr, event.getMember(), CATEGORY_UTILITY)) {
			String hlpMsg = "";
			String title = "**Utility commands**";
			List<String> help = CommandRegistry.getUserSpecificHelpListForCategory(permMgr, event.getMember(), CATEGORY_UTILITY);
			for(String hlp : help) {
				if((hlpMsg + hlp + "\n").length() > 1024) {
					message.addField(title, hlpMsg, false);
					EmbedBuilder copy = message;
					event.getMember().getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(copy.build()).queue());
					message = new EmbedBuilder();
					message.setColor(Color.RED);
					title = "**Utility commands continued**";
					hlpMsg = "";
				}
				hlpMsg += hlp + "\n";
			}
			message.addField(title, hlpMsg, false);
		}
		if(!message.isValidLength(AccountType.BOT)) {
			EmbedBuilder copy = message;
			event.getMember().getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(copy.build()).queue());
			message = new EmbedBuilder();
			message.setColor(Color.RED);
		}
		if (CommandRegistry.checkIfUserHasAccessToCategory(permMgr, event.getMember(), CATEGORY_MISC)) {
			String hlpMsg = "";
			String title = "**Other commands**";
			List<String> help = CommandRegistry.getUserSpecificHelpListForCategory(permMgr, event.getMember(), CATEGORY_MISC);
			for(String hlp : help) {
				if((hlpMsg + hlp + "\n").length() > 1024) {
					message.addField(title, hlpMsg, false);
					EmbedBuilder copy = message;
					event.getMember().getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(copy.build()).queue());
					message = new EmbedBuilder();
					message.setColor(Color.RED);
					title = "**Other commands continued**";
					hlpMsg = "";
				}
				hlpMsg += hlp + "\n";
			}
			message.addField(title, hlpMsg, false);
		}
		EmbedBuilder copy = message;
		event.getMember().getUser().openPrivateChannel().queue((channel) -> channel.sendMessage(copy.build()).queue());
	}

	@Override
	public String getHelpSnippet() {
		return "**nood help** - Lists available commands";
	}

	@Override
	public String getCommandId() {
		return "help";
	}

	@Override
	public String getCommandCategory() {
		return "generic";
	}
}
