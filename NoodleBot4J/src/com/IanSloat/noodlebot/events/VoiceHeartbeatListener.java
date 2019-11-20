package com.IanSloat.noodlebot.events;

import com.IanSloat.noodlebot.controllers.lavalink.GuildLavalinkController;

import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;

public class VoiceHeartbeatListener {
	
	public void VoiceHeartbeatEvent(GenericGuildVoiceEvent event) {
		if(event.getGuild().getSelfMember().getVoiceState().inVoiceChannel())
			if(event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size() == 1) {
				GuildLavalinkController.getController(event.getGuild()).disconnect();
			}
	}
	
}
