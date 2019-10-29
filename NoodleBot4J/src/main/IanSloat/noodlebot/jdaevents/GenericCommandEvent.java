package main.IanSloat.noodlebot.jdaevents;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

public abstract class GenericCommandEvent extends GenericGuildEvent {
	
	public GenericCommandEvent(JDA api, long responseNumber, Guild guild) {
		super(api, responseNumber, guild);
	}

	public abstract String getCommandId();
	
}
