/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package TriviaBot;

import Objects.TimedWaitForQueue;
import Objects.Question;
import Objects.Answer;
import static TriviaBot.TriviaMain.gameList;
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
 *          they get a 1 minute ban and kicked from the chan, if the user answers
 *          the question before the caller does, the caller gets a 1 min ban
 *          and kicked
 *
 */
public class TriviaKick extends ListenerAdapter{
    int time = 60;               // Seconds between the start of the trivia challenge and failure
    int timeBetweenUpdates = 15; // Seconds between each clue update
    @Override
    public void onMessage(MessageEvent event) throws InterruptedException, Exception {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        String gameChan = event.getChannel().getName();
        
        if (message.startsWith(Global.commandPrefix)){
            
            String command = message.split(Global.commandPrefix)[1];
            String[] cmdSplit = command.split(" ");
            if (cmdSplit[0].equalsIgnoreCase("kick")&&cmdSplit.length==2){
                
                String kicker = event.getUser().getNick();
                String kickee = cmdSplit[1];
                
                if(event.getBot().getUserChannelDao().getAllUsers().contains(event.getBot().getUserChannelDao().getUser(kickee))&&!gameList.contains(new String[] {gameChan, "kick", "long"})) {
                    gameList.add(gameChan, "kick", "long");
                    String triviaChan = event.getChannel().getName();
                    Question kickQuestion = new Question();
                    Answer kickAnswer = new Answer(kickQuestion.getAnswer());
                    
                    event.getBot().sendIRC().message(event.getChannel().getName(),"Question:");
                    event.getBot().sendIRC().message(event.getChannel().getName(), kickQuestion.getQuestion());
                    
                    int key=(int) (Math.random()*100000+1);
                    int updateKey = (int) (Math.random()*100000+1);
                    
                    TimedWaitForQueue timedQueue = new TimedWaitForQueue(event,time,key);
                    kickQuestion.startQuestionUpdates(event, timeBetweenUpdates, key, updateKey);
                    event.getBot().sendIRC().message(event.getChannel().getName(),"Clue: "+kickAnswer.getClue());
                    boolean running = true;
                    
                    while (running){
                        MessageEvent CurrentEvent = timedQueue.waitFor(MessageEvent.class);
                        
                        String currentChan = CurrentEvent.getChannel().getName();
                        String currentMessage = Colors.removeFormattingAndColors(CurrentEvent.getMessage());
                        
                        if (currentMessage.equalsIgnoreCase(Integer.toString(key))&&currentChan.equalsIgnoreCase(triviaChan)){
                            
                            event.getBot().sendIRC().message(triviaChan,"Times Up! You've failed in your attempt to kick "+kickee+". ");
                            event.getBot().sendIRC().message(triviaChan,"The answer was: "+Colors.BOLD+Colors.RED+kickQuestion.getAnswer());
                            event.getBot().sendRaw().rawLine("tban " + event.getChannel().getName() + " 1m " + event.getUser().getNick() + "!*@*");
                            event.getChannel().send().kick(event.getBot().getUserChannelDao().getUser(event.getUser().getNick()), "You are the weakest link, goodbye");
                            running = false;
                            kickQuestion.endQuestionUpdates();
                            timedQueue.end();
                        }
                        
                        else if (currentMessage.equalsIgnoreCase(Integer.toString(updateKey))){
                            
                            event.getBot().sendIRC().message(currentChan,"Clue: "+kickAnswer.giveClue());
                        }
                        
                        else if (currentMessage.equalsIgnoreCase(kickQuestion.getAnswer())&&CurrentEvent.getUser().getNick().equalsIgnoreCase(kickee)&&currentChan.equalsIgnoreCase(triviaChan)){
                            
                            event.getBot().sendIRC().message(triviaChan,kickee.toUpperCase()+", YOU HAVE DEFEATED "+kicker.toUpperCase()+" AT HIS OWN GAME");
                            event.getBot().sendRaw().rawLine("tban " + event.getChannel().getName() + " 1m " + event.getUser().getNick() + "!*@*");
                            event.getChannel().send().kick(event.getBot().getUserChannelDao().getUser(kicker), "You are the weakest link, goodbye");
                            running = false;
                            kickQuestion.endQuestionUpdates();
                            timedQueue.end();
                        }
                        
                        else if (currentMessage.equalsIgnoreCase(kickQuestion.getAnswer())&&CurrentEvent.getUser().getNick().equalsIgnoreCase(kicker)&&currentChan.equalsIgnoreCase(triviaChan)){
                            
                            event.getBot().sendIRC().message(triviaChan,kicker.toUpperCase()+", YOU HAVE SUCCEEDED!");
                            event.getChannel().send().kick(event.getBot().getUserChannelDao().getUser(kickee), "You are the weakest link, goodbye");
                            running = false;
                            kickQuestion.endQuestionUpdates();
                            timedQueue.end();
                        }
                    }
                    gameList.remove(gameChan, "kick");
                }
                else if (gameList.contains(new String[] {gameChan, "kick", "long"})){
                    event.getBot().sendIRC().notice(event.getUser().getNick(), Colors.BOLD+"KICK: "+Colors.NORMAL+"Only one trivia game may be played at a time");
                }
                else {
                    event.getBot().sendIRC().notice(event.getUser().getNick(), Colors.BOLD+"KICK: "+Colors.NORMAL+"user not in channel");
                }
            }
            else if(command.equalsIgnoreCase("kick")){
                event.getChannel().send().kick(event.getUser(),"Okay, I can do that");
            }
        }
    }
}