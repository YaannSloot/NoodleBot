package com.IanSloat.noodlebot.tools;

import java.awt.Color;
import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

// TODO Update documentation and finish for other undocumented methods

/**
 * Standard core ThiccBot class used to create Discord4J embed objects based on
 * a provided lavaplayer audio track
 * 
 * @author Ian Sloat
 *
 */
public class MusicEmbedFactory {

	private AudioTrack track;

	/**
	 * Creates a new MusicEmbedFactory embedded message generator
	 * @param track the lavaplayer audio track to reference when creating embedded content
	 */
	public MusicEmbedFactory(AudioTrack track) {
		this.track = track;
	}

	private EmbedBuilder getYouTubeEmbed(int volume) {
		EmbedBuilder response = new EmbedBuilder();
		response.addField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		response.addField("Uploaded by:", track.getInfo().author, true);
		String duration = "";
		if (track.getInfo().isStream) {
			duration = "**LIVE**";
		} else {
			duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		}
		response.addField("Duration: ", duration, false);
		response.addField("Current volume: ", volume + "%", false);
		response.setAuthor("YouTube", null, "https://www.dropbox.com/s/fc3c205wed6tl0l/youtubeicon.png?dl=1");
		response.setColor(new Color(238, 36, 21));
		return response;
	}

	private EmbedBuilder getVimeoEmbed(int volume) {
		EmbedBuilder response = new EmbedBuilder();
		response.addField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		response.addField("Uploaded by:", track.getInfo().author, true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.addField("Duration: ", duration, false);
		response.addField("Current volume: ", volume + "%", false);
		response.setAuthor("Vimeo", null, "https://www.dropbox.com/s/pq58eeszgjsntvc/vimeoicon.png?dl=1");
		response.setColor(new Color(26, 183, 234));
		return response;
	}

	private EmbedBuilder getTwitchEmbed(int volume) {
		EmbedBuilder response = new EmbedBuilder();
		response.addField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		response.addField("Stream by:", track.getInfo().author, true);
		String duration = "";
		if (track.getInfo().isStream) {
			duration = "**LIVE**";
		} else {
			duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		}
		response.addField("Duration: ", duration, false);
		response.addField("Current volume: ", volume + "%", false);
		response.setAuthor("Twitch", null, "https://www.dropbox.com/s/upr0qp6yd46bkw1/twitchicon.png?dl=1");
		response.setColor(new Color(142, 36, 170));
		return response;
	}

	private EmbedBuilder getSoundCloudEmbed(int volume) {
		EmbedBuilder response = new EmbedBuilder();
		response.addField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		response.addField("By:", track.getInfo().author, true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.addField("Duration: ", duration, false);
		response.addField("Current volume: ", volume + "%", false);
		response.setAuthor("SoundCloud", null, "https://www.dropbox.com/s/e6yj0h1nfkjbc9k/soundcloudicon.png?dl=1");
		response.setColor(new Color(247, 98, 14));
		return response;
	}

	private EmbedBuilder getBandcampEmbed(int volume) {
		EmbedBuilder response = new EmbedBuilder();
		response.addField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		response.addField("By:", track.getInfo().author, true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.addField("Duration: ", duration, false);
		response.addField("Current volume: ", volume + "%", false);
		response.setAuthor("bandcamp", null, "https://www.dropbox.com/s/uwkjuu3e6lck5ec/bandcampicon.png?dl=1");
		response.setColor(new Color(97, 146, 156));
		return response;
	}
	
	private EmbedBuilder getGenericEmbed(int volume) {
		EmbedBuilder response = new EmbedBuilder();
		response.addField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.addField("Duration: ", duration, false);
		response.addField("Current volume: ", volume + "%", false);
		response.setAuthor("Music Track", null, "https://www.dropbox.com/s/0id8stlif05i3ym/generic.png?dl=1");
		response.setColor(new Color(0, 0, 0));
		return response;
	}

	/**
	 * Generates embedded content based on the EmbedFactory's AudioTrack object
	 * @return the EmbedObject message created off of the AudioTrack
	 */
	public MessageEmbed getPlaying(boolean AttachPlaylist, List<AudioTrack> tracks, int volume) {
		EmbedBuilder result = new EmbedBuilder();
		if (track.getSourceManager().getSourceName().equals("youtube")) {
			result = getYouTubeEmbed(volume);
		} else if (track.getSourceManager().getSourceName().equals("vimeo")) {
			result = getVimeoEmbed(volume);
		} else if (track.getSourceManager().getSourceName().equals("twitch")) {
			result = getTwitchEmbed(volume);
		} else if (track.getSourceManager().getSourceName().equals("soundcloud")) {
			result = getSoundCloudEmbed(volume);
		} else if (track.getSourceManager().getSourceName().equals("bandcamp")) {
			result = getBandcampEmbed(volume);
		} else {
			result = getGenericEmbed(volume);
		}
		
		if(AttachPlaylist && tracks.size() > 0) {
			String songList = "";
			if (tracks.size() > 10) {
				for (int i = 0; i < 10; i++) {
					songList += (i + 1) + ". " + tracks.get(i).getInfo().title + '\n';
				}
				songList += "**_And " + (tracks.size() - 10) + " more!_**";
				result.addField("Up next:", songList, false);
			} else {
				int listPlacement = 1;
				for (AudioTrack track : tracks) {
					songList += listPlacement + ". " + track.getInfo().title + '\n';
					listPlacement++;
				}
				result.addField("Up next:", songList, false);
			}
		}
		
		return result.build();
	}

}
