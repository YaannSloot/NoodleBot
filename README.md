# ThiccBot4Java

## Info
𝙏𝙃𝙄𝘾𝘾 𝘽𝙊𝙏 is a bot that I started working on in my free time. It was first created in python but due to the fact that python is a slow memory hogging language by nature, I decided that an upgrade was needed. This version of my bot is designed to run on minimal hardware power, i.e. a raspberry pi.

## How to run the bot
To run the bot, open the jarfile located inside this repository. A .sh or .bat file will be created automatically, and you can then open that file and follow the instructions printed in the command shell. The rest is pretty self-explanatory

Requirements:
* A Discord bot to run the program on
* A WolframAlphaAPI AppID
* Java JRE or JDK

Minimum hardware requirements:
* An internet connection
* 1 GB of ram
* A computer that actually runs java
* A computer that can run the jar without crashing (If this happens your JRE might be too out of date)

Recommended hardware requirements:
* 4 GB or more of ram
* Quad core processor
* Java 8 or greater

Remember that the hardware load has a potential to increase with each new guild that the bot is added to. You should try running this bot on a dedicated machine if possible, with an ethernet connection to your router to minimize latency

## Features

### Music player 
This bot allows you to play music from multiple sources such as: 
* YouTube 
* SoundCloud 
* Vimeo 
* Twitch 
* BandCamp  
#### Command list:
`thicc play <search term|video url>` - Plays music from a source url or video/soundcloud search result  
`thicc volume <0-150>` - Adjusts the volume of the player  
`thicc show queue` - If AutoPlay is enabled or a playlist url is loaded, this shows the queued tracks
#### Settings related to the audio player:
`thicc set volume <0-150>` - Sets the default volume for the player when it enters a voice chat  
`thicc set autoplay <on|off>` - Sets AutoPlay on or off

## Important links for those wanting to modify source code

Discord4J Documentation:

[ReadTheDocs](https://discord4j.readthedocs.io/en/latest/) | [Javadoc](https://jitpack.io/com/github/austinv11/Discord4J/2.9.3/javadoc/)

WolframAlpha Java API Documentation:

[Javadoc](https://products.wolframalpha.com/api/libraries/java/)

## Change log
```
* v0.6alpha  
  –  Bot works. Basic functionality i.e. receiving/sending messages has been fully implemented  
* v0.7alpha  
  –  Lavaplayer finally implemented after fixing a dependency issue.  
  –  Converted project to maven project  
  –  YouTube music playing has been implemented  
  –  Added log4j logging via slf4j  
 * v0.8alpha  
  –  Changed how the bot retrieves required information for launch  
  –  Implemented bot launch settings being retrieved from a launch file. Currently the things stored are:  
    –  The bots token  
    –  The Wolfram Alpha API App ID  
    –  The local machines public ip(optional)  
  –  Added the bot setup wizard. Runs on first time startup or if the save file was deleted  
  –  Bot now generates a .sh or .bat run file automatically after it is executed for the first time  
  –  Guilds now have individual save files and settings files saved within their own settings directory  
  –  Added the default volume setting to the guild settings list  
 * v0.9alpha
  – IRL: NEW SERVER YAY
  – Added AutoPlay feature and settings to enable it
  – Added song queue
  – Added song skip command
  – Added full support for all LavaPlayer sources
  – Added commands to delete messages from channels
  – Added an inspire me command. Inspirational pictures from InspiroBot!
```
## TODO
In no particular order
* Integrate google and wikipedia into command functionality
* Auto-Mod support. Moderate your server for you

Server-related
* ~Proper logging~(done)
* ~Proper exception handling~(Not a problem at this moment)

General
* Fix any possible bugs (Top priority)

## Warnings
If you are forking, creating a pull request, or committing any sort of information or code to github, DO NOT leave a discord bot token in your commits. Your bot will be hijacked. Trust me, it has happened before. Your bot may last a week at most.
