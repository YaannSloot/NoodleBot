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

## Important links for those wanting to modify source code

Discord4J Documentation:

[ReadTheDocs](https://discord4j.readthedocs.io/en/latest/) | [Javadoc](https://jitpack.io/com/github/austinv11/Discord4J/2.9.3/javadoc/)

WolframAlpha Java API Documentation:

[Javadoc](https://products.wolframalpha.com/api/libraries/java/)

## Change log
* v0.6alpha  
  – Bot works. Basic functionality i.e. receiving/sending messages has been fully implemented  
* v0.7alpha  
  – Lavaplayer finally implemented after fixing a dependency issue.  
  – Converted project to maven project  
  – YouTube music playing has been implemented  
  – Added log4j logging via slf4j  
 * v0.8alpha  
  – Changed how the bot retrieves required information for launch  
  – Implemented bot launch settings being retrieved from a launch file. Currently the things stored are:  
    – The bots token  
    – The Wolfram Alpha API App ID  
    – The local machines public ip(optional)  
  – Added the bot setup wizard. Runs on first time startup or if the save file was deleted  
  – Made it so that the bot generates a .sh or .bat run file automatically after it is executed for the first time  
  – Added Guild settings. Guilds now have individual save files and settings files saved within their own settings directory  
  – Added the default volume setting to the guild settings list  

## TODO
In no particular order
* Get the bot playing music complete with volume control (Youtube currently works, adding support for other links)
* Integrate google and wikipedia into command functionality
* Games? Maybe? IDK

Server-related
* ~Proper logging~(done)
* ~Proper exception handling~(Not a problem at this moment)

General
* Fix any possible bugs (Top priority)

## Warnings
If you are forking, creating a pull request, or committing any sort of information or code to github, DO NOT leave a discord bot token in your commits. Your bot will be hijacked. Trust me, it has happened before. Your bot may last a week at most.
