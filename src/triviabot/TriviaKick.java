/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package triviabot;

import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Steve-O
 * 
 * Activate Command with:
 *      !kick
 *          Kicks the user who sent the command
 *      !kick [user]
 *          Starts a trivia game in which the caller can answer a trivia 
 *          question to get the user kicked, if the caller of the function fails,
 *          they get a 1 minute ban and kicked from the chan
 * 
 */
public class TriviaKick extends ListenerAdapter{
    int time = 60;  // Seconds
    int timeBetweenUpdates = 15; // Seconds between each clue update
    @Override
    public void onMessage(MessageEvent event) throws InterruptedException, Exception {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        if (message.startsWith(Global.commandPrefix)){
            String command = message.split(Global.commandPrefix)[1].toLowerCase();
            if (command.toLowerCase().startsWith("kick ")){
                String kicker = event.getUser().getNick();
                String kickee = command.split(" ")[1];
                if(event.getBot().getUserChannelDao().getAllUsers().contains(event.getBot().getUserChannelDao().getUser(kickee))) {
                    String triviaChan = event.getChannel().getName();
                    Question kickQuestion = new Question();
                    Answer kickAnswer = new Answer(kickQuestion.getAnswer());
                    
                    
                    int key=(int) (Math.random()*100000+1);
                    TimedWaitForQueue timedQueue = new TimedWaitForQueue(event,time,key);
                    kickQuestion.startQuestionUpdates(event, kickAnswer, kickQuestion, timeBetweenUpdates);
                    boolean running = true;
                    while (running){
                        MessageEvent CurrentEvent = timedQueue.waitFor(MessageEvent.class);
                        String currentChan = CurrentEvent.getChannel().getName();
                        if (CurrentEvent.getMessage().equalsIgnoreCase(Integer.toString(key))){
                            event.getBot().sendIRC().message(triviaChan,"Times Up! You've failed in your attempt to kick "+kickee+". ");
                            event.getBot().sendRaw().rawLine("tban " + event.getChannel().getName() + " 1m " + event.getUser().getNick() + "!*@*");
                            event.getChannel().send().kick(event.getBot().getUserChannelDao().getUser(event.getUser().getNick()), "You are the weakest link, goodbye");
                            running = false;
                            kickQuestion.endQuestionUpdates();
                            timedQueue.end();
                            
                        }
                        
                        
                    }
                    
                    
                   
                    
                    
                    
                    
                }
                else {
                    event.getBot().sendIRC().notice(event.getUser().getNick(), Colors.BOLD+"KICK: "+Colors.NORMAL+"user not in channel");
                    return;
                }
            }
            else if(command.equalsIgnoreCase("kick")){
                event.getChannel().send().kick(event.getUser(),"Okay, I can do that");
                return;
            }
        }
    }
}
