package com.IanSloat.noodlebot.events;

import com.IanSloat.noodlebot.controllers.lavalink.GuildLavalinkController;

import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;

/**
 * Monitors voice channel activity so that the bot can leave a channel that is
 * no longer in use.
 */
public class VoiceHeartbeatListener {

	public void VoiceHeartbeatEvent(GenericGuildVoiceEvent event) {
		if (event.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
			if (event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size() == 1)
				GuildLavalinkController.getController(event.getGuild()).disconnect();
		} else
			GuildLavalinkController.getController(event.getGuild()).resetQueue();
	}

}
