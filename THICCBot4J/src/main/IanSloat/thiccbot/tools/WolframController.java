package main.IanSloat.thiccbot.tools;

import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WolframController {

	private static final Logger logger = LoggerFactory.getLogger(WolframController.class);
	private WAEngine engine = new WAEngine();
	private String appID;

	public WolframController(String appID) {
		this.appID = appID;
		engine.setAppID(this.appID);
		engine.addFormat("plaintext");
	}

	public EmbedBuilder askQuestion(String question) {
		EmbedBuilder response = new EmbedBuilder();
		WAQuery query = engine.createQuery();
		query.setInput(question);
		try {
			WAQueryResult queryResult = engine.performQuery(query);
			if (queryResult.isError()) {
				System.out.println("error");
				response.setTitle("Error: Your question could not be understood");
				response.setColor(new Color(255, 0, 0));
			} else if (!queryResult.isSuccess()) {
				System.out.println("noresult");
				response.setTitle("Error: No results found");
				response.setColor(new Color(255, 0, 0));
			} else {
				logger.info("Wolfram Controller successfully retrieved response");
				for (WAPod pod : queryResult.getPods()) {
					if (!pod.isError()) {
						Object queryPod = pod.getSubpods()[0].getContents()[0];
						if (queryPod instanceof WAPlainText && ((WAPlainText) queryPod).getText().length() > 0) {
							response.addField(pod.getTitle(), ((WAPlainText) queryPod).getText(), false);
						}
					}
				}
				response.setAuthor("WolframAlpha", null, "http://thiccbot.site/boticons/wolframalphaicon.png");
				response.setColor(new Color(255, 127, 0));
			}
		} catch (WAException e) {
			e.printStackTrace();
		}
		return response;
	}

	public void askQuestionAndSend(String question, TextChannel destination) {
		class questionThread extends Thread {
			EmbedBuilder response;

			public void run() {
				response = askQuestion(question);
			}
		}
		class askingThread extends Thread {
			public void run() {
				EmbedBuilder thinking = new EmbedBuilder();
				thinking.setTitle("Thinking...");
				thinking.setColor(new Color(0, 127, 0));
				destination.sendMessage(thinking.build()).queue(new Consumer<Message>() {

					public void accept(Message t) {
						questionThread questionQuery = new questionThread();
						questionQuery.start();
						try {
							questionQuery.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						t.editMessage(questionQuery.response.build()).queue();
					}
					
				});
			}
		}
		askingThread ask = new askingThread();
		ask.start();
	}

}
