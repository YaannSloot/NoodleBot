package main.IanSloat.thiccbot.tools;

import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.arsenarsen.lavaplayerbridge.player.Playlist;
import com.arsenarsen.lavaplayerbridge.player.Track;
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
		response.appendField("Now playing: ",
				'[' + track.getInfo().title + "](" + track.getInfo().uri + ')', true);
		response.appendField("Uploaded by:", track.getInfo().author, true);
		String duration = DurationFormatUtils.formatDuration(track.getInfo().length,
				"**H:mm:ss**", true);
		response.appendField("Duration: ", duration, false);
		response.withAuthorName("YouTube");
		response.withAuthorIcon("http://thiccbot.site/boticons/youtubeicon.png");
		response.withColor(238, 36, 21);
		return response.build();
	}
	
	public EmbedObject getPlaying() {
		EmbedObject result = new EmbedObject();
		if (track.getSourceManager().getSourceName().equals("youtube")) {
			result = getYouTubeEmbed();
		}
		return result;
	}
	
	public static EmbedObject generatePlaylistList(String title, List<Track> tracks) {
		EmbedBuilder response = new EmbedBuilder();
		response.setLenient(true);
		response.withTitle(title);
		response.withColor(139, 0, 139);
		String songList = "";
		if(tracks.size() > 10) {
			for(int i = 0; i <= 10; i++) {
				songList += (i + 1) + ". " + tracks.get(i).getTrack().getInfo().title + '\n';
			}
			songList += "**_And " + (tracks.size() - 10) + " more!_**";
			response.appendField("Up next:", songList, false);
		} else {
			int listPlacement = 1;
			for(Track track : tracks) {
				songList += listPlacement + ". " + track.getTrack().getInfo().title + '\n';
				listPlacement++;
			}
			response.appendField("Up next:", songList, false);
		}
		return response.build();
	}
	
	public static EmbedObject generateTracklistList(String title, List<AudioTrack> tracks) {
		EmbedBuilder response = new EmbedBuilder();
		response.setLenient(true);
		response.withTitle(title);
		response.withColor(139, 0, 139);
		String songList = "";
		if(tracks.size() > 10) {
			for(int i = 0; i <= 10; i++) {
				songList += (i + 1) + ". " + tracks.get(i).getInfo().title + '\n';
			}
			songList += "**_And " + (tracks.size() - 10) + " more!_**";
			response.appendField("Up next:", songList, false);
		} else {
			int listPlacement = 1;
			for(AudioTrack track : tracks) {
				songList += listPlacement + ". " + track.getInfo().title + '\n';
				listPlacement++;
			}
			response.appendField("Up next:", songList, false);
		}
		return response.build();
	}
	
}
