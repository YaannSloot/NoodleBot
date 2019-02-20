package main.IanSloat.thiccbot.tools;

import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

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
		response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		response.appendField("Uploaded by:", track.getInfo().author, true);
		String duration = "";
		if (track.getInfo().isStream) {
			duration = "**LIVE**";
		} else {
			duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		}
		response.appendField("Duration: ", duration, false);
		response.appendField("Current volume: ", volume + "%", false);
		response.withAuthorName("YouTube");
		response.withAuthorIcon("http://thiccbot.site/boticons/youtubeicon.png");
		response.withColor(238, 36, 21);
		return response;
	}

	private EmbedBuilder getVimeoEmbed(int volume) {
		EmbedBuilder response = new EmbedBuilder();
		response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		response.appendField("Uploaded by:", track.getInfo().author, true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.appendField("Duration: ", duration, false);
		response.appendField("Current volume: ", volume + "%", false);
		response.withAuthorName("Vimeo");
		response.withAuthorIcon("http://thiccbot.site/boticons/vimeoicon.png");
		response.withColor(26, 183, 234);
		return response;
	}

	private EmbedBuilder getTwitchEmbed(int volume) {
		EmbedBuilder response = new EmbedBuilder();
		response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		response.appendField("Stream by:", track.getInfo().author, true);
		String duration = "";
		if (track.getInfo().isStream) {
			duration = "**LIVE**";
		} else {
			duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		}
		response.appendField("Duration: ", duration, false);
		response.appendField("Current volume: ", volume + "%", false);
		response.withAuthorName("Twitch");
		response.withAuthorIcon("http://thiccbot.site/boticons/twitchicon.png");
		response.withColor(142, 36, 170);
		return response;
	}

	private EmbedBuilder getSoundCloudEmbed(int volume) {
		EmbedBuilder response = new EmbedBuilder();
		response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		response.appendField("By:", track.getInfo().author, true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.appendField("Duration: ", duration, false);
		response.appendField("Current volume: ", volume + "%", false);
		response.withAuthorName("SoundCloud");
		response.withAuthorIcon("http://thiccbot.site/boticons/soundcloudicon.png");
		response.withColor(247, 98, 14);
		return response;
	}

	private EmbedBuilder getBandcampEmbed(int volume) {
		EmbedBuilder response = new EmbedBuilder();
		response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		response.appendField("By:", track.getInfo().author, true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.appendField("Duration: ", duration, false);
		response.appendField("Current volume: ", volume + "%", false);
		response.withAuthorName("bandcamp");
		response.withAuthorIcon("http://thiccbot.site/boticons/bandcampicon.png");
		response.withColor(97, 146, 156);
		return response;
	}
	
	private EmbedBuilder getGenericEmbed(int volume) {
		EmbedBuilder response = new EmbedBuilder();
		response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ")\n", true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.appendField("Duration: ", duration, false);
		response.appendField("Current volume: ", volume + "%", false);
		response.withAuthorName("Music Track");
		response.withAuthorIcon("http://thiccbot.site/boticons/generic.png");
		response.withColor(0, 0, 0);
		return response;
	}

	/**
	 * Generates embedded content based on the EmbedFactory's AudioTrack object
	 * @return the EmbedObject message created off of the AudioTrack
	 */
	public EmbedObject getPlaying(boolean AttachPlaylist, List<AudioTrack> tracks, int volume) {
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
				result.appendField("Up next:", songList, false);
			} else {
				int listPlacement = 1;
				for (AudioTrack track : tracks) {
					songList += listPlacement + ". " + track.getInfo().title + '\n';
					listPlacement++;
				}
				result.appendField("Up next:", songList, false);
			}
		}
		
		return result.build();
	}

}
