# Say hello to NoodleBot

But what is **NoodleBot** you ask?

**NoodleBot** is a pure server moderation and management tool. No spam, no economy BS, just automatic moderation and admin commands. Not to mention it cleans up after itself. You'll barely notice its there. It also has a few extra fun features and a music player if you're in to that.

### Quick links
- [Features](#Features)
- [Commands](#Commands)
- [Q&A](#Q&A)

Required permissions (this may change when new features are added):
* Manage messages - Needed so that the bot can clean up its own spam
* Move members - Needed for the vckick command to function

If additional permissions are required based on your guild's settings the bot will send you a missing permissions error detailing the required permission upon trying to use a command

### Side note: report issues here

## Features

Currently, **NoodleBot** has the ability to:
* Play music complete with volume controls using the powerful LavaPlayer library
* Batch delete messages by user(s) and/or role(s)
* Batch kick user(s) and/or role(s) from any connected voice chats
* Query WolframAlpha for math answers or other WolframAlpha related things
* Query Wikipedia for articles
* Access to all of these features can be adjusted via a custom per user/role permission system
* Custom per guild bot settings to adjust the way **NoodleBot** functions
* Some commands can be controlled via reactions on output messages. Read more about it at the [**Reactive Messages**](#Reactive) section

Many more features are on the way, notably:
* Complete server logging (output channel adjusted via settings)
* Adjustable bot prefix
* Schedulable chat cleanup to remove spam complete with adjustable message filters (also regex filters if you want to make a filter yourself)
* Anti-spam functionality with adjustable filters (regex here as well)
* Video live streaming so you can watch online videos with friends
* A custom WebSocket gateway which will be used to support features such as:
	* A complete web dashboard where you can adjust settings and permissions
	* An announcement framework where you can send requests to the gateway for **NoodleBot** to send an announcement message to your guild. This means if for example your guild is a support hub for a private game server you can send automated server status messages and the like through **NoodleBot**. More details on this located on the bot website.
	* A custom management client for Desktop/Mobile users. This will definitely take a while to complete, with a desktop client coming first, and a phone app later.
* More customization tools
* Since this bot is in open beta, a bug reporting tool as well as a patch notes announcer so you can keep up with the bot's development

**NoodleBot** is spam resistant. That means that it will try to clean up after itself as much as possible. You may have an issue with tracking who uses commands, so the logger will be added sooner rather than later

### Supported music player audio sources
Since **NoodleBot** uses LavaPlayer, you can load audio from the following sources:
* YouTube
* SoundCloud
* Bandcamp
* Vimeo
* Twitch
* HTTP URLs pointing to audio files
* Media files uploaded to discord as message attachments

YouTube and SoundCloud are searchable. YouTube searching is the default function. If you want to search from SoundCloud, put "scsearch:" before your search phrase. 

Ex: nood scsearch:a song

### Permissions system info
Commands are separated into 4 different categories:
* Player commands
* Server management commands
* Utility commands
* Misc commands

These categories as well as every command on the bot have a command id associated with them.
Access to these commands as well as entire command categories can be adjusted via permission settings for each of these ids. *ANY* of the guild permissions settings, including admin privileges, are completely ignored by this system. This is an independent system for adjusting command access, with the only discord-related factor being role hierarchy. Role hierarchy can affect the way command access is determined, and this is explained below.

A default set of permissions are also added as soon as the bot is added to a guild. All roles lacking admisistrator will have the bot's info and management commands disabled.

**Permission hierarchy**

If the command user is the guild owner, access is immediately given, otherise:
* Global settings are read first
* If the command the user has issued is in a category in which the user has denied global access, or the users highest role has denied global access, the user is denied usage to the command. *UNLESS:*
	* If the user has global access to the category but their highest role does not, they still have access
	* If the specific command issued has settings to allow the user or their highest role access, the global settings are overridden and they are allowed access
	* If the users highest role has access to the specific command, but the user has had their access to that specific command denied, their denied access overrides the roles access and they are denied command usage 
* Else if the command the user has issued is in a category in which the user has global access, or the users highest role has global access, the user is allowed usage to the command. *UNLESS:*
	* If the user does not have global access to the category but their highest role does, they still do not have access
	* If the specific command issued has settings to deny the user or their highest role access, the global settings are overridden and they are denied access
	* If the users highest role does not have access to the specific command, but the user does have access, their access overrides the roles lack of access and they are allowed command usage 

Users who are allowed to adjust permissions cannot adjust permissions for those higher or at the same position as them in the role hierarchy. In addition, the user must be an administrator of the guild to adjust permissions. Resetting permissions back to default can only be done by the guild owner.

A good way to see what commands you have access to is to issue the help command. This command is available to all users, and help entries for commands the user does not have access to will be excluded from the command output.

*nood show permission ids* will give you a list of command and category ids if you don't know them

### <a id="Reactive"></a>Reactive Messages
**NoodleBot** uses a custom event driven system for performing tasks when a reaction is added to certain discord messages. This way, output for certain commands can act as a sort of gui interface, complete with buttons and interactive menus. The music player currently is currently the only implementation of the ReactiveMessage library, and contains buttons that act as shortcuts to other player commands.


## <a id="Commands"></a>Commands
### General Commands
```
nood help - Lists available commands
```
### Player commands
```
nood jump <position> - Jumps to a specific position in the currently playing track specified by a timecode in HH:MM:SS.ss form 
nood leave - Makes the bot leave the chat 
nood pause - This command is a toggle. It will either pause or unpause the current track 
nood play <[scsearch:]Video name|Video URL> - Plays a video or song 
nood remove track <track number/range of track numbers> - Removes a track or range of tracks from the queue 
nood show queue - Lists the songs currently in the song queue 
nood skip - Skips the currently playing song 
nood stop - Stops the currently playing song and clears the queue 
nood volume <0-200> - Changes the player volume
```
### Server management commands
```
nood delete messages
	Parameters: 
	older than <number> <day(s)/week(s)/month(s)/year(S)> from <@user|@role> 
	Ex 1 - nood delete messages older than 1 week 3 days from @everyone 
	Ex 2 - nood delete messages from @someuser 
nood get gui login - Creates a guild password for the bot's gui manager 
nood list settings or nood settings - Lists all of the settings and their values 
nood get new gui login - Creates a new guild password for the bot's gui manager 
nood show permission ids - lists the command id/group id for all of the bot's available commands 
nood apply default permissions - Sets the recommended default permissions for your server 
nood permission <command id/command group> <allow/deny> <@user(s) and/or @role(s)> - sets a permission for a command/command catagory 
nood set <setting> <value> - Changes a server setting on the guild's settings file located on the bot server
nood vckick <@user(s)|@role(s)> - Kicks any mentioned users or roles from whatever voice channel they are connected to. The person using this command does not need to be connected to any voice channels.
```
### Utility commands
```
nood info - Gets general info about the bot and it's current version number 
nood <question> - Sends a question to WolframAlpha 
nood wiki - Looks up an article on Wikipedia
```
### Misc commands
```
nood inspire me - Shows an inspirational image from InspiroBot™
```
## <a id="Q&A"></a>Some Q&A
*How many people are working on this bot?* - Currently, just me. 

*Ok so who are you?* - My name is Ian Sloat. You can dm me at Yaannsloot#6326. Just try not to spam me

*How often do you work on this bot?* - That depends entirely on how busy I am with college. I work on this as much as possible in my free time but of course I will take breaks.

*Why did you start working on this bot?* - I originally made this as a joke bot. It did one thing and ran in one of my friends guilds. Eventually I got in to making an actual bot and kept adding to it.

*How stable is this bot?* - As stable as I can make it. If anything major keeps happening I'll try as hard as I can to get a patch out ASAP

*Do you have a website for this bot?* - Yes. It is currently in the works and everything is being done from scratch. Once it looks presentable I'll add a link to it here

*Where can I report bugs? Do you have a support discord server?* - Will I make a support discord server? Maybe. If you need to report bugs, report them here.

*WHY ARE SOME OF THE COMMANDS SO COMPLICATED!?* - I know they are complicated and I'm sorry. Command syntax will be adjusted in the near future, along with unix style alternative syntax so you can do more and type less. Hold on to your butts in the meantime.

