package main.IanSloat.noodlebot.gateway.events.guest;

import org.json.JSONObject;

import main.IanSloat.noodlebot.gateway.events.Event;
import main.IanSloat.noodlebot.gateway.sessions.Session;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;

public class shardStatRequestEvent extends Event {

	private JDA shard;
	
	public shardStatRequestEvent(Session conn, JSONObject message) {
		super(conn, message);
		this.shard = conn.getShardManager().getShardById(message.getInt("shardid"));
	}

	public JDA getShard() {
		return this.shard;
	}
	
	public boolean isShardOnline() {
		return !shard.getStatus().equals(Status.DISCONNECTED);
	}
	
	public int getShardGuildCount() {
		return shard.getGuilds().size();
	}
	
}
