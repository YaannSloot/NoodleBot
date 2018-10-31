package main;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

class BotUtils {

    // Constants for use throughout the bot
    static String BOT_PREFIX = "thicc4j ";

    // Handles the creation and getting of a IDiscordClient object for a token
    static IDiscordClient getBuiltDiscordClient(String token){

        // The ClientBuilder object is where you will attach your params for configuring the instance of your bot.
        // Such as withToken, setDaemon etc
        return new ClientBuilder()
                .withToken(token)
                .withRecommendedShardCount()
                .build();

    }

    // Helper functions to make certain aspects of the bot easier to use.
    static void sendMessage(IChannel channel, String message){

        // This might look weird but it'll be explained in another page.
        RequestBuffer.request(() -> {
            try{
                channel.sendMessage(message);
            } catch (DiscordException e){
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
            }
        });

        /*
        // The below example is written to demonstrate sending a message if you want to catch the RLE for logging purposes
        RequestBuffer.request(() -> {
            try{
                channel.sendMessage(message);
            } catch (RateLimitException e){
                System.out.println("Do some logging");
                throw e;
            }
        });
        */

    }
    
    static boolean checkForWords(String input, String[] list, boolean isCaseSensitive, boolean insertSpaces) {
    	boolean isTrue = false;
    	if(!isCaseSensitive) {
    		input = input.toLowerCase();
    		for(int i = 0; i < list.length; i++) list[i] = list[i].toLowerCase();
    	}
    	for(String word : list) {
    		if(insertSpaces) word = " " + word + " ";
    		if(input.contains(word)) isTrue = true;
    	}
    	return isTrue;
    }
    
    static boolean checkForWords(String input, String[] list, boolean isCaseSensitive) {
    	boolean isTrue = false;
    	if(!isCaseSensitive) {
    		input = input.toLowerCase();
    		for(int i = 0; i < list.length; i++) list[i] = list[i].toLowerCase();
    	}
    	for(String word : list) if(input.contains(word)) isTrue = true;
    	return isTrue;
    }
    
    static boolean checkForWords(String input, String[] list) {
    	boolean isTrue = false;
    	for(String word : list) if(input.contains(word)) isTrue = true;
    	return isTrue;
    }
}
