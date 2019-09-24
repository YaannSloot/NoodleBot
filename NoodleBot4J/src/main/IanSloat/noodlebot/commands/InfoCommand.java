package main.IanSloat.noodlebot.commands;

import java.awt.Color;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.NoodleBotMain;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class InfoCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "info");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		try {
			event.getMessage().delete().queue();
			EmbedBuilder response = new EmbedBuilder();
			response.addField("Powered by", "Java", false);
			response.addField("Bot Version", NoodleBotMain.botVersion + "\n(Release #).(feature version).(patch #)",
					false);
			response.addField("Status", NoodleBotMain.devMsg, false);
			response.addField("Current number of guilds bot is a member of",
					NoodleBotMain.shardmgr.getGuilds().size() + " guilds", false);
			response.addField("Current shard count", event.getJDA().getShardInfo().getShardTotal() + " Shards active",
					false);
			response.addField("Current amount of threads running on server", Thread.activeCount() + " Active threads",
					false);
			response.setTitle("Bot Info");
			response.setColor(new Color(0, 255, 0));
			event.getChannel().sendMessage(response.build()).queue();
		} catch (InsufficientPermissionException e) {
			String permission = e.getPermission().getName();
			EmbedBuilder message = new EmbedBuilder();
			message.setTitle("Missing permission error | " + event.getGuild().getName());
			message.addField("Error message:", "Bot is missing required permission **" + permission
					+ "**. Please grant this permission to the bot's role or contact a guild administrator to apply this permission to the bot's role.",
					false);
			message.setColor(Color.red);
			event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(message.build()).queue());
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood info** - Gets general info about the bot and it's current version number";
	}

	@Override
	public String getCommandId() {
		return "info";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_UTILITY;
	}
}
