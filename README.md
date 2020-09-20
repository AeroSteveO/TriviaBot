TriviaBot
=========

Java port of Rawsonj's python TriviaBot

https://github.com/rawsonj/triviabot

Bot is built using PIRCBOTX 2.0.1

[Bot commands](Commands.md) are available with descriptions of what they do

Features
--------

Simple implementation. Questions are <string>`<string> formatted in plain text files.

Event driven implementation: uses very little cycles.

Implementation
--------------

Questions exist in files under $BOTDIR/questions.
Each round, a file is selected at random, then a line is selected at random from the file.

The answer is then masked and the question is asked. Periodically, the bot unmask a letter of the current question. This happens three times before the answer is revealed.

How do I get set up?
--------------
* Building can be done using ant or docker
  * docker build . -t triviabot
  * ant clean-build
* .jar dependencies are included in the repo to provide a baseline configuration

Docker Environment
------------------
All of the environment variables the bot loads from docker are put into the configuration files at first start. If the config files exist, then these will not be used, and the config file settings will be used instead.
* BOT_CONFIG_FOLDER
  * The path to use for all of the files needed by the bot
* BOT_NICK
  * The nickname to use on IRC, this is loaded into the configuration file and should be updated there after the first startup
* BOT_PASSWORD
  * The nickname to use on IRC, this is loaded into the configuration file and should be updated there after the first startup
* BOT_OWNER_NICK
  * The nickname to use on IRC, this is loaded into the configuration file and should be updated there after the first startup
* BOT_IRC_PORT
  * The IRC server port, this is loaded into the configuration file and should be updated there after the first startup
* BOT_LOGIN
  * The nickname to use on IRC, this is loaded into the configuration file and should be updated there after the first startup
* BOT_IRC_ADDRESS
  * The IRC server address, this is loaded into the configuration file and should be updated there after the first startup
* BOT_CHANNEL_LIST
  * A comma separated list of channels to join, this is loaded into the configuration file and should be updated there after the first startup

What the bot doesn't do.
------------------------

  * It doesn't have multiple answers to a question. It shows you the format. Part of the game is to match its formatting.

  * Have error-free questions: the questions come from other bot implementations which themselves had horrible typos.
There needs to be an army of editors to go through the 350+k lines and format them to the standard format for the bot.
The bot was written to catch malformed questions so it wouldn't crash, but if it technically matches <string>`<string>
there's no way for the bot to understand that's not part of the question.

  * Pull requests and feature requests are welcome
