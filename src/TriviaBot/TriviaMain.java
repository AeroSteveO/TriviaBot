/**
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package TriviaBot;

import Objects.Answer;
import Objects.GameList;
import Objects.Question;
import Objects.Score;
import Objects.Score.ScoreArray;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import Objects.Vote.VoteLog;
import Utils.TextUtils;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    int numQuestionsAllowedTillEnd = 3;                     // Number of questions allowed to go without response till trivia ends
    int questionsTillAutoEnd = numQuestionsAllowedTillEnd;  // Number of questions left till trivia ends
    int timeBetweenUpdates = 10;                            // Seconds between each clue update    //30
    int time = 20;               // Seconds between the start of the trivia challenge and failure //160
    
    VoteLog startVotes = new VoteLog();                     // Log of current Votes for starting trivia
    VoteLog stopVotes  = new VoteLog();                     // Log of current Votes for stopping trivia
    
    public static Question previousQuestion = null;
    public static Question currentQuestion = null;
    
    ScoreArray scores = new ScoreArray();
    String filename = "scores.json";
    boolean loaded = startScores();
    ArrayList<Integer> levels = scoreLevels();
    public static GameList gameList = new GameList();
    
    @Override
    public void onMessage(MessageEvent event) throws InterruptedException, Exception {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        String gameChan = event.getChannel().getName();
        
        if (message.startsWith(Global.commandPrefix)){
            String command = message.split(Global.commandPrefix)[1];//.toLowerCase()
            String[] cmdSplit = command.split(" ");
            
            // Admin start
            if (command.equalsIgnoreCase("start")
                    &&Global.botAdmins.contains(event.getUser().getNick())&&event.getUser().isVerified()){
                
                runTrivia = true;
            }
            // User start
            else if (command.equalsIgnoreCase("start")){
                startVotes.addVote(event.getUser().getNick(),event.getChannel().getName());
            }
            // Admin stop
            else if (command.equalsIgnoreCase("stop")
                    &&Global.botAdmins.contains(event.getUser().getNick())&&event.getUser().isVerified()){
                
                runTrivia = false;
            }
            // User stop
            else if (command.equalsIgnoreCase("stop")){
                stopVotes.addVote(event.getUser().getNick(),event.getChannel().getName());
            }
            // Admin stop
            else if (command.equalsIgnoreCase("stop")
                    &&Global.botAdmins.contains(event.getUser().getNick())&&event.getUser().isVerified()){
                
                runTrivia = false;
            }
            
            else if (cmdSplit[0].equalsIgnoreCase("report")&&!gameList.contains(new String[] {gameChan, "trivia", "long"})){
                if (cmdSplit.length==2){
                    if(cmdSplit[1].equalsIgnoreCase("current")){
                        if (currentQuestion == null)
                            event.respond("No current trivia question");
                        else{
                            TextUtils.addToDoc("TriviaQuestionIssues.txt", event.getUser().getNick()+" is reporting: "+currentQuestion.getRaw());
                            event.getBot().sendIRC().message(event.getChannel().getName(),"Question: "+Colors.RED+currentQuestion.getQuestion()+Colors.NORMAL+" has been marked for correction");
                            event.getBot().sendIRC().message(Global.botOwner, "A new question has been reported by "+event.getUser().getNick());
                        }
                    }
                    else if (cmdSplit[1].equalsIgnoreCase("previous")){
                        if (previousQuestion == null)
                            event.respond("No previous trivia question");
                        else{
                            TextUtils.addToDoc("TriviaQuestionIssues.txt", event.getUser().getNick()+" is reporting: "+previousQuestion.getRaw());
                            event.getBot().sendIRC().message(event.getChannel().getName(),"Question: "+Colors.RED+previousQuestion.getQuestion()+Colors.NORMAL+" has been marked for correction");
                            event.getBot().sendIRC().message(Global.botOwner, "A new question has been reported by "+event.getUser().getNick());
                        }
                    }
                    else{
                        event.respond("Report requires an input of either 'current' or 'previous' to signify which question you are reporting");
                    }
                }
                else{
                    event.respond("Report requires an input of either 'current' or 'previous' to signify which question you are reporting");
                }
            }
            
            else if (cmdSplit[0].equalsIgnoreCase("combine")&&cmdSplit.length==3
                    &&Global.botAdmins.contains(event.getUser().getNick())&&event.getUser().isVerified()) {
                
                String mergeThis = cmdSplit[1];
                String mergeIntoThis = cmdSplit[2];
                
                if (scores.getScore(mergeThis) == Integer.MIN_VALUE){
                    event.getBot().sendIRC().notice(event.getUser().getNick(), mergeThis+": USER NOT FOUND");
                }
                else if (scores.getScore(mergeIntoThis) == Integer.MIN_VALUE){
                    event.getBot().sendIRC().notice(event.getUser().getNick(), mergeIntoThis+": USER NOT FOUND");
                }
                
                else{
                    scores.merge(mergeThis,mergeIntoThis);
                    event.getBot().sendIRC().message(event.getChannel().getName(),mergeIntoThis+"'s overall score is: "+scores.getScore(mergeIntoThis));
                }
            }
            
            // Get your current score
//            else if (command.equalsIgnoreCase("score")&&!Global.activeGames.isGameActive(event.getChannel().getName())){
//                int globalScore = scores.getScore(event.getUser().getNick());
//
//                if (globalScore == Integer.MIN_VALUE){
//                    event.getBot().sendIRC().notice(event.getUser().getNick(), "USER NOT FOUND");
//                }
//                else
//                    event.respond("Your overall score is: "+globalScore);
//            }
            // Get someone elses current score
            else if (cmdSplit[0].equalsIgnoreCase("score")){
                if(cmdSplit.length==1&&!gameList.contains(new String[] {gameChan, "trivia", "long"})){
                    int globalScore = scores.getScore(event.getUser().getNick());
                    
                    if (globalScore == Integer.MIN_VALUE){
                        event.getBot().sendIRC().notice(event.getUser().getNick(), "USER NOT FOUND");
                    }
                    else
                        event.respond("Your overall score is: "+globalScore);
                }
                else if(command.split(" ").length==2){
                    String user = cmdSplit[1];
                    int globalScore = scores.getScore(user);
                    
                    if (globalScore == Integer.MIN_VALUE){
                        event.getBot().sendIRC().notice(event.getUser().getNick(), "USER NOT FOUND");
                    }
                    else
                        event.getBot().sendIRC().message(event.getChannel().getName(),user+"'s overall score is: "+globalScore);
                }
                else if (command.split(" ").length==3
                        &&Global.botAdmins.contains(event.getUser().getNick())&&event.getUser().isVerified()) {
                    
                    String user = command.split(" ")[1];
                    String score = command.split(" ")[2];
                    int userCurrentScore = scores.getScore(user);
                    
                    if (userCurrentScore == Integer.MIN_VALUE){
                        event.getBot().sendIRC().notice(event.getUser().getNick(), "USER NOT FOUND");
                    }
                    
                    else if (!score.matches("[0-9]+")){
                        event.getBot().sendIRC().notice(event.getUser().getNick(),"Input number must be an integer");
                    }
                    
                    else if(score.matches("\\-[0-9]+")){
                        event.getBot().sendIRC().notice(event.getUser().getNick(),"You cannot give a user a negative score");
                    }
                    
                    else{
                        scores.setScore(user, Integer.parseInt(score));
                        event.getBot().sendIRC().message(event.getChannel().getName(),user+"'s overall score is: "+scores.getScore(user));
                    }
                }
            }
            
            // Save the scores file
            else if (command.equalsIgnoreCase("save")
                    &&Global.botAdmins.contains(event.getUser().getNick())&&event.getUser().isVerified()){
                
                scores.clean();
                scores.saveToJSON();
            }
            // List out the overall standings of the trivia channel
            else if (command.equalsIgnoreCase("standings")&&!gameList.contains(new String[] {gameChan, "trivia", "long"})){
                int i=0;
                scores.sort();
                List<Score> scoreList = scores.getList();
                for(Score temp: scoreList){
                    // If a score is zero, ignore it
                    if (temp.getScore()>0 && i<5)
                        event.getBot().sendIRC().message(event.getChannel().getName(), ++i + " : " + temp.getUser() + ", Score : " + temp.getScore());
                }
                // If nobody has a score, say that instead of saying nothing at all
                if (i==0){
                    event.getBot().sendIRC().message(event.getChannel().getName(), "Nobody's score is greater than zero at this moment");
                }
            }
            
            else if (cmdSplit[0].equalsIgnoreCase("standings")&&cmdSplit.length==2&&!gameList.contains(new String[] {gameChan, "trivia", "long"})){
                
                if (cmdSplit[1].matches("[0-9]+")){
                    int lim = Integer.parseInt(cmdSplit[1]);
                    int i=0;
                    scores.sort();
                    List<Score> scoreList = scores.getList();
                    for(Score temp: scoreList){
                        // If a score is zero, ignore it
                        if (temp.getScore()>0 && i<lim)
                            event.getBot().sendIRC().message(event.getChannel().getName(), ++i + " : " + temp.getUser() + ", Score : " + temp.getScore());
                    }
                    // If nobody has a score, say that instead of saying nothing at all
                    if (i==0){
                        event.getBot().sendIRC().message(event.getChannel().getName(), "Nobody's score is greater than zero at this moment");
                    }
                }
                
                else if(cmdSplit[1].matches("\\-{0,1}[0-9\\.]+")){
                    event.getBot().sendIRC().notice(event.getUser().getNick(),"You must input a non-negative integer");
                    return;
                }
                
                else{
                    int globalScore = scores.getScore(cmdSplit[1]);
                    
                    if (globalScore ==Integer.MIN_VALUE){
                        event.getBot().sendIRC().notice(event.getUser().getNick(), "USER NOT FOUND");
                        return;
                    }
                    else{
                        
                        int i=0;
//                int lim = Integer.parseInt(cmdSplit[1]);
                        scores.sort();
                        List<Score> scoreList = scores.getList();
                        for(Score temp: scoreList){
                            // If a score is zero, ignore it
                            if (temp.getScore()>0){
                                i++;
                                if (temp.getUser().equalsIgnoreCase(cmdSplit[1])){
                                    event.getBot().sendIRC().message(event.getChannel().getName(), i + " : " + temp.getUser() + ", Score : " + temp.getScore());
                                }
                            }
                        }
                        // If nobody has a score, say that instead of saying nothing at all
                        if (i==0){
                            event.getBot().sendIRC().message(event.getChannel().getName(), "Nobody's score is greater than zero at this moment");
                        }
                    }
                }
            }
        }
        
        if ((runTrivia||startVotes.start(event.getChannel().getName()))&&!gameList.contains(new String[] {gameChan, "trivia", "long"})){
            gameList.add(gameChan, "trivia", "long");
            if (startVotes.start(event.getChannel().getName())){
                ArrayList<String> voters = startVotes.getUsers();
                String startMessage="";
                for (int i=0;i<voters.size();i++){
                    startMessage+=voters.get(i)+", ";
                }
                startMessage+=" your trivia game will be starting soon";
                event.getBot().sendIRC().message(event.getChannel().getName(), startMessage);
            }
            
            runTrivia = false;
            startVotes.clear();
            ScoreArray currentGame = scores.copyOutZeros();
            
            int key=(int) (Math.random()*100000+1);
            int updateKey = (int) (Math.random()*100000+1);
            int counter = 0;
            
            VoteLog skipVotes = new VoteLog();
            
            String triviaChan = event.getChannel().getName();
//            Global.activeGames.activate(triviaChan);
            
            Question triviaQuestion = new Question();
            currentQuestion = triviaQuestion;
            Answer triviaAnswer = new Answer(triviaQuestion.getAnswer());
            
            event.getBot().sendIRC().message(event.getChannel().getName(),"Question:");
            event.getBot().sendIRC().message(event.getChannel().getName(), triviaQuestion.getQuestion());
            event.getBot().sendIRC().message(event.getChannel().getName(),"Clue: "+triviaAnswer.getClue());
            
            WaitForQueue queue = new WaitForQueue(event.getBot());
            triviaQuestion.startQuestionUpdates(event, timeBetweenUpdates, key, updateKey);
            boolean running = true;
            int questionsTillEnd = numQuestionsAllowedTillEnd;
            
            while (running){
                MessageEvent currentEvent = queue.waitFor(MessageEvent.class);
                String currentMessage = Colors.removeFormattingAndColors(currentEvent.getMessage());
                String currentChan = currentEvent.getChannel().getName();
                if ((currentMessage.equalsIgnoreCase(Global.commandPrefix+"stop")&&currentChan.equalsIgnoreCase(triviaChan)
                        &&Global.botAdmins.contains(currentEvent.getUser().getNick())&&currentEvent.getUser().isVerified())
                        ||stopVotes.start()){
                    
                    event.getBot().sendIRC().message(triviaChan,"Thanks for playing trivia!");
                    running = false;
                    stopVotes.clear();
                    
                    previousQuestion = triviaQuestion;
                    triviaQuestion.endQuestionUpdates();
                    currentQuestion = null;
                    
                    queue.close();
                }
                
                else if (currentMessage.equalsIgnoreCase(Integer.toString(updateKey))){
                    
                    event.getBot().sendIRC().message(currentChan,"Clue: "+triviaAnswer.giveClue());
                    counter++;
                }
                
                else if (currentMessage.equalsIgnoreCase(Integer.toString(key))){
                    if(questionsTillEnd==0){
                        event.getBot().sendIRC().message(triviaChan,"No one got it. The answer was: "+Colors.BOLD+Colors.RED+triviaQuestion.getAnswer());
                        event.getBot().sendIRC().message(triviaChan,"Looks like nobody is around, Thanks for playing trivia! Come again soon!");
                        running = false;
                        stopVotes.clear();
                        
                        previousQuestion = triviaQuestion;
                        triviaQuestion.endQuestionUpdates();
                        currentQuestion = null;
                        
                        queue.close();
                    }
                    
                    else{
                        event.getBot().sendIRC().message(triviaChan,"No one got it. The answer was: "+Colors.BOLD+Colors.RED+triviaQuestion.getAnswer());
                        
                        triviaQuestion.endQuestionUpdates();
                        previousQuestion = triviaQuestion;
                        triviaQuestion = new Question();
                        currentQuestion = triviaQuestion;
                        
                        key=(int) (Math.random()*100000+1);
                        updateKey = (int) (Math.random()*100000+1);
                        triviaAnswer = new Answer(triviaQuestion.getAnswer());
                        
                        event.getBot().sendIRC().message(triviaChan,"Next Question:");
                        event.getBot().sendIRC().message(triviaChan,triviaQuestion.getQuestion());
                        event.getBot().sendIRC().message(triviaChan,"Clue: "+triviaAnswer.getClue());
                        
                        triviaQuestion.startQuestionUpdates(event, timeBetweenUpdates, key, updateKey);
                        counter = 0;
                    }
                }
                
                else if (currentChan.equalsIgnoreCase(triviaChan)&&!currentEvent.getUser().getNick().equalsIgnoreCase(event.getBot().getNick())){
                    
                    if (currentMessage.startsWith(Global.commandPrefix)){
                        
                        String command = currentMessage.split(Global.commandPrefix)[1].toLowerCase();
                        String[] cmdSplit = command.split(" ");
                        // Get your current score
                        if (command.equalsIgnoreCase("score")){
                            
                            int currentScore = currentGame.getScore(currentEvent.getUser().getNick());
                            int globalScore = scores.getScore(currentEvent.getUser().getNick());
                            
                            if (currentScore == Integer.MIN_VALUE || globalScore == Integer.MIN_VALUE){
                                currentEvent.getBot().sendIRC().notice(currentEvent.getUser().getNick(), "USER NOT FOUND");
                            }
                            else
                                currentEvent.respond("Your current score is: "+Colors.BOLD+Colors.RED+currentScore+Colors.NORMAL+" and your overall score is: "+Colors.BOLD+Colors.RED+globalScore);
                        }
                        
                        else if (cmdSplit[0].equalsIgnoreCase("report")){
                            if (cmdSplit.length==2){
                                if(cmdSplit[1].equalsIgnoreCase("current")){
                                    TextUtils.addToDoc("TriviaQuestionIssues.txt", currentEvent.getUser().getNick()+" is reporting: "+currentQuestion.getRaw());
                                    currentEvent.getBot().sendIRC().message(currentEvent.getChannel().getName(),"Question: "+Colors.RED+currentQuestion.getQuestion()+Colors.NORMAL+" has been marked for correction");
                                    currentEvent.getBot().sendIRC().message(Global.botOwner, "A new question has been reported by "+currentEvent.getUser().getNick());
                                }
                                else if (cmdSplit[1].equalsIgnoreCase("previous")){
                                    if (previousQuestion == null){
                                        event.respond("No previous question available to report");
                                    }
                                    else{
                                        TextUtils.addToDoc("TriviaQuestionIssues.txt", currentEvent.getUser().getNick()+" is reporting: "+previousQuestion.getRaw());
                                        currentEvent.getBot().sendIRC().message(currentEvent.getChannel().getName(),"Question: "+Colors.RED+previousQuestion.getQuestion()+Colors.NORMAL+" has been marked for correction");
                                        currentEvent.getBot().sendIRC().message(Global.botOwner, "A new question has been reported by "+currentEvent.getUser().getNick());
                                    }
                                }
                                else{
                                    currentEvent.respond("Report requires an input of either 'current' or 'previous' to signify which question you are reporting");
                                }
                            }
                            else{
                                currentEvent.respond("Report requires an input of either 'current' or 'previous' to signify which question you are reporting");
                            }
                        }
                        // Get someone elses current score
                        else if (cmdSplit[0].equalsIgnoreCase("score")&&cmdSplit.length==2){
                            
                            String user = command.split(" ")[1];
                            int currentScore = currentGame.getScore(user);
                            int globalScore = scores.getScore(user);
                            
                            if (currentScore == Integer.MIN_VALUE || globalScore == Integer.MIN_VALUE){
                                currentEvent.getBot().sendIRC().notice(currentEvent.getUser().getNick(), "USER NOT FOUND");
                            }
                            else
                                currentEvent.getBot().sendIRC().message(triviaChan,user+"'s current score is "+Colors.BOLD+Colors.RED+currentScore+Colors.NORMAL+" and their overall score is "+Colors.BOLD+Colors.RED+globalScore);
                        }
                        // SKIP THE CURRENT QUESTION
                        else if (cmdSplit[0].equalsIgnoreCase("skip")){
                            // Admin start
                            if (skipVotes.start()||(Global.botAdmins.contains(event.getUser().getNick())&&event.getUser().isVerified())){
                                
//                                event.getBot().sendIRC().message(triviaChan,"No one got it. The answer was: "+Colors.BOLD+Colors.RED+triviaQuestion.getAnswer());
                                event.getBot().sendIRC().message(triviaChan,"This question will be skipped. The answer was: "+Colors.BOLD+Colors.RED+triviaQuestion.getAnswer());
                                
                                triviaQuestion.endQuestionUpdates();
                                previousQuestion = triviaQuestion;
                                triviaQuestion = new Question();
                                currentQuestion = triviaQuestion;
                                
                                key=(int) (Math.random()*100000+1);
                                updateKey = (int) (Math.random()*100000+1);
                                triviaAnswer = new Answer(triviaQuestion.getAnswer());
                                
                                event.getBot().sendIRC().message(triviaChan,"Next Question:");
                                event.getBot().sendIRC().message(triviaChan,triviaQuestion.getQuestion());
                                event.getBot().sendIRC().message(triviaChan,"Clue: "+triviaAnswer.getClue());
                                
                                triviaQuestion.startQuestionUpdates(event, timeBetweenUpdates, key, updateKey);
                                counter = 0;
                                skipVotes.clear();
//                                runTrivia = true;
                            }
                            // User start
                            else {
                                skipVotes.addVote(event.getUser().getNick(),event.getChannel().getName());
                            }
                        }
                        
                        // Get the current game's standings
                        else if (command.equalsIgnoreCase("standings")){
                            int i=0;
                            currentGame.sort();
                            List<Score> scoreList = currentGame.getList();
                            for(Score temp: scoreList){
                                // If a score is zero, ignore it
                                if (temp.getScore()>0)
                                    currentEvent.getBot().sendIRC().message(triviaChan, ++i + " : " + temp.getUser() + ", Score : " + temp.getScore());
                            }
                            // If nobody has a score greater than zero, say so
                            if (i==0){
                                currentEvent.getBot().sendIRC().message(triviaChan, "Nobody's score is greater than zero at this moment");
                            }
                        }
                        else if (cmdSplit[0].equalsIgnoreCase("standings")&&cmdSplit.length==2){
                            
                            if (cmdSplit[1].matches("[0-9]+")){
                                int lim = Integer.parseInt(cmdSplit[1]);
                                int i=0;
                                currentGame.sort();
                                List<Score> scoreList = currentGame.getList();
                                for(Score temp: scoreList){
                                    // If a score is zero, ignore it
                                    if (temp.getScore()>0 && i<lim)
                                        currentEvent.getBot().sendIRC().message(triviaChan, ++i + " : " + temp.getUser() + ", Score : " + temp.getScore());
                                }
                                // If nobody has a score, say that instead of saying nothing at all
                                if (i==0){
                                    currentEvent.getBot().sendIRC().message(triviaChan, "Nobody's score is greater than zero at this moment");
                                }
                            }
                            
                            else if(cmdSplit[1].matches("\\-{0,1}[0-9\\.]+")){
                                currentEvent.getBot().sendIRC().notice(triviaChan,"You must input a non-negative integer");
                                return;
                            }
                            
                            else{
                                int globalScore = currentGame.getScore(cmdSplit[1]);
                                
                                if (globalScore == Integer.MIN_VALUE){
                                    currentEvent.getBot().sendIRC().notice(triviaChan, "USER NOT FOUND");
                                    return;
                                }
                                else{
                                    
                                    int i=0;
//                int lim = Integer.parseInt(cmdSplit[1]);
                                    currentGame.sort();
                                    List<Score> scoreList = currentGame.getList();
                                    for(Score temp: scoreList){
                                        // If a score is zero, ignore it
                                        if (temp.getScore()>0){
                                            i++;
                                            if (temp.getUser().equalsIgnoreCase(cmdSplit[1])){
                                                currentEvent.getBot().sendIRC().message(triviaChan, i + " : " + temp.getUser() + ", Score : " + temp.getScore());
                                            }
                                        }
                                    }
                                    // If nobody has a score, say that instead of saying nothing at all
                                    if (i==0){
                                        currentEvent.getBot().sendIRC().message(triviaChan, "Nobody's score is greater than zero at this moment");
                                    }
                                }
                            }
                        }
                    }
                    if (currentMessage.equalsIgnoreCase(triviaQuestion.getAnswer())){
                        currentEvent.getBot().sendIRC().message(triviaChan,currentEvent.getUser().getNick()+" GOT IT!");
//                        int cluesGiven = triviaQuestion.getClueCount();
                        if (levels.size()>counter){
                            currentGame.addScore(currentEvent.getUser().getNick(), levels.get(counter));
                            currentEvent.getBot().sendIRC().message(triviaChan,levels.get(counter)+" points have been added to your score");
                        }
                        counter = 0;
                        
                        triviaQuestion.endQuestionUpdates();
                        previousQuestion = triviaQuestion;
                        triviaQuestion = new Question();
                        currentQuestion = triviaQuestion;
                        
                        key=(int) (Math.random()*100000+1);
                        updateKey = (int) (Math.random()*100000+1);
                        triviaAnswer = new Answer(triviaQuestion.getAnswer());
                        
                        currentEvent.getBot().sendIRC().message(triviaChan,"Next Question:");
                        currentEvent.getBot().sendIRC().message(triviaChan,triviaQuestion.getQuestion());
                        currentEvent.getBot().sendIRC().message(triviaChan,"Clue: "+triviaAnswer.getClue());
                        
                        triviaQuestion.startQuestionUpdates(event, timeBetweenUpdates, key, updateKey);
                    }
                    questionsTillEnd = numQuestionsAllowedTillEnd;
//                    System.out.println("questions till end reset");
                }
                
                if (currentChan.equalsIgnoreCase(triviaChan)&&currentEvent.getUser().getNick().equalsIgnoreCase(event.getBot().getNick())){ //if(currentEvent.getMessage().equalsIgnoreCase(Integer.toString(key)))
                    questionsTillEnd--; // Every time the bot sends a message with a question update, reduce the number of questions till the game auto-ends
                }
            }
            scores.merge(currentGame);
//            Global.activeGames.deactivate(triviaChan);
            gameList.remove(gameChan, "trivia");
            startVotes.clear();
            stopVotes.clear();
            runTrivia=false;
        }
        else if (gameList.contains(new String[] {gameChan, "trivia", "long"})&&message.equalsIgnoreCase(Global.commandPrefix+"start")){
            event.getBot().sendIRC().notice(event.getUser().getNick(), Colors.BOLD+"Trivia: "+Colors.NORMAL+"Only one trivia game may be played at a time");
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