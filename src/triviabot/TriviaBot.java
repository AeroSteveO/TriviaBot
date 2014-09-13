/**
* Copyright (C) 2013 Joe Rawson
* Java port and included upgrades by Stephen
* -> Additional functionality and robustness being added in including:
*    - Rejoin server on network failure
*    - Rejoin channel on kick
*    - Fixing issue with ?standings command
*    - Ability to easily change the prefix to commands
*    - TRIVIA WARS
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
* Need to load the scores, if they exist, and connect to irc, displaying
* a welcome message.
*
* Scores should be kept in a class which will hold a nick -> score dict
* object, and at the end of every question will dump the dict to json
* where it can be loaded from. This might get weird if people start using
* weird nicks, but we'll cross that road when we get to it.
*
* irc connection should be a class, and we should use twisted. We don't
* really care if people come and go, since everyone in the channel is
* playing. Should handle this like karma. Watch all traffic, and if
* someone blurts out a string that matches the answer, they get the points.
* If they haven't scored before, add them to the scoreboard and give them
* their points, else, add their points to their total. Then dump the json.
*
* This bot requires there to be a ../questions/ directory with text files
* in it. These files are named after there genres, so "80s Films.txt"
* and the like. While the bot is running, it will randomly choose a
* file from this directory, open it, randomly choose a line, which is
* a question*answer pair, then load that into a structure to be asked.
*
* Once the question is loaded, the bot will ask the IRC channel the
* question, wait a period of time, show a character, then ask the question
* again.
*
* The bot should respond to /msgs, so that users can check their scores,
* and admins can give admin commands, like die, show all scores, edit
* player scores, etc. Commands should be easy to implement.
*
* Every so often between questions the bot should list the top ranked
* players, wait some, then continue.
*/

package triviabot;

import java.util.Random;
import org.pircbotx.Colors;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.Configuration.*;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.managers.BackgroundListenerManager;

/**
 *
 * @author Steve-O
 */
public class TriviaBot extends ListenerAdapter {
    
    @Override
    public void onMessage(final MessageEvent event) throws Exception {
        String message = Colors.removeFormattingAndColors(event.getMessage().trim());
        if (message.startsWith(Global.commandPrefix)){
            if(message.split(Global.commandPrefix)[1].equalsIgnoreCase("source")){
                event.getBot().sendIRC().notice(event.getUser().getNick(), "My current source can be found at: https://github.com/AeroSteveO/TriviaBot Which is based on: https://github.com/rawsonj/triviabot");
            }
            if(message.split(Global.commandPrefix)[1].equalsIgnoreCase("help")){
                event.getBot().sendIRC().notice(event.getUser().getNick(), "Hi, I'm "+Global.botOwner+"'s trivia bot");
                event.getBot().sendIRC().notice(event.getUser().getNick(), "Commands: score, standings, giveclue, help, next, skip, source");
                if(Global.botAdmins.contains(event.getUser().getNick())){
                    event.getBot().sendIRC().notice(event.getUser().getNick(), "Admin Commands: die, set <user> <score>, start, stop, save");
                }
            }
            if(message.split(Global.commandPrefix)[1].toLowerCase().startsWith("test")){
                String answer = message.split(" ",2)[1];
                Answer test = new Answer(answer);
                event.respond(test.getClue()+" for "+test.reveal());
                event.respond(test.giveClue());
                event.respond(test.giveClue());
                event.respond(test.giveClue());
                event.respond(test.giveClue());
                event.respond(test.giveClue());
            }
            if (message.equalsIgnoreCase(Global.commandPrefix+"die")||message.equalsIgnoreCase(Global.mainNick+", shutdown")) {
                if (event.getUser().getNick().equals("Steve-O")){
                    Global.reconnect = false;
                    event.getBot().sendIRC().message(event.getChannel().getName(), "I still make Trebek look bad");
                    Random generator = new Random();
                    int i = generator.nextInt(2);
                    if (i == 0) {
                        event.getBot().sendIRC().quitServer("Found by WATSON");
                    } else {
                        event.getBot().sendIRC().quitServer("Pesky humans still can't answer my questions");
                    }
                    System.exit(0);
                }
                else
                    event.getChannel().send().kick(event.getUser(), "PART 5! BOOBY TRAP THE STALEMATE BUTTON!"); // kick people for trying to kill the bot
            }
        }
    }
    @Override
    // Rejoin on Kick
    public void onKick(KickEvent event) throws Exception {
        if (event.getRecipient().getNick().equals(event.getBot().getNick())) {
            event.getBot().sendIRC().joinChannel(event.getChannel().getName());
        }
    }
    @Override
    // Set mode +B for Bots
    public void onConnect(ConnectEvent event) throws Exception {
        event.getBot().sendRaw().rawLine("mode " + event.getBot().getNick() + " +B");
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Setup this bot
        BackgroundListenerManager BackgroundListener = new BackgroundListenerManager();
        
        Configuration.Builder configuration = new Configuration.Builder()
                .setName("TriviaBot") //Set the nick of the bot. CHANGE IN YOUR CODE
                .setLogin("LQ") //login part of hostmask, eg name:login@host
                .setAutoNickChange(true) //Automatically change nick when the current one is in use
                .setCapEnabled(true) //Enable CAP features
                .addAutoJoinChannel("#rapterverse")
                .setAutoReconnect(true)
                .setMaxLineLength(425)
//                .setListenerManager(BackgroundListener)//Allow for logger background listener
                .addListener(new TriviaBot())
                .setServerHostname("irc.stevensnet.info"); //Join the official #pircbotx channel
        //.buildConfiguration();
//        BackgroundListener.addListener(new Logger(),true); //Add logger background listener
        Configuration config = configuration.buildConfiguration();
        //bot.connect throws various exceptions for failures
        try {
            Global.bot = new PircBotX(config);
            Runner parallel = new Runner(Global.bot);
            Thread t = new Thread(parallel);
            parallel.giveT(t);
            t.start();
            //PircBotX bot = new PircBotX(configuration);
            //Global.bot.startBot();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.printf("Failed to start bot\n");
        }
    }
}