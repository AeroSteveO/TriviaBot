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
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import Objects.Vote.VoteLog;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.pircbotx.User;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.UserListEvent;

/**
 *
 * @author Steve-O
 * ADMIN COMMANDS
 * Activate Command with:
 *      !start
 *          Instantly starts the trivia
 *      !stop
 *          Instantly stops the trivia
 *      !save
 *          Saves everyones score to JSON and removes duplicate entries if any 
 *          were made
 *      !merge [user a] [user b]
 *          Merges the score of user a into user b, and resets user a's score to
 *          the base score used by the scoring array
 *
 * USER COMMANDS
 * Activate Command with:
 *      !start
 *          Adds a vote to start the trivia, 3 votes are needed within 10 min to start
 *      !stop
 *          Adds a vote to stop the trivia, 3 votes are needed within 10min to stop
 *      !score
 *          Responds with your score, if a game is currently running, it gives both
 *          your current score and your overall trivia score, otherwise it just gives
 *          your overall score
 *      !score [user]
 *          Responds with the users score, if a game is currently running, it gives both
 *          their current score and their overall trivia score, otherwise it just gives
 *          their overall score
 *      !standings
 *          Responds with a list of the current standings (either for the
 *          actively running game or the overall standings if no game is active)
 *          If a user has a score of zero, they aren't listed, if nobody has a score
 *          greater than zero, then it responds that nobody has a score greater than zero
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
    VoteLog startVotes = new VoteLog();                     // Log of current Votes for starting trivia
    VoteLog stopVotes  = new VoteLog();                     // Log of current Votes for stopping trivia
    
    ScoreArray scores = new ScoreArray();
    String filename = "scores.json";
    boolean loaded = startScores();
    ArrayList<Integer> levels = scoreLevels();
    
    @Override
    public void onMessage(MessageEvent event) throws InterruptedException, Exception {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        if (message.startsWith(Global.commandPrefix)){
            String command = message.split(Global.commandPrefix)[1].toLowerCase();
            // Admin start
            if (command.equalsIgnoreCase("start")&&Global.botAdmins.contains(event.getUser().getNick())){
                runTrivia = true;
            }
            // User start
            else if (command.equalsIgnoreCase("start")){
                startVotes.addVote(event.getUser().getNick(),event.getChannel().getName());
            }
            // User stop
            else if (command.equalsIgnoreCase("stop")){
                stopVotes.addVote(event.getUser().getNick(),event.getChannel().getName());
            }
            // Admin stop
            else if (command.equalsIgnoreCase("stop")&&Global.botAdmins.contains(event.getUser().getNick())){
                runTrivia = false;
            }
            
            else if (command.toLowerCase().startsWith("merge")&&command.split(" ").length==3&&Global.botAdmins.contains(event.getUser().getNick())) {
                String mergeThis = command.split(" ")[1];
                String mergeIntoThis = command.split(" ")[2];
                
                if (scores.getScore(mergeThis)<0){
                    event.getBot().sendIRC().notice(event.getUser().getNick(), mergeThis+": USER NOT FOUND");
                }
                else if (scores.getScore(mergeIntoThis)<0){
                    event.getBot().sendIRC().notice(event.getUser().getNick(), mergeIntoThis+": USER NOT FOUND");
                }
                
                else{
                    scores.merge(mergeThis,mergeIntoThis);
                    event.getBot().sendIRC().message(event.getChannel().getName(),mergeIntoThis+"'s overall score is: "+scores.getScore(mergeIntoThis));
                }
            }
            
            // Get your current score
            else if (command.equalsIgnoreCase("score")&&!Global.activeGames.isGameActive(event.getChannel().getName())){
                int globalScore = scores.getScore(event.getUser().getNick());
                
                if (globalScore < 0){
                    event.getBot().sendIRC().notice(event.getUser().getNick(), "USER NOT FOUND");
                }
                else
                    event.respond("Your overall score is: "+globalScore);
            }
            // Get someone elses current score
            else if (command.toLowerCase().startsWith("score")&&command.split(" ").length==2){
                String user = command.split(" ")[1];
                int globalScore = scores.getScore(user);
                
                if (globalScore < 0){
                    event.getBot().sendIRC().notice(event.getUser().getNick(), "USER NOT FOUND");
                }
                else
                    event.getBot().sendIRC().message(event.getChannel().getName(),user+"'s overall score is: "+globalScore);
            }
            // Save the scores file
            else if (command.equalsIgnoreCase("save")&&Global.botAdmins.contains(event.getUser().getNick())){
                scores.removeDupes();
                scores.saveToJSON();
            }
            // List out the overall standings of the trivia channel
            else if (command.equalsIgnoreCase("standings")&&!Global.activeGames.isGameActive(event.getChannel().getName())){
                int i=0;
                Collections.sort(scores);
                for(Score temp: scores){
                    // If a score is zero, ignore it
                    if (temp.getScore()>0)
                        event.getBot().sendIRC().message(event.getChannel().getName(), ++i + " : " + temp.getUser() + ", Score : " + temp.getScore());
                }
                // If nobody has a score, say that instead of saying nothing at all
                if (i==0){
                    event.getBot().sendIRC().message(event.getChannel().getName(), "Nobody's score is greater than zero at this moment");
                }
            }
        }
        
        if ((runTrivia||startVotes.start(event.getChannel().getName()))&&!Global.activeGames.isGameActive(event.getChannel().getName())){
            runTrivia = false;
            startVotes.clear();
            ScoreArray currentGame = scores.copyOutZeros();
            int key=(int) (Math.random()*100000+1);
            String triviaChan = event.getChannel().getName();
            Global.activeGames.activate(triviaChan);
            Question triviaQuestion = new Question();
            Answer triviaAnswer = new Answer(triviaQuestion.getAnswer());
            
            event.getBot().sendIRC().message(event.getChannel().getName(),"Question:");
            event.getBot().sendIRC().message(event.getChannel().getName(), triviaQuestion.getQuestion());
            
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
                    stopVotes.clear();
                    triviaQuestion.endQuestionUpdates();
                    queue.close();
                }
                
                else if (currentMessage.equalsIgnoreCase(Integer.toString(key))){
                    if(questionsTillEnd==0){
                        event.getBot().sendIRC().message(triviaChan,"No one got it. The answer was: "+Colors.BOLD+Colors.RED+triviaQuestion.getAnswer());
                        event.getBot().sendIRC().message(triviaChan,"Looks like nobody is around, Thanks for playing trivia! Come again soon!");
                        running = false;
                        stopVotes.clear();
                        triviaQuestion.endQuestionUpdates();
                        queue.close();
                    }
                    
                    else{
                        event.getBot().sendIRC().message(triviaChan,"No one got it. The answer was: "+Colors.BOLD+Colors.RED+triviaQuestion.getAnswer());
                        triviaQuestion.endQuestionUpdates();
                        triviaQuestion = new Question();
                        key=(int) (Math.random()*100000+1);
                        triviaAnswer = new Answer(triviaQuestion.getAnswer());
                        event.getBot().sendIRC().message(triviaChan,"Next Question:");
                        event.getBot().sendIRC().message(triviaChan,triviaQuestion.getQuestion());
                        triviaQuestion.startQuestionUpdates(event, triviaAnswer, triviaQuestion, timeBetweenUpdates,key);
                    }
                }
                
                else if (currentChan.equalsIgnoreCase(triviaChan)&&!currentEvent.getUser().getNick().equalsIgnoreCase(event.getBot().getNick())){
                    
                    if (currentMessage.startsWith(Global.commandPrefix)){
                        
                        String command = currentMessage.split(Global.commandPrefix)[1].toLowerCase();
                        // Get your current score
                        if (command.equalsIgnoreCase("score")){
                            
                            int currentScore = currentGame.getScore(event.getUser().getNick());
                            int globalScore = scores.getScore(event.getUser().getNick());
                            
                            if (currentScore < 0 || globalScore < 0){
                                currentEvent.getBot().sendIRC().notice(currentEvent.getUser().getNick(), "USER NOT FOUND");
                            }
                            else
                                currentEvent.respond("Your current score is: "+Colors.BOLD+Colors.RED+currentScore+Colors.NORMAL+" and your overall score is: "+Colors.BOLD+Colors.RED+globalScore);
                        }
                        // Get someone elses current score
                        else if (command.toLowerCase().startsWith("score")&&command.split(" ").length==2){
                            
                            String user = command.split(" ")[1];
                            int currentScore = currentGame.getScore(user);
                            int globalScore = scores.getScore(user);
                            
                            if (currentScore < 0 || globalScore < 0){
                                currentEvent.getBot().sendIRC().notice(currentEvent.getUser().getNick(), "USER NOT FOUND");
                            }
                            else
                                currentEvent.getBot().sendIRC().message(triviaChan,user+"'s current score is "+Colors.BOLD+Colors.RED+currentScore+Colors.NORMAL+" and their overall score is "+Colors.BOLD+Colors.RED+globalScore);
                        }
                        // Get the current game's standings
                        else if (command.equalsIgnoreCase("standings")){
                            int i=0;
                            Collections.sort(currentGame);
                            for(Score temp: currentGame){
                                // If a score is zero, ignore it
                                if (temp.getScore()>0)
                                    event.getBot().sendIRC().message(event.getChannel().getName(), ++i + " : " + temp.getUser() + ", Score : " + temp.getScore());
                            }
                            // If nobody has a score greater than zero, say so
                            if (i==0){
                                event.getBot().sendIRC().message(event.getChannel().getName(), "Nobody's score is greater than zero at this moment");
                            }
                        }
                    }
                    if (currentMessage.equalsIgnoreCase(triviaQuestion.getAnswer())){
                        event.getBot().sendIRC().message(triviaChan,currentEvent.getUser().getNick()+" GOT IT!");
                        int cluesGiven = triviaQuestion.getClueCount();
                        if (levels.size()>cluesGiven){
                            currentGame.addScore(currentEvent.getUser().getNick(), levels.get(cluesGiven));
                            event.getBot().sendIRC().message(triviaChan,levels.get(cluesGiven)+" points have been added to your score");
                        }
                        
                        triviaQuestion.endQuestionUpdates();
                        triviaQuestion = new Question();
                        key=(int) (Math.random()*100000+1);
                        triviaAnswer = new Answer(triviaQuestion.getAnswer());
                        event.getBot().sendIRC().message(triviaChan,"Next Question:");
                        event.getBot().sendIRC().message(triviaChan,triviaQuestion.getQuestion());
                        triviaQuestion.startQuestionUpdates(event, triviaAnswer, triviaQuestion, timeBetweenUpdates,key);
                    }
                    questionsTillEnd = numQuestionsAllowedTillEnd;
//                    System.out.println("questions till end reset");
                }
                
                if (currentChan.equalsIgnoreCase(triviaChan)&&currentEvent.getUser().getNick().equalsIgnoreCase(event.getBot().getNick())){ //if(currentEvent.getMessage().equalsIgnoreCase(Integer.toString(key)))
                    questionsTillEnd--; // Every time the bot sends a message with a question update, reduce the number of questions till the game auto-ends
                }
            }
            scores.merge(currentGame);
            Global.activeGames.deactivate(triviaChan);
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
    
    @Override // Grabs users who join the channel and adds them to the score list
    public void onJoin(JoinEvent event){
        if (!scores.containsUser(event.getUser().getNick())){
            scores.addUser(event.getUser().getNick());
            scores.saveToJSON();
        }
    }
    
    @Override // Grabs the user list and adds them to the score list
    public void onUserList(UserListEvent event){
        ImmutableSortedSet users = event.getUsers();
        
        Iterator<User> iterator = users.iterator();
        boolean modified = false;
        while(iterator.hasNext()) {
            User element = iterator.next();
            if (!scores.containsUser(element.getNick())){
                //temp = (User)users.floor(temp);
                scores.addUser(element.getNick());
                System.out.println(element.getNick());
                modified = true;
            }
        }
        if (modified)
            scores.saveToJSON();
    }
}