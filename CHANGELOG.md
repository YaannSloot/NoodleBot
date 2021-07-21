# Changelog (as of beta v1.1.12):
### Version number = (Release #).(feature version).(patch #)

## Beta v1.1.12

\+ Updated wiki command to do nsfw checking

## Beta v1.1.11

\+ Stability patch

## Beta v1.1.10

\+ Updated help command to work regardless of any permissions present. If the bot is missing the minimum required permissions, the help command will warn of what is missing

## Beta v1.1.9

\+ Updated commands to respond to missing permissions needed for the command to function. NoodleBot no longer needs an admin role to function. Minimum of manage messages and move members is needed for commands to work. A warning will be sent in case these permissions are not present

## Beta v1.1.8

\+ Additional stability patch for the permissions system

## Beta v1.1.7

\- Removed the r34 command from the command registry indefinitely. It is now effectively disabled

## Beta v1.1.6

\+ Added JDA-NAS library in an attempt to improve audio playback

\+ Fixed permissions manager not authorizing users properly

## Beta v1.1.5

\+ Completely rewrote gateway server. Everything is now event driven and different sessions can be registered each with their own functions

\+ Added the guest session to the gateway server. This session can be used to retrieve bot statistics and shard info.

\+ Minor stability improvements and bug fixes

## Beta v1.1.4

\+ Updated help command to operate via a help snippet request system so that a help list can be compiled from all active commands

\+ Permission system being modified to allow for more efficient permission info requests. System is also easier to integrate into future features

## Beta v1.1.3

\* Modified bot to use sharding when needed (important for later release to public)

\+ vckick command now accepts mentionable roles for batch disconnecting members from voice chats

## Beta v1.1.2

\* Stability patch for reactive message performance

## Beta v1.1.1

\* Minor stability patch for the new reactive buttons that have been added to the music player

## Beta v1.1.0

\+ Major feature addition: reactive messages

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\* Reactive messages are normal messages that have event driven reaction emojis

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\* This means that emoji reactions can be used as buttons, making certain commands easier to use

\+ Added reactive buttons to the music player. Commands such as stop, skip, fast forward, rewind, etc have all been made into easy to use buttons for those that are too lazy to type

## Beta v1.0.2

\* Fixed a potentially serious issue where default permissions for commands would not be set automatically when the bot would be added to a new server

## Beta v1.0.1

\* Minor patch changing the status message from the info command

## Beta v1.0.0

\* Message delete commands that were broken after changing from D4J to JDA were finally fixed

\* vckick command has been updated to take advantage of discord's new disconnect feature in voice chats, making the command faster

\- Removed the "clear message history" command since it was redundant and the same task could be achieved by typing "delete messages from @everyone"

## Alpha v0.9.5 - v0.9.9

\* Nothing major of note. Performance improvements mostly

\+ Added vckick command

\+ Added r34 command (because every bot needs nsfw stuff and I was bored)

## Alpha v0.9.4

! Major change from using D4J as the primary java discord library to JDA due to changes that were made in the newest version of D4J

\* Made a hotfix to all incompatable classes that were affected by the library change

## Alpha v0.6 - v0.9

\~ Primary development period

\! Moved away from discord-py and python in general. (Never very good at nor liked python. Code was messy, library management was messy, 
audio library wasn't great, etc)

\* noodlebot now completely rewritten in java using Discord4J aka D4J

\+ Added a system for saving guild settings server side

\+ Added a custom permissions system for commands so command access could be completely customizable

\+ Modified help command to only display commands a member has access to

\+ Added a websocket info gateway to use for access to noodlebot from a browser or gui client (Still heavily in development)

\+ Made wolfram alpha question command much more informative thanks to the much more powerfull java library for the WolframAlpha API

\+ Added a command to search for articles from Wikipedia

\+ Audio player can now retrieve audio from multiple sources besides just youtube

\+ Audio player can now have output volume adjusted so people don't always need to individually adjust the bot's output volume

\+ Added a setting for the default volume noodlebot should set its audio output to when joining a new call

\+ Added many other commands for controlling the audio player

\+ Added two commands for deleting messages in bulk. Can be usefull for cleaning up spam

\+ Added commands for getting a special password to log in to a future gui manager

\+ Added a command to get images from InspiroBot. For fun

\+ Added a command for listing permission ids since there are a lot

\+ Added commands to list and set settings for the guild noodlebot is present in

\+ Made noodlebot clean up after itself when necessary. Focused on this since I hate bots that create a lot of spam

\- noodlebot no longer open source. Github repository moved to a private server

## Pre-Alpha v0.1 - v0.5

\* The noodlebot project had been started as a privately hosted bot with open source code. During these initial versions, the whole bot was written in python and ran on a raspberry pi
