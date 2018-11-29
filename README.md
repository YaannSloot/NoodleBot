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
* 4GB or more of ram
* Quad core processor
* Java 8 or greater

Remember that the hardware load has a potential to increase with each new guild that the bot is added to.

## Important links for contributors

Discord4J Documentation:

[ReadTheDocs](https://discord4j.readthedocs.io/en/latest/) | [Javadoc](https://jitpack.io/com/github/austinv11/Discord4J/2.9.3/javadoc/)

WolframAlpha Java API Documentation:

[Javadoc](https://products.wolframalpha.com/api/libraries/java/)

## TODO
In no particular order
* Get the bot playing music complete with volume control (Youtube currently works, adding support for other links)
* Integrate google and wikipedia into command functionality
* Games? Maybe? IDK

Server-related
* ~Proper logging~(done)
* Proper exception handling

General
* Fix any possible bugs (Top priority)

## Warnings
If you are forking, creating a pull request, or commiting any sort of information or code to github, DO NOT leave a discord bot token in your commits. Your bot will be hijacked. Trust me, it has happened before. Your bot may last a week at most.
