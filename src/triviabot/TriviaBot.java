/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
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
                event.respond(test.getCurrentClue()+" for "+test.reveal());
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