# TriviaBot
TriviaBot is a bot that has been developed by Steve-O as a Java replacement for the python TriviaBot by rawsonj. This is mostly because he wanted to add features and robustness to the bot, and didn’t want to start from scratch. This bot shares a lot of code with Wheatley, Steve-O’s other bot. It still acts a bit derpy due to multiple threads processing at different speeds.

## Trivia Functions

**[Public]**
* !Start
  * Adds a vote to start the trivia, 3 votes are needed within 10 min to start
* !Stop
  * Adds a vote to stop the trivia, 3 votes are needed within 10min to stop
* !Skip
  * Adds a vote to skip the current question, 3 votes are needed within the questions time to skip
* !Score
  * Responds with your score, if a game is currently running, it gives both your current score and your overall trivia score, otherwise it just gives your overall score
* !Score [user]
  * Responds with the users score, if a game is currently running, it gives both their current score and their overall trivia score, otherwise it just gives their overall score
* !Standings
  * Responds with a list of the current standings (up to 5 users) (either for the actively running game or the overall standings if no game is active). If a user has a score of zero, they aren't listed, if nobody has a score greater than zero, then it responds that nobody has a score greater than zero
* !Standings [user]
  * Responds with the current location in the standings the given user is
* !Standings [value]
  * Responds with that number of users in the standings
* !Report [current or previous]
  * Reports either the current or previous question as incorrect and notifies the bot admin

**[Bot Admin Only Commands]**
* !Start
  * Instantly starts the trivia
* !Stop
  * Instantly stops the trivia
* !Save
  * Saves everyone's score to JSON and removes duplicate entries if any were made
* !Skip
  * Skips the current question
!Score [user] [amount]
Sets the input users score to the input money value
!Combine [user a] [user b]
Merges the score of user a into user b, and resets user a's score to the base score used by the scoring array
!Update
Replaces the current production question files with the latest files from the git repo

## Trivia Game Functions
**[Public]**
* !Kick
  * Kicks the user who sent the command
* !Kick [user]
  * Starts a trivia game in which the caller can answer a trivia question to get the user kicked, if the caller of the function fails, they get a 1 minute ban and kicked from the chan, if the user answers the question before the caller does, the caller gets a 1 min ban and kicked

## Latent Functions 
*(stuff that runs automagically)*
* Automatically stops trivia after 5 questions with no activity in the channel
* Auto-joins channels when invited via chanserv

## Administrative Functions
**[Bot Owner Only Commands]**
* TriviaBot, please shutdown
* TriviaBot, shutdown
  * Turns off TriviaBot
* TriviaBot, join [#channel]
* TriviaBot, please join [#channel]
  * Commands TriviaBot to join the given channel
* TriviaBot, leave [#channel]
* TriviaBot, please leave [#channel]
  * Commands TriviaBot to leave the given channel, if no channel is given, it’ll leave the one the command is sent from
* TriviaBot, fix yourself
  * Ghosts main nick, changes to main nick, identifies to nickserv
* !SysInfo
  * Responds with the current number of threads being used, as well as free ram and used ram

**[Channel Owners and Bot Owner Only Commands]**
* TriviaBot, leave
* TriviaBot, please leave
  * Commands TriviaBot to leave the channel the command is sent from
