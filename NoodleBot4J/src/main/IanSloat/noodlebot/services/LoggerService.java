package main.IanSloat.noodlebot.services;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;

public class LoggerService {

	private final Logger logger = LoggerFactory.getLogger(LoggerService.class);

	public void logEvent(GenericEvent event) {
		if (event instanceof GenericGuildEvent) {
			NBMLSettingsParser setMgr = new GuildSettingsManager(((GenericGuildEvent) event).getGuild())
					.getNBMLParser();
			setMgr.setScopePath("LoggerSettings");
			String setting = setMgr.getFirstInValGroup("LoggerChannel");
			if (!setting.equals("")) {
				try {
					long channelId = Long.parseLong(setting);
					TextChannel channel = ((GenericGuildEvent) event).getGuild().getTextChannelById(channelId);
					if (channel != null) {

						// Event mapping
						if (event instanceof GuildMemberJoinEvent) {
							channel.sendMessage(
									getUTCTimestamp() + " - " + ((GuildMemberJoinEvent) event).getUser().getAsTag()
											+ "(<@" + ((GuildMemberJoinEvent) event).getUser().getId() + ">)"
											+ " has joined the server")
									.queue();
						} else if (event instanceof GuildMemberLeaveEvent) {
							((GenericGuildEvent) event).getGuild().retrieveAuditLogs().queue(auditLog -> {
								if (auditLog.size() > 0) {
									if (auditLog.get(0).getType().equals(ActionType.BAN) && auditLog.get(0)
											.getTargetId().equals(((GuildMemberLeaveEvent) event).getUser().getId())) {
										channel.sendMessage(getUTCTimestamp() + " - "
												+ auditLog.get(0).getUser().getAsTag() + "(<@"
												+ auditLog.get(0).getUser().getId() + ">)" + " banned "
												+ ((GuildMemberLeaveEvent) event).getUser().getAsTag() + "(<@"
												+ ((GuildMemberLeaveEvent) event).getUser().getId() + ">)").queue();
									} else if (auditLog.get(0).getType().equals(ActionType.KICK) && auditLog.get(0)
											.getTargetId().equals(((GuildMemberLeaveEvent) event).getUser().getId())) {
										channel.sendMessage(getUTCTimestamp() + " - "
												+ auditLog.get(0).getUser().getAsTag() + "(<@"
												+ auditLog.get(0).getUser().getId() + ">)" + " kicked "
												+ ((GuildMemberLeaveEvent) event).getUser().getAsTag() + "(<@"
												+ ((GuildMemberLeaveEvent) event).getUser().getId() + ">)").queue();
									} else {
										channel.sendMessage(getUTCTimestamp() + " - "
												+ ((GuildMemberLeaveEvent) event).getUser().getAsTag() + "(<@"
												+ ((GuildMemberLeaveEvent) event).getUser().getId() + ">)"
												+ " has left the server").queue();
									}
								} else {
									channel.sendMessage(getUTCTimestamp() + " - "
											+ ((GuildMemberLeaveEvent) event).getUser().getAsTag() + "(<@"
											+ ((GuildMemberLeaveEvent) event).getUser().getId() + ">)"
											+ " has left the server").queue();
								}
							});
						} else if (event instanceof GuildUnbanEvent) {
							((GenericGuildEvent) event).getGuild().retrieveAuditLogs().queue(auditLog -> {
								if (auditLog.size() > 0) {
									if (auditLog.get(0).getType().equals(ActionType.UNBAN) && auditLog.get(0)
											.getTargetId().equals(((GuildUnbanEvent) event).getUser().getId())) {
										channel.sendMessage(getUTCTimestamp() + " - "
												+ auditLog.get(0).getUser().getAsTag() + "(<@"
												+ auditLog.get(0).getUser().getId() + ">)" + " unbanned "
												+ ((GuildUnbanEvent) event).getUser().getAsTag() + "(<@"
												+ ((GuildUnbanEvent) event).getUser().getId() + ">)").queue();
									}
								}
							});
						}

					} else {
						logger.warn("[Guild \" " + ((GenericGuildEvent) event).getGuild().getName() + "\":id="
								+ ((GenericGuildEvent) event).getGuild().getId()
								+ "] Specified channel in settings file is not a text channel. Settings for the logger service have been cleared");
						setMgr.removeValGroup("LoggerChannel");
					}
				} catch (NumberFormatException e) {
					logger.warn("[Guild \" " + ((GenericGuildEvent) event).getGuild().getName() + "\":id="
							+ ((GenericGuildEvent) event).getGuild().getId()
							+ "] Attempted to convert setting to long for LoggerChannel but failed due to a formatting error");
				}
			}
		}
	}

	private String getUTCTimestamp() {
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		return "(UTC " + formatter.format(Date.from(Instant.now())) + ")";
	}

}
