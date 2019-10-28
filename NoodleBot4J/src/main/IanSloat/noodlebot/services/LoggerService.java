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
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfMuteEvent;

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
							if (((GuildMemberJoinEvent) event).getUser().isBot()) {
								channel.sendMessage(getUTCTimestamp() + " - A new bot **"
										+ ((GuildMemberJoinEvent) event).getUser().getAsTag() + "(<@"
										+ ((GuildMemberJoinEvent) event).getUser().getId() + ">)"
										+ "** was added to the server").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildMemberJoinEvent) event).getUser().getAsTag() + "(<@"
										+ ((GuildMemberJoinEvent) event).getUser().getId() + ">)**"
										+ " has joined the server").queue();
							}
						} else if (event instanceof GuildMemberLeaveEvent) {
							((GenericGuildEvent) event).getGuild().retrieveAuditLogs().queue(auditLog -> {
								if (auditLog.size() > 0) {
									if (auditLog.get(0).getType().equals(ActionType.BAN) && auditLog.get(0)
											.getTargetId().equals(((GuildMemberLeaveEvent) event).getUser().getId())) {
										channel.sendMessage(getUTCTimestamp() + " - **"
												+ auditLog.get(0).getUser().getAsTag() + "(<@"
												+ auditLog.get(0).getUser().getId() + ">)" + "** banned **"
												+ ((GuildMemberLeaveEvent) event).getUser().getAsTag() + "(<@"
												+ ((GuildMemberLeaveEvent) event).getUser().getId() + ">)**").queue();
									} else if (auditLog.get(0).getType().equals(ActionType.KICK) && auditLog.get(0)
											.getTargetId().equals(((GuildMemberLeaveEvent) event).getUser().getId())) {
										channel.sendMessage(getUTCTimestamp() + " - **"
												+ auditLog.get(0).getUser().getAsTag() + "(<@"
												+ auditLog.get(0).getUser().getId() + ">)" + "** kicked **"
												+ ((GuildMemberLeaveEvent) event).getUser().getAsTag() + "(<@"
												+ ((GuildMemberLeaveEvent) event).getUser().getId() + ">)**").queue();
									} else {
										channel.sendMessage(getUTCTimestamp() + " - **"
												+ ((GuildMemberLeaveEvent) event).getUser().getAsTag() + "(<@"
												+ ((GuildMemberLeaveEvent) event).getUser().getId() + ">)"
												+ "** has left the server").queue();
									}
								} else {
									channel.sendMessage(getUTCTimestamp() + " - **"
											+ ((GuildMemberLeaveEvent) event).getUser().getAsTag() + "(<@"
											+ ((GuildMemberLeaveEvent) event).getUser().getId() + ">)"
											+ "** has left the server").queue();
								}
							});
						} else if (event instanceof GuildUnbanEvent) {
							((GenericGuildEvent) event).getGuild().retrieveAuditLogs().queue(auditLog -> {
								if (auditLog.size() > 0) {
									if (auditLog.get(0).getType().equals(ActionType.UNBAN) && auditLog.get(0)
											.getTargetId().equals(((GuildUnbanEvent) event).getUser().getId())) {
										channel.sendMessage(getUTCTimestamp() + " - **"
												+ auditLog.get(0).getUser().getAsTag() + "(<@"
												+ auditLog.get(0).getUser().getId() + ">)" + "** unbanned **"
												+ ((GuildUnbanEvent) event).getUser().getAsTag() + "(<@"
												+ ((GuildUnbanEvent) event).getUser().getId() + ">)**").queue();
									}
								}
							});
						} else if (event instanceof GuildVoiceGuildDeafenEvent) {
							if (((GuildVoiceGuildDeafenEvent) event).isGuildDeafened()) {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildVoiceGuildDeafenEvent) event).getMember().getUser().getAsTag() + "(<@"
										+ ((GuildVoiceGuildDeafenEvent) event).getMember().getId()
										+ ">)** was server deafened").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildVoiceGuildDeafenEvent) event).getMember().getUser().getAsTag() + "(<@"
										+ ((GuildVoiceGuildDeafenEvent) event).getMember().getId()
										+ ">)** was server undeafened").queue();
							}
						} else if (event instanceof GuildVoiceGuildMuteEvent) {
							if (((GuildVoiceGuildMuteEvent) event).isGuildMuted()) {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildVoiceGuildMuteEvent) event).getMember().getUser().getAsTag() + "(<@"
										+ ((GuildVoiceGuildMuteEvent) event).getMember().getId()
										+ ">)** was server muted").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildVoiceGuildMuteEvent) event).getMember().getUser().getAsTag() + "(<@"
										+ ((GuildVoiceGuildMuteEvent) event).getMember().getId()
										+ ">)** was server unmuted").queue();
							}
						} else if (event instanceof GuildVoiceJoinEvent) {
							channel.sendMessage(getUTCTimestamp() + " - **"
									+ ((GuildVoiceJoinEvent) event).getMember().getUser().getAsTag() + "(<@"
									+ ((GuildVoiceJoinEvent) event).getMember().getId()
									+ ">)** joined voice channel **:loud_sound:"
									+ ((GuildVoiceJoinEvent) event).getChannelJoined().getName() + "**").queue();
						} else if (event instanceof GuildVoiceLeaveEvent) {
							channel.sendMessage(getUTCTimestamp() + " - **"
									+ ((GuildVoiceLeaveEvent) event).getMember().getUser().getAsTag() + "(<@"
									+ ((GuildVoiceLeaveEvent) event).getMember().getId()
									+ ">)** left voice channel **:loud_sound:"
									+ ((GuildVoiceLeaveEvent) event).getChannelLeft().getName() + "**").queue();
						} else if (event instanceof GuildVoiceMoveEvent) {
							channel.sendMessage(getUTCTimestamp() + " - **"
									+ ((GuildVoiceMoveEvent) event).getMember().getUser().getAsTag() + "(<@"
									+ ((GuildVoiceMoveEvent) event).getMember().getId()
									+ ">)** moved from voice channel **:loud_sound:"
									+ ((GuildVoiceMoveEvent) event).getChannelLeft().getName()
									+ "** to voice channel **:loud_sound:"
									+ ((GuildVoiceMoveEvent) event).getChannelJoined().getName() + "**").queue();
						} else if (event instanceof GuildVoiceSelfDeafenEvent) {
							if (((GuildVoiceSelfDeafenEvent) event).isSelfDeafened()) {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildVoiceSelfDeafenEvent) event).getMember().getUser().getAsTag() + "(<@"
										+ ((GuildVoiceSelfDeafenEvent) event).getMember().getId()
										+ ">)** deafened themselves").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildVoiceSelfDeafenEvent) event).getMember().getUser().getAsTag() + "(<@"
										+ ((GuildVoiceSelfDeafenEvent) event).getMember().getId()
										+ ">)** undeafened themselves").queue();
							}
						} else if (event instanceof GuildVoiceSelfMuteEvent) {
							if (((GuildVoiceSelfMuteEvent) event).isSelfMuted()) {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildVoiceSelfMuteEvent) event).getMember().getUser().getAsTag() + "(<@"
										+ ((GuildVoiceSelfMuteEvent) event).getMember().getId()
										+ ">)** muted themselves").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildVoiceSelfMuteEvent) event).getMember().getUser().getAsTag() + "(<@"
										+ ((GuildVoiceSelfMuteEvent) event).getMember().getId()
										+ ">)** unmuted themselves").queue();
							}
						} else if (event instanceof GuildMemberRoleAddEvent) {
							if (((GuildMemberRoleAddEvent) event).getRoles().size() > 1) {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildMemberRoleAddEvent) event).getUser().getAsTag() + "(<@"
										+ ((GuildMemberRoleAddEvent) event).getMember().getId() + ">)** was given **"
										+ ((GuildMemberRoleAddEvent) event).getRoles().size() + " new roles**").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildMemberRoleAddEvent) event).getUser().getAsTag() + "(<@"
										+ ((GuildMemberRoleAddEvent) event).getMember().getId()
										+ ">)** was given the role <@&"
										+ ((GuildMemberRoleAddEvent) event).getRoles().get(0).getId() + ">").queue();
							}
						} else if (event instanceof GuildMemberRoleRemoveEvent) {
							if (((GuildMemberRoleRemoveEvent) event).getRoles().size() > 1) {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildMemberRoleRemoveEvent) event).getRoles().size()
										+ " roles** were removed from **"
										+ ((GuildMemberRoleRemoveEvent) event).getUser().getAsTag() + "(<@"
										+ ((GuildMemberRoleRemoveEvent) event).getMember().getId() + ">)**").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - The role <@&"
										+ ((GuildMemberRoleRemoveEvent) event).getRoles().get(0).getId()
										+ "> was removed from **"
										+ ((GuildMemberRoleRemoveEvent) event).getUser().getAsTag() + "(<@"
										+ ((GuildMemberRoleRemoveEvent) event).getMember().getId() + ">)**").queue();
							}
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
