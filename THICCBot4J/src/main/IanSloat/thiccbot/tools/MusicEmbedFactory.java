package main.IanSloat.thiccbot.tools;

import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

public class MusicEmbedFactory {

	private AudioTrack track;

	public MusicEmbedFactory(AudioTrack track) {
		this.track = track;
	}

	private EmbedObject getYouTubeEmbed() {
		EmbedBuilder response = new EmbedBuilder();
		response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ')', true);
		response.appendField("Uploaded by:", track.getInfo().author, true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.appendField("Duration: ", duration, false);
		response.withAuthorName("YouTube");
		response.withAuthorIcon("http://thiccbot.site/boticons/youtubeicon.png");
		response.withColor(238, 36, 21);
		return response.build();
	}

	private EmbedObject getVimeoEmbed() {
		EmbedBuilder response = new EmbedBuilder();
		response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ')', true);
		response.appendField("Uploaded by:", track.getInfo().author, true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.appendField("Duration: ", duration, false);
		response.withAuthorName("Vimeo");
		response.withAuthorIcon("http://thiccbot.site/boticons/vimeoicon.png");
		response.withColor(26, 183, 234);
		return response.build();
	}

	private EmbedObject getTwitchEmbed() {
		EmbedBuilder response = new EmbedBuilder();
		response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ')', true);
		response.appendField("Stream by:", track.getInfo().author, true);
		String duration = "";
		if (track.getInfo().isStream) {
			duration = "**LIVE**";
		} else {
			duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		}
		response.appendField("Duration: ", duration, false);
		response.withAuthorName("Twitch");
		response.withAuthorIcon("http://thiccbot.site/boticons/twitchicon.png");
		response.withColor(142, 36, 170);
		return response.build();
	}
	
	private EmbedObject getSoundCloudEmbed() {
		EmbedBuilder response = new EmbedBuilder();
		response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ')', true);
		response.appendField("By:", track.getInfo().author, true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.appendField("Duration: ", duration, false);
		response.withAuthorName("SoundCloud");
		response.withAuthorIcon("http://thiccbot.site/boticons/soundcloudicon.png");
		response.withColor(247, 98, 14);
		return response.build();
	}

	private EmbedObject getBandcampEmbed() {
		EmbedBuilder response = new EmbedBuilder();
		response.appendField("Now playing: ", '[' + track.getInfo().title + "](" + track.getInfo().uri + ')', true);
		response.appendField("By:", track.getInfo().author, true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length, "**H:mm:ss**", true);
		response.appendField("Duration: ", duration, false);
		response.withAuthorName("bandcamp");
		response.withAuthorIcon("http://thiccbot.site/boticons/bandcampicon.png");
		response.withColor(97, 146, 156);
		return response.build();
	}
	
	public EmbedObject getPlaying() {
		EmbedObject result = new EmbedObject();
		if (track.getSourceManager().getSourceName().equals("youtube")) {
			result = getYouTubeEmbed();
		} else if (track.getSourceManager().getSourceName().equals("vimeo")) {
			result = getVimeoEmbed();
		} else if (track.getSourceManager().getSourceName().equals("twitch")) {
			result = getTwitchEmbed();
		} else if (track.getSourceManager().getSourceName().equals("soundcloud")) {
			result = getSoundCloudEmbed();
		} else if (track.getSourceManager().getSourceName().equals("bandcamp")) {
			result = getBandcampEmbed();
		} 
		return result;
	}

	public static EmbedObject generatePlaylistList(String title, List<AudioTrack> tracks) {
		EmbedBuilder response = new EmbedBuilder();
		response.setLenient(true);
		response.withTitle(title);
		response.withColor(139, 0, 139);
		String songList = "";
		if (tracks.size() > 10) {
			for (int i = 0; i < 10; i++) {
				songList += (i + 1) + ". " + tracks.get(i).getInfo().title + '\n';
			}
			songList += "**_And " + (tracks.size() - 10) + " more!_**";
			response.appendField("Up next:", songList, false);
		} else {
			int listPlacement = 1;
			for (AudioTrack track : tracks) {
				songList += listPlacement + ". " + track.getInfo().title + '\n';
				listPlacement++;
			}
			response.appendField("Up next:", songList, false);
		}
		return response.build();
	}
}
