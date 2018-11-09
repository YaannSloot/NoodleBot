package main;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.util.audio.AudioPlayer;
import threadbox.AutoLeaveCounter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.libraries.LibraryFactory;
import com.arsenarsen.lavaplayerbridge.libraries.UnknownBindingException;
import com.arsenarsen.lavaplayerbridge.player.Player;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.impl.events.shard.LoginEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;
import main.THICCBotMain;

public class Events {
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event){
    	if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)) {
	    	
    		System.out.println("Message recieved from: " + event.getAuthor().getName() + " server=" + event.getGuild().getName() + " Content=\"" + event.getMessage() + "\"");
	    	if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping"))
	            BotUtils.sendMessage(event.getChannel(), "pong");
	        
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "help")) {
	        	
	        	String help = "help - Lists available commands\n"
	        	+ "die - No u\n"
	        	+ "play <video> - Plays a youtube video. You can enter the video name or the video URL\n"
	        	+ "volume <0-2> - Changes the volume of the video thats playing. Volume ranges from 0-2\n"
	        	+ "stop - Stops the current playing video\n"
	        	+ "leave - Leaves the voice chat\n"
	        	+ "what <question> - Asks ThiccBot a question\n"
	        	+ "info - Prints info about the bot\n\n"
	        	+ "Reminder: the calling word \'thicc\' is not case sensitive\n"
	        	+ "This is to accommodate for mobile users";
	        	BotUtils.sendMessage(event.getChannel(), help);
	        }
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
	        	BotUtils.sendMessage(event.getChannel(), "no u");
	        }
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX) && BotUtils.checkForWords(event.getMessage().getContent(), THICCBotMain.questionIDs, false, true)) {
	        	System.out.println(event.getMessage().getContent().substring(8));
	        	WolframController waClient = new WolframController(THICCBotMain.waAppID);
	        	waClient.askQuestionAndSend(event.getMessage().getContent().substring(8), event.getChannel());
	        }
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "info")) {
	        	EmbedBuilder response = new EmbedBuilder();
	        	response.appendField("Current server location", "University of Illinois at Urbana-Champaign", false);
	        	response.appendField("Powered by", "Java", false);
	        	response.appendField("Bot Version", "v0.6alpha", false);
	        	response.appendField("Status", "Currently being ported from python build\nAwaiting deployment to main bot", false);
	        	response.appendField("Current shard count", event.getClient().getShardCount() + " Shards active", false);
	        	response.appendField("Current amount of threads running on server", Thread.activeCount() + " Active threads", false);
	        	response.withTitle("Bot Info");
	        	response.withColor(0, 255, 0);
	        	RequestBuffer.request(() -> event.getChannel().sendMessage(response.build()));
	        }
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")) {
	        	IVoiceChannel voiceChannel = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();
	        	if(voiceChannel != null) {
	        		voiceChannel.join();
	        		String videoURL = event.getMessage().getContent().substring(13);
	        		String directory = System.getProperty("user.home");
	        		new File(directory + "/thicctemp").mkdirs();
	        		YoutubeDLRequest request = new YoutubeDLRequest('\"' + videoURL + '\"', directory + "/thicctemp");
	        		request.setOption("default-search", "auto");
	        		request.setOption("format", "mp3/bestaudio");
	        		request.setOption("print-json");
	        		request.setOption("no-playlist");
	        		request.setOption("output", event.getGuild().getStringID() + ".mp3");
	        		YoutubeDLResponse response;
	        		/*try {
						PlayerManager manager = PlayerManager.getPlayerManager(LibraryFactory.getLibrary(event.getClient()));
						manager.getManager();
						
						Player newPlayer = manager.getPlayer(event.getGuild().getStringID());
						newPlayer.stop();
						try {
							newPlayer.resolve(event.getMessage().getContent().substring(13));
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					} catch (UnknownBindingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	        		*/
					try {
						PlayerManager manager;
						manager = PlayerManager.getPlayerManager(LibraryFactory.getLibrary(event.getClient()));
						manager.getManager();
						Player newPlayer = manager.getPlayer(event.getGuild().getStringID());
						newPlayer.stop();
						new File(directory + "/thicctemp/" + event.getGuild().getStringID() + ".mp3").delete();
						response = YoutubeDL.execute(request);
						System.out.println("Request performed");
						System.out.println(response.getOut());
						ytdlOutputProcessor vInfo = new ytdlOutputProcessor(response.getOut());
						System.out.println(vInfo.getUploader());
						System.out.println(vInfo.getVideoUrl());
						System.out.println(vInfo.getDuration());
						newPlayer.queue((AudioTrack) new File(directory + "/thicctemp/" + event.getGuild().getStringID() + ".mp3"));
					} catch (YoutubeDLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnknownBindingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        }
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "leave")) {
	        	IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
	        	if(voiceChannel != null) {
	        		voiceChannel.leave();
	        	}
	        }
    	}
    }
    @EventSubscriber
    public void onBotLogin(LoginEvent event){
		System.out.println("Logged in.");
		event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, "thicc4j help");
	}
    @EventSubscriber
    public void onUserLeavesVoice(UserVoiceChannelLeaveEvent event) {
    	try {
	    	if(event.getGuild().getConnectedVoiceChannel().getStringID().equals(event.getVoiceChannel().getStringID())) {
	    		System.out.println("User: " + event.getUser().getName() + "(id:" + event.getUser().getStringID() + ')' + " disconnected from connected voice channel on guild \"" + event.getGuild().getName() + "\"(id:" + event.getGuild().getLongID() + "). Remaining users: " + (event.getVoiceChannel().getConnectedUsers().size() - 1));
	    		if(event.getVoiceChannel().getConnectedUsers().size() == 1) {
	    			System.out.println("No more users are currently connected. Auto-Leave countdown has been started.");
	    			AutoLeaveCounter counter = new AutoLeaveCounter(event.getGuild().getConnectedVoiceChannel());
	    			counter.start();
	    		}
	    	}
    	} catch (NullPointerException e) {}
    }
    @EventSubscriber
    public void onUserMovesOutOfVoice(UserVoiceChannelMoveEvent event) {
    	try {
	    	if(event.getGuild().getConnectedVoiceChannel().getStringID().equals(event.getOldChannel().getStringID())) {
	    		System.out.println("User: " + event.getUser().getName() + "(id:" + event.getUser().getStringID() + ')' + " moved out of connected voice channel on guild \"" + event.getGuild().getName() + "\"(id:" + event.getGuild().getLongID() + "). Remaining users: " + (event.getOldChannel().getConnectedUsers().size() - 1));
	    		if(event.getOldChannel().getConnectedUsers().size() == 1) {
	    			System.out.println("No more users are currently connected. Auto-Leave countdown has been started.");
	    			AutoLeaveCounter counter = new AutoLeaveCounter(event.getGuild().getConnectedVoiceChannel());
	    			counter.start();
	    		}
	    	}
	    	else if(event.getGuild().getConnectedVoiceChannel().getStringID().equals(event.getNewChannel().getStringID())) {
	    		System.out.println("User: " + event.getUser().getName() + "(id:" + event.getUser().getStringID() + ')' + " moved in to connected voice channel on guild \"" + event.getGuild().getName() + "\"(id:" + event.getGuild().getLongID() + "). Remaining users: " + (event.getNewChannel().getConnectedUsers().size() - 1));
	    		for(AutoLeaveCounter counter : AutoLeaveCounter.getAllRunningCounters()) {
	    			if(counter.getChannel().getStringID().equals(event.getGuild().getConnectedVoiceChannel().getStringID())) {
	    				counter.stopCountDown();
	    				System.out.println("Auto-Leave counter has been stopped.");
	    			}
	    		}
	    	}
    	} catch (NullPointerException e) {}
    }
}
