 /**
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */

package triviabot;

import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import triviabot.Vote.VoteLog;

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
    int votesTillStart = 3; // Number of votes needed to start trivia
    int key=(int) (Math.random()*100000+1);
    VoteLog votes = new Vote.VoteLog(); // Log of current votes for starting trivia
    
    @Override
    public void onMessage(MessageEvent event) throws InterruptedException, Exception {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        if (message.startsWith(Global.commandPrefix)){
            String command = message.split(Global.commandPrefix)[1].toLowerCase();
            if (command.equalsIgnoreCase("start")&&Global.botAdmins.contains(event.getUser().getNick())){
                runTrivia = true;
            }
            else if (command.equalsIgnoreCase("start")){
                votes.addVote(event.getUser().getNick());
            }
            else if (command.equalsIgnoreCase("end")&&Global.botAdmins.contains(event.getUser().getNick())){
                runTrivia = false;
            }
        }
        
        
        runTrivia = votes.start();
        
        if (runTrivia){
            
        }
        
        
        
    }
}
