package main.IanSloat.thiccbot.commands;

import java.awt.Color;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.ThiccBotMain;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class InfoCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.INFO, user);
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
		event.getMessage().delete().queue();
		EmbedBuilder response = new EmbedBuilder();
		response.addField("Powered by", "Java", false);
		response.addField("Bot Version", ThiccBotMain.botVersion + "\n(Release #).(feature version).(patch #)", false);
		response.addField("Status", ThiccBotMain.devMsg, false);
		response.addField("Current number of guilds bot is a member of", ThiccBotMain.shardmgr.getGuilds().size() + " guilds", false);
		response.addField("Current shard count", event.getJDA().getShardInfo().getShardTotal() + " Shards active", false);
		response.addField("Current amount of threads running on server", Thread.activeCount() + " Active threads",
				false);
		response.setTitle("Bot Info");
		response.setColor(new Color(0, 255, 0));
		event.getChannel().sendMessage(response.build()).queue();
	}
}
