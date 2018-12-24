package main.IanSloat.thiccbotgui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import main.IanSloat.thiccbotgui.events.ThiccbotClient;

public class clientTest {

	public static void main(String[] args) {
		Scanner readLine = new Scanner(System.in);
		try {
			WebSocketClient client = new ThiccbotClient(new URI("ws://thiccbot.site"));
			client.connect();
			while(true) {
				try {
					client.send(readLine.nextLine());
				} catch (WebsocketNotConnectedException e) {
					System.out.println("Error: not connected to gateway. Attempting to reconnect...");
					client = new ThiccbotClient(new URI("ws://thiccbot.site"));
					client.connect();
				}
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
