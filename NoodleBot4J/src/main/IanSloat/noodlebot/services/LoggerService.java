package main.IanSloat.noodlebot.services;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.IanSloat.noodlebot.jdaevents.FilterDeleteCommandEvent;
import main.IanSloat.noodlebot.jdaevents.GenericCommandErrorEvent;
import main.IanSloat.noodlebot.jdaevents.GenericCommandEvent;
import main.IanSloat.noodlebot.tools.GuildSettingsManager;
import main.IanSloat.noodlebot.tools.NBMLSettingsParser;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateAfkChannelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateAfkTimeoutEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBannerEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateBoostTierEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateDescriptionEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateExplicitContentLevelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateMFALevelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateOwnerEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateRegionEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateSplashEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateSystemChannelEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateVerificationLevelEvent;
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
								channel.sendMessage(getUTCTimestamp() + " - A new bot "
										+ getUserStamp(((GuildMemberJoinEvent) event).getMember())
										+ " was added to the server").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - "
										+ getUserStamp(((GuildMemberJoinEvent) event).getMember())
										+ " has joined the server").queue();
							}
						} else if (event instanceof GuildMemberLeaveEvent) {
							((GenericGuildEvent) event).getGuild().retrieveAuditLogs().queue(auditLog -> {
								if (auditLog.size() > 0) {
									if (auditLog.get(0).getType().equals(ActionType.BAN) && auditLog.get(0)
											.getTargetId().equals(((GuildMemberLeaveEvent) event).getUser().getId())) {
										channel.sendMessage(getUTCTimestamp() + " - "
												+ getUserStamp(((GenericGuildEvent) event).getGuild()
														.getMember(auditLog.get(0).getUser()))
												+ " banned "
												+ getUserStamp(((GuildMemberLeaveEvent) event).getMember())).queue();
									} else if (auditLog.get(0).getType().equals(ActionType.KICK) && auditLog.get(0)
											.getTargetId().equals(((GuildMemberLeaveEvent) event).getUser().getId())) {
										channel.sendMessage(getUTCTimestamp() + " - "
												+ getUserStamp(((GenericGuildEvent) event).getGuild()
														.getMember(auditLog.get(0).getUser()))
												+ " kicked "
												+ getUserStamp(((GuildMemberLeaveEvent) event).getMember())).queue();
									} else {
										channel.sendMessage(getUTCTimestamp() + " - "
												+ getUserStamp(((GuildMemberLeaveEvent) event).getMember())
												+ " has left the server").queue();
									}
								} else {
									channel.sendMessage(getUTCTimestamp() + " - "
											+ getUserStamp(((GuildMemberLeaveEvent) event).getMember())
											+ " has left the server").queue();
								}
							});
						} else if (event instanceof GuildUnbanEvent) {
							((GenericGuildEvent) event).getGuild().retrieveAuditLogs().queue(auditLog -> {
								if (auditLog.size() > 0) {
									if (auditLog.get(0).getType().equals(ActionType.UNBAN) && auditLog.get(0)
											.getTargetId().equals(((GuildUnbanEvent) event).getUser().getId())) {
										channel.sendMessage(
												getUTCTimestamp() + " - "
														+ getUserStamp(((GenericGuildEvent) event)
																.getGuild().getMember(auditLog.get(0).getUser()))
														+ " unbanned "
														+ getUserStamp(((GuildUnbanEvent) event).getGuild()
																.getMember(((GuildUnbanEvent) event).getUser())))
												.queue();
									}
								}
							});
						} else if (event instanceof GuildVoiceGuildDeafenEvent) {
							if (((GuildVoiceGuildDeafenEvent) event).isGuildDeafened()) {
								channel.sendMessage(getUTCTimestamp() + " - "
										+ getUserStamp(((GuildVoiceGuildDeafenEvent) event).getMember())
										+ " was server deafened").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - "
										+ getUserStamp(((GuildVoiceGuildDeafenEvent) event).getMember())
										+ " was server undeafened").queue();
							}
						} else if (event instanceof GuildVoiceGuildMuteEvent) {
							if (((GuildVoiceGuildMuteEvent) event).isGuildMuted()) {
								channel.sendMessage(getUTCTimestamp() + " - "
										+ getUserStamp(((GuildVoiceGuildMuteEvent) event).getMember())
										+ " was server muted").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - "
										+ getUserStamp(((GuildVoiceGuildMuteEvent) event).getMember())
										+ " was server unmuted").queue();
							}
						} else if (event instanceof GuildVoiceJoinEvent) {
							channel.sendMessage(
									getUTCTimestamp() + " - " + getUserStamp(((GuildVoiceJoinEvent) event).getMember())
											+ " joined voice channel **:loud_sound:"
											+ ((GuildVoiceJoinEvent) event).getChannelJoined().getName() + "**")
									.queue();
						} else if (event instanceof GuildVoiceLeaveEvent) {
							channel.sendMessage(getUTCTimestamp() + " - " + getUserStamp(((GuildVoiceLeaveEvent) event).getMember())
									+ " left voice channel **:loud_sound:"
									+ ((GuildVoiceLeaveEvent) event).getChannelLeft().getName() + "**").queue();
						} else if (event instanceof GuildVoiceMoveEvent) {
							channel.sendMessage(
									getUTCTimestamp() + " - " + getUserStamp(((GuildVoiceMoveEvent) event).getMember())
											+ " moved from voice channel **:loud_sound:"
											+ ((GuildVoiceMoveEvent) event).getChannelLeft().getName()
											+ "** to voice channel **:loud_sound:"
											+ ((GuildVoiceMoveEvent) event).getChannelJoined().getName() + "**")
									.queue();
						} else if (event instanceof GuildVoiceSelfDeafenEvent) {
							if (((GuildVoiceSelfDeafenEvent) event).isSelfDeafened()) {
								channel.sendMessage(getUTCTimestamp() + " - "
										+ getUserStamp(((GuildVoiceSelfDeafenEvent) event).getMember())
										+ " deafened themselves").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - "
										+ getUserStamp(((GuildVoiceSelfDeafenEvent) event).getMember())
										+ " undeafened themselves").queue();
							}
						} else if (event instanceof GuildVoiceSelfMuteEvent) {
							if (((GuildVoiceSelfMuteEvent) event).isSelfMuted()) {
								channel.sendMessage(getUTCTimestamp() + " - "
										+ getUserStamp(((GuildVoiceSelfMuteEvent) event).getMember())
										+ " muted themselves").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - "
										+ getUserStamp(((GuildVoiceSelfMuteEvent) event).getMember())
										+ " unmuted themselves").queue();
							}
						} else if (event instanceof GuildMemberRoleAddEvent) {
							if (((GuildMemberRoleAddEvent) event).getRoles().size() > 1) {
								channel.sendMessage(getUTCTimestamp() + " - "
										+ getUserStamp(((GuildMemberRoleAddEvent) event).getMember()) + " was given **"
										+ ((GuildMemberRoleAddEvent) event).getRoles().size() + " new roles**").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - "
										+ getUserStamp(((GuildMemberRoleAddEvent) event).getMember())
										+ " was given the role <@&"
										+ ((GuildMemberRoleAddEvent) event).getRoles().get(0).getId() + ">").queue();
							}
						} else if (event instanceof GuildMemberRoleRemoveEvent) {
							if (((GuildMemberRoleRemoveEvent) event).getRoles().size() > 1) {
								channel.sendMessage(getUTCTimestamp() + " - **"
										+ ((GuildMemberRoleRemoveEvent) event).getRoles().size()
										+ " roles** were removed from "
										+ getUserStamp(((GuildMemberRoleRemoveEvent) event).getMember())).queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - The role <@&"
										+ ((GuildMemberRoleRemoveEvent) event).getRoles().get(0).getId()
										+ "> was removed from "
										+ getUserStamp(((GuildMemberRoleRemoveEvent) event).getMember())).queue();
							}
						} else if (event instanceof GuildMemberUpdateNicknameEvent) {
							String oldNick = "";
							String newNick = "";
							if (((GuildMemberUpdateNicknameEvent) event).getOldNickname() == null)
								oldNick = ((GuildMemberUpdateNicknameEvent) event).getUser().getName();
							else
								oldNick = ((GuildMemberUpdateNicknameEvent) event).getOldNickname();
							if (((GuildMemberUpdateNicknameEvent) event).getNewNickname() == null)
								newNick = ((GuildMemberUpdateNicknameEvent) event).getUser().getName();
							else
								newNick = ((GuildMemberUpdateNicknameEvent) event).getNewNickname();
							channel.sendMessage(getUTCTimestamp() + " - "
									+ getUserStamp(((GuildMemberUpdateNicknameEvent) event).getMember())
									+ "**\'s** nickname was changed from **" + oldNick + "** to **" + newNick + "**")
									.queue();
						} else if (event instanceof GuildUpdateAfkChannelEvent) {
							if (((GuildUpdateAfkChannelEvent) event).getNewAfkChannel() != null) {
								channel.sendMessage(getUTCTimestamp()
										+ " - Afk voice channel was changed to **:loud_sound:"
										+ ((GuildUpdateAfkChannelEvent) event).getNewAfkChannel().getName() + "**")
										.queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - Afk voice channel was disabled").queue();
							}
						} else if (event instanceof GuildUpdateAfkTimeoutEvent) {
							channel.sendMessage(getUTCTimestamp() + " - Afk timeout interval was changed to **"
									+ ((GuildUpdateAfkTimeoutEvent) event).getNewAfkTimeout().getSeconds() / 60
									+ " minutes**").queue();
						} else if (event instanceof GuildUpdateBannerEvent) {
							if (((GuildUpdateBannerEvent) event).getNewBannerId() != null) {
								channel.sendMessage(getUTCTimestamp() + " -  Server banner was changed").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - Server banner was removed").queue();
							}
						} else if (event instanceof GuildUpdateBoostTierEvent) {
							channel.sendMessage(getUTCTimestamp() + " - Server boost tier changed to **"
									+ ((GuildUpdateBoostTierEvent) event).getNewBoostTier().toString() + "**").queue();
						} else if (event instanceof GuildUpdateDescriptionEvent) {
							if (((GuildUpdateDescriptionEvent) event).getNewDescription() != null) {
								channel.sendMessage(getUTCTimestamp() + " - Server description was changed").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - Server description was removed").queue();
							}
						} else if (event instanceof GuildUpdateExplicitContentLevelEvent) {
							channel.sendMessage(getUTCTimestamp()
									+ " - Server NSFW content filtering settings were changed to **"
									+ ((GuildUpdateExplicitContentLevelEvent) event).getNewLevel().toString() + "**")
									.queue();
						} else if (event instanceof GuildUpdateIconEvent) {
							if (((GuildUpdateIconEvent) event).getNewIconId() != null) {
								channel.sendMessage(getUTCTimestamp() + " - Server icon was changed").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - Server icon was removed").queue();
							}
						} else if (event instanceof GuildUpdateMFALevelEvent) {
							channel.sendMessage(
									getUTCTimestamp() + " - Administrator 2FA verification level changed to **"
											+ ((GuildUpdateMFALevelEvent) event).getNewMFALevel().toString() + "**")
									.queue();
						} else if (event instanceof GuildUpdateNameEvent) {
							channel.sendMessage(getUTCTimestamp() + " - Server name was changed from **"
									+ ((GuildUpdateNameEvent) event).getOldName() + "** to **"
									+ ((GuildUpdateNameEvent) event).getNewName() + "**").queue();
						} else if (event instanceof GuildUpdateOwnerEvent) {
							channel.sendMessage(getUTCTimestamp() + " - Server owner was changed from "
									+ getUserStamp(((GuildUpdateOwnerEvent) event).getOldOwner()) + " to "
									+ getUserStamp(((GuildUpdateOwnerEvent) event).getNewOwner())).queue();
						} else if (event instanceof GuildUpdateRegionEvent) {
							channel.sendMessage(getUTCTimestamp() + " - Server region was changed from **"
									+ ((GuildUpdateRegionEvent) event).getOldRegionRaw() + "** to **"
									+ ((GuildUpdateRegionEvent) event).getNewRegionRaw() + "**").queue();
						} else if (event instanceof GuildUpdateSplashEvent) {
							if (((GuildUpdateSplashEvent) event).getNewSplashId() != null) {
								channel.sendMessage(getUTCTimestamp() + " - Server splash was changed").queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - Server splash was removed").queue();
							}
						} else if (event instanceof GuildUpdateSystemChannelEvent) {
							if (((GuildUpdateSystemChannelEvent) event).getNewSystemChannel() != null
									&& ((GuildUpdateSystemChannelEvent) event).getOldSystemChannel() != null) {
								channel.sendMessage(getUTCTimestamp() + " - Server system channel was changed from <#"
										+ ((GuildUpdateSystemChannelEvent) event).getOldSystemChannel().getId()
										+ "> to <#"
										+ ((GuildUpdateSystemChannelEvent) event).getNewSystemChannel().getId() + ">")
										.queue();
							} else if (((GuildUpdateSystemChannelEvent) event).getNewSystemChannel() != null) {
								channel.sendMessage(getUTCTimestamp() + " - Server system channel was changed to <#"
										+ ((GuildUpdateSystemChannelEvent) event).getNewSystemChannel().getId() + ">")
										.queue();
							} else {
								channel.sendMessage(getUTCTimestamp() + " - Server system channel was disabled")
										.queue();
							}
						} else if (event instanceof GuildUpdateVerificationLevelEvent) {
							channel.sendMessage(getUTCTimestamp() + " - Server member verification level changed to **"
									+ ((GuildUpdateVerificationLevelEvent) event).getNewVerificationLevel().toString()
									+ "**").queue();
						} else if (event instanceof FilterDeleteCommandEvent) {
							String targets = "";
							if(((FilterDeleteCommandEvent) event).getTargetMembers().size() > 0) {
								if(((FilterDeleteCommandEvent) event).getTargetMembers().size() == 1) {
									targets += "from " + getUserStamp(((FilterDeleteCommandEvent) event).getTargetMembers().get(0)) + " ";
								} else {
									targets += "from **" + ((FilterDeleteCommandEvent) event).getTargetMembers().size() + "** members ";
								}
							}
							if(((FilterDeleteCommandEvent) event).getTargetRoles().size() > 0) {
								if(targets.length() > 0) {
									targets += "and ";
								}
								if(((FilterDeleteCommandEvent) event).getTargetRoles().size() == 1) {
									targets += "from " + ((FilterDeleteCommandEvent) event).getTargetRoles().get(0).getAsMention() + " ";
								} else {
									targets += "from **" + ((FilterDeleteCommandEvent) event).getTargetRoles().size() + "** roles ";
								}
							}
							channel.sendMessage(getUTCTimestamp() + " - A new filtered delete command set to delete **"
									+ ((FilterDeleteCommandEvent) event).getTargetMessages().size()
									+ "** messages " + targets + "has been created").queue();
						} else if (event instanceof GenericCommandErrorEvent) {
							channel.sendMessage(getUTCTimestamp() + " - Command [args: **"
									+ ((GenericCommandErrorEvent) event).getInput() + "**, id: **"
									+ ((GenericCommandErrorEvent) event).getCommand().getCommandId()
									+ "**, globalid: **"
									+ ((GenericCommandErrorEvent) event).getCommand().getCommandCategory()
									+ "**] issued by "
									+ getUserStamp(((GenericCommandErrorEvent) event).getCommandIssuer())
									+ " failed due to the following error: **"
									+ ((GenericCommandErrorEvent) event).getErrorMessage() + "**").queue();
						} else if (event instanceof GenericCommandEvent) {
							channel.sendMessage(getUTCTimestamp() + " - "
									+ getUserStamp(((GenericCommandEvent) event).getCommandIssuer())
									+ " issued command [args: **" + ((GenericCommandEvent) event).getInput()
									+ "**, id: **" + ((GenericCommandEvent) event).getCommand().getCommandId()
									+ "**, globalid: **"
									+ ((GenericCommandEvent) event).getCommand().getCommandCategory() + "**]").queue();
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

	private String getUserStamp(Member user) {
		String result = "";
		result += "**" + user.getUser().getAsTag();
		NBMLSettingsParser setMgr = new GuildSettingsManager(user.getGuild()).getNBMLParser();
		setMgr.setScopePath("LoggerSettings");
		String mentionSetting = setMgr.getFirstInValGroup("LoggerMentions");
		if (!mentionSetting.equals("false")) {
			result += "(<@" + user.getId() + ">)**";
		} else {
			result += "**";
		}
		return result;
	}

}
