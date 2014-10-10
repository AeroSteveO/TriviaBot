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
import org.pircbotx.hooks.WaitForQueue;

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
                startVotes.addVote(event.getUser().getNick(),event.getChannel().getName());
            }
            else if (command.equalsIgnoreCase("stop")){
                stopVotes.addVote(event.getUser().getNick(),event.getChannel().getName());
            }
            else if (command.equalsIgnoreCase("end")&&Global.botAdmins.contains(event.getUser().getNick())){
                runTrivia = false;
            }
        }
        
        if ((runTrivia||startVotes.start(event.getChannel().getName()))&&!Global.activeGames.isGameActive(event.getChannel().getName())){
            runTrivia = false;
            startVotes.clear();
            String triviaChan = event.getChannel().getName();
            Question triviaQuestion = new Question();
            Answer triviaAnswer = new Answer(triviaQuestion.getAnswer());
            
            event.getBot().sendIRC().message(event.getChannel().getName(),"Question:");
            event.getBot().sendIRC().message(event.getChannel().getName(), triviaQuestion.getQuestion());
            int key=(int) (Math.random()*100000+1);
            WaitForQueue queue = new WaitForQueue(event.getBot());
            triviaQuestion.startQuestionUpdates(event, triviaAnswer, triviaQuestion, timeBetweenUpdates, key);
            boolean running = true;
            int questionsTillEnd = numQuestionsAllowedTillEnd;
            while (running){
                MessageEvent currentEvent = queue.waitFor(MessageEvent.class);
                String currentMessage = Colors.removeFormattingAndColors(currentEvent.getMessage());
                String currentChan = currentEvent.getChannel().getName();
                if ((currentMessage.equalsIgnoreCase(Global.commandPrefix+"stop")&&currentChan.equalsIgnoreCase(triviaChan)&&Global.botAdmins.contains(currentEvent.getUser().getNick()))||stopVotes.start()){
                    event.getBot().sendIRC().message(triviaChan,"Thanks for playing trivia!");
                    running = false;
                    triviaQuestion.endQuestionUpdates();
                    queue.close();
                }
                else if (currentMessage.equalsIgnoreCase(Integer.toString(key))){
                    if(questionsTillEnd==0){
                        event.getBot().sendIRC().message(triviaChan,"No one got it. The answer was: "+Colors.BOLD+Colors.RED+triviaQuestion.getAnswer());
                        event.getBot().sendIRC().message(triviaChan,"Looks like nobody is around, Thanks for playing trivia! Come again soon!");
                        running = false;
                        triviaQuestion.endQuestionUpdates();
                        queue.close();
                    }
                    else{
                        event.getBot().sendIRC().message(triviaChan,"No one got it. The answer was: "+Colors.BOLD+Colors.RED+triviaQuestion.getAnswer());
                        
                        triviaQuestion.endQuestionUpdates();
                        triviaQuestion = new Question();
                        
                        triviaAnswer = new Answer(triviaQuestion.getAnswer());
                        event.getBot().sendIRC().message(triviaChan,"Next Question:");
                        event.getBot().sendIRC().message(triviaChan,triviaQuestion.getQuestion());
                        triviaQuestion.startQuestionUpdates(event, triviaAnswer, triviaQuestion, timeBetweenUpdates,key);
                    }
                }
                else if (currentChan.equalsIgnoreCase(triviaChan)&&!currentEvent.getUser().getNick().equalsIgnoreCase(event.getBot().getNick())){
                    if (currentMessage.equalsIgnoreCase(triviaQuestion.getAnswer())){
                        event.getBot().sendIRC().message(triviaChan,currentEvent.getUser().getNick()+" GOT IT!");
                        
                        triviaQuestion.endQuestionUpdates();
                        triviaQuestion = new Question();
                        
                        triviaAnswer = new Answer(triviaQuestion.getAnswer());
                        event.getBot().sendIRC().message(triviaChan,"Next Question:");
                        event.getBot().sendIRC().message(triviaChan,triviaQuestion.getQuestion());
                        triviaQuestion.startQuestionUpdates(event, triviaAnswer, triviaQuestion, timeBetweenUpdates,key);
                    }
                    questionsTillEnd = numQuestionsAllowedTillEnd;
                    System.out.println("questions till end reset");
                }
                if (currentChan.equalsIgnoreCase(triviaChan)&&currentEvent.getUser().getNick().equalsIgnoreCase(event.getBot().getNick())) //if(currentEvent.getMessage().equalsIgnoreCase(Integer.toString(key)))
                    questionsTillEnd--;
            }
        }
    }
}