 /**
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */

package TriviaBot;

import Objects.Answer;
import Objects.Question;
import Objects.TimedWaitForQueue;
import Objects.Vote;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import Objects.Vote.VoteLog;

/**
 *
 * @author Steve-O
 *
 *
 */
public class TriviaMain extends ListenerAdapter{
    boolean runTrivia = false;                              // Should Trivia Be Running
    int numQuestionsAllowedTillEnd = 5;                     // Number of questions allowed to go without response till trivia ends
    int questionsTillAutoEnd = numQuestionsAllowedTillEnd;  // Number of questions left till trivia ends
    int timeBetweenUpdates = 5;                            // Seconds between each clue update    //30
    int time = 20;               // Seconds between the start of the trivia challenge and failure //160
//    int votesTillStart = 3;       // Number of startVotes needed to start trivia
    //int key=(int) (Math.random()*100000+1);
    VoteLog startVotes = new Vote.VoteLog();                     // Log of current Votes for starting trivia
    VoteLog stopVotes  = new Vote.VoteLog();                     // Log of current Votes for stopping trivia
    
    @Override
    public void onMessage(MessageEvent event) throws InterruptedException, Exception {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        if (message.startsWith(Global.commandPrefix)){
            String command = message.split(Global.commandPrefix)[1].toLowerCase();
            if (command.equalsIgnoreCase("start")&&Global.botAdmins.contains(event.getUser().getNick())){
                runTrivia = true;
            }
            else if (command.equalsIgnoreCase("start")){
                startVotes.addVote(event.getUser().getNick());
            }
            else if (command.equalsIgnoreCase("stop")){
                stopVotes.addVote(event.getUser().getNick());
            }
            else if (command.equalsIgnoreCase("end")&&Global.botAdmins.contains(event.getUser().getNick())){
                runTrivia = false;
            }
            
        }
        
        
//        runTrivia = startVotes.start();
//        if (!Global.activeGames.isGameActive(event.getChannel().getName())){
//
//        }
        if ((runTrivia||startVotes.start())&&!Global.activeGames.isGameActive(event.getChannel().getName())){
            runTrivia = false;
            startVotes.clear();
            String triviaChan = event.getChannel().getName();
            Question kickQuestion = new Question();
            Answer kickAnswer = new Answer(kickQuestion.getAnswer());
            
            event.getBot().sendIRC().message(event.getChannel().getName(),"Question:");
            event.getBot().sendIRC().message(event.getChannel().getName(), kickQuestion.getQuestion());
            int key=(int) (Math.random()*100000+1);
            TimedWaitForQueue timedQueue = new TimedWaitForQueue(event,time,key);
            kickQuestion.startQuestionUpdates(event, kickAnswer, kickQuestion, timeBetweenUpdates);
            boolean running = true;
            while (running){
                MessageEvent CurrentEvent = timedQueue.waitFor(MessageEvent.class);
                String currentChan = CurrentEvent.getChannel().getName();
                if (CurrentEvent.getMessage().equalsIgnoreCase(Integer.toString(key))){
                    event.getBot().sendIRC().message(triviaChan,"No one got it. The answer was: "+Colors.BOLD+Colors.RED+kickQuestion.getAnswer());
                    
                    
//                    running = false;
                    kickQuestion.endQuestionUpdates();
//                    timedQueue.end();
                    kickQuestion.getNewQuestion();
                    kickAnswer.setAnswer(kickQuestion.getAnswer());
                    event.getBot().sendIRC().message(triviaChan,"Next Question:");
                    event.getBot().sendIRC().message(triviaChan,kickQuestion.getQuestion());
                    kickQuestion.startQuestionUpdates(event, kickAnswer, kickQuestion, timeBetweenUpdates);
                }
                else if (CurrentEvent.getMessage().equalsIgnoreCase(kickQuestion.getAnswer())){
                    
                    event.getBot().sendIRC().message(triviaChan,CurrentEvent.getUser().getNick()+" GOT IT!");
                    
//                    running = false;
                    kickQuestion.endQuestionUpdates();
//                    timedQueue.end();
                    kickQuestion.getNewQuestion();
                    kickAnswer.setAnswer(kickQuestion.getAnswer());
                    event.getBot().sendIRC().message(triviaChan,"Next Question:");
                    event.getBot().sendIRC().message(triviaChan,kickQuestion.getQuestion());
                    kickQuestion.startQuestionUpdates(event, kickAnswer, kickQuestion, timeBetweenUpdates);
                }
                else if (CurrentEvent.getMessage().equalsIgnoreCase(Global.commandPrefix+"stop")){
                    running = false;
                    kickQuestion.endQuestionUpdates();
                    timedQueue.end();
                }
            }
        }
    }
}
