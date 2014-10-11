 /**
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */

package TriviaBot;

import Objects.Answer;
import Objects.Question;
import Objects.Score;
import Objects.Score.ScoreArray;
import Objects.Vote;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import Objects.Vote.VoteLog;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Iterator;
import org.pircbotx.User;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.UserListEvent;

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
    
    ScoreArray scores = new ScoreArray();
    String filename = "scores.json";
    boolean loaded = startScores();
    ArrayList<Integer> levels = scoreLevels();
    
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
            else if (command.equalsIgnoreCase("score")){
                event.respond("Your current score is: "+scores.getScore(event.getUser().getNick()));
            }
            else if (command.matches("score\\s[a-z\\|]+")){
                String user = command.split(" ")[1];
                event.respond(user+"'s current score is: "+scores.getScore(user));
            }
            else if (command.equalsIgnoreCase("save")&&Global.botAdmins.contains(event.getUser().getNick())){
                scores.saveToJSON();
            }
        }
        
        if ((runTrivia||startVotes.start(event.getChannel().getName()))&&!Global.activeGames.isGameActive(event.getChannel().getName())){
            runTrivia = false;
            startVotes.clear();
            int cluesGiven = 0;
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
                        cluesGiven = 0;
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
                        if (levels.size()<cluesGiven){
                            scores.addScore(currentEvent.getUser().getNick(), levels.get(cluesGiven));
                            event.getBot().sendIRC().message(triviaChan,levels.get(cluesGiven)+" points have been added to your score");
                        }
                        
                        cluesGiven = 0;
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
                if (currentChan.equalsIgnoreCase(triviaChan)&&currentEvent.getUser().getNick().equalsIgnoreCase(event.getBot().getNick())){ //if(currentEvent.getMessage().equalsIgnoreCase(Integer.toString(key)))
                    questionsTillEnd--;
                    cluesGiven++;
                }
            }
        }
    }
    private ArrayList<Integer> scoreLevels(){
        ArrayList<Integer> levels = new ArrayList<>();
        levels.add(5);
        levels.add(3);
        levels.add(2);
        levels.add(1);
        return levels;
    }
    
    
    private boolean startScores(){
        boolean loaded;
        try{
            scores.setFilename(filename);
            loaded = scores.loadFromJSON();
        }
        catch (Exception ex){
            System.out.println("SCORES FAILED TO LOAD");
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    @Override
    public void onJoin(JoinEvent event){
        if (!scores.containsUser(event.getUser().getNick())){
            scores.add(new Score(event.getUser().getNick()));
            scores.saveToJSON();
        }
    }
    @Override
    public void onUserList(UserListEvent event){
        ImmutableSortedSet users = event.getUsers();
        
        Iterator<User> iterator = users.iterator();
        boolean modified = false;
        while(iterator.hasNext()) {
            User element = iterator.next();
            if (!scores.containsUser(element.getNick())){
                //temp = (User)users.floor(temp);
                scores.add(new Score(element.getNick()));
                System.out.println(element.getNick());
                modified = true;
            }
        }
        if (modified)
            scores.saveToJSON();
    }
}