package main.IanSloat.thiccbot.tools;
import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WolframController {
	
	private final Logger logger = LoggerFactory.getLogger(WolframController.class);
	private WAEngine engine = new WAEngine();
	private String appID;
	private String serverIP;
	
	public WolframController(String appID){
		this.appID = appID;
		engine.setAppID(this.appID);
		engine.addFormat("plaintext");
		serverIP = engine.getIP();
	}
	
	public EmbedBuilder askQuestion(String question) {
		EmbedBuilder response = new EmbedBuilder();
		WAQuery query = engine.createQuery();
        query.setInput(question);
		try {
        	WAQueryResult queryResult = engine.performQuery(query);
        	if (queryResult.isError()) {
                System.out.println("error");
                response.withTitle("Error: Your question could not be understood");
                response.withColor(255, 0, 0);
        	}
        	else if (!queryResult.isSuccess()) {
        		System.out.println("noresult");
        		response.withTitle("Error: No results found");
                response.withColor(255, 0, 0);
        	}
        	else {
        		logger.info("Wolfram Controller successfully retrieved response");
                for (WAPod pod : queryResult.getPods()) {
                    if (!pod.isError()) {
                    	Object queryPod = pod.getSubpods()[0].getContents()[0];
                    	if (queryPod instanceof WAPlainText && ((WAPlainText) queryPod).getText().length() > 0) {
                        	response.appendField(pod.getTitle(), ((WAPlainText) queryPod).getText(), false);
                        }
                    }
                }
                response.withAuthorName("WolframAlpha");
                response.withAuthorIcon("http://thiccbot.site/boticons/wolframalphaicon.png");
                response.withColor(255, 127, 0);
        	}
        }
        catch (WAException e) {
        	e.printStackTrace();
        }
		return response;
	}
	
	public void askQuestionAndSend(String question, IChannel destination) {
		class questionThread extends Thread{
			EmbedBuilder response;
			public void run() {
				response = askQuestion(question);
			}
		}
		class askingThread extends Thread {
			public void run() {
				EmbedBuilder thinking = new EmbedBuilder();
				thinking.withTitle("Thinking...");
				thinking.withColor(0, 127, 0);
				IMessage message = destination.sendMessage(thinking.build());
				questionThread questionQuery = new questionThread();
				questionQuery.start();
				try {
					questionQuery.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				RequestBuffer.request(() -> message.edit(questionQuery.response.build()));
			}
		}
		askingThread ask = new askingThread();
		ask.start();
	}
	
	public String getServerIP() {
		return serverIP;
	}
	
}
