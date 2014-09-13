/**
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
 * 
 */
public class TriviaMain extends ListenerAdapter{
    @Override
    public void onMessage(MessageEvent event) throws InterruptedException, Exception {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        
        
        
        
//<theDoctor> Steve-O!!! make a kick function for the channel
//<theDoctor> !kick burg 
//<theDoctor> then it starts a trivia that only that person can answer
//<theDoctor> if they get it right quickly
//<theDoctor> like reduced time
//<theDoctor> then it will kick the person
//<Steve-O> if you don't get the question, you get kicked, if you get the question, they get kicked
//<theDoctor> trivia WARS
//<theDoctor> hell, make the stakes higher to use it
//<theDoctor> 1min ban
//<theDoctor> for the kicker
//<theDoctor> no ban on kickee
        
        
    }
}
