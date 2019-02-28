package main.IanSloat.thiccbot.commands;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.ThiccBotMain;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class InfoCommand extends Command {

	@Override
	public boolean CheckUsagePermission(IUser user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.INFO, user);
	}

	@Override
	public boolean CheckForCommandMatch(IMessage command) {
		return command.getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "info");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		RequestBuffer.request(() -> event.getMessage().delete());
		EmbedBuilder response = new EmbedBuilder();
		if (!(ThiccBotMain.locator.getIPAddress().equals("")))
			response.appendField("Current server location", ThiccBotMain.locator.getCity() + ", "
					+ ThiccBotMain.locator.getRegion() + ", " + ThiccBotMain.locator.getCountry(), false);
		response.appendField("Powered by", "Java", false);
		response.appendField("Bot Version", ThiccBotMain.botVersion, false);
		response.appendField("Status", ThiccBotMain.devMsg, false);
		response.appendField("Current shard count", event.getClient().getShardCount() + " Shards active", false);
		response.appendField("Current amount of threads running on server", Thread.activeCount() + " Active threads",
				false);
		response.withTitle("Bot Info");
		response.withColor(0, 255, 0);
		RequestBuffer.request(() -> event.getChannel().sendMessage(response.build()));
	}
}
