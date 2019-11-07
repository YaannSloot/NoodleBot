package main.IanSloat.noodlebot.jda.events;

import main.IanSloat.noodlebot.commands.Command;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

//TODO Document class
public class GenericCommandErrorEvent extends GenericCommandEvent {

	private String errorMessage;
	
	public GenericCommandErrorEvent(JDA api, long responseNumber, Guild guild, Command command, String input,
			Member commandIssuer, String errorMessage) {
		super(api, responseNumber, guild, command, input, commandIssuer);
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
}
