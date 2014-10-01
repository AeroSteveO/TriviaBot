/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Objects;

import java.util.Vector;

/**
 *
 * @author Steve-O
 * 
 * Object: 
 *      TriviaGames
 * - Contains channels that the bot is in and whether they have active games of 
 *   trivia or not
 * - Votes can be added to the log using addVote, this allows the object to control
 *   vote expiration and prevent duplicates
 * Methods:
 *     *isActive   - Returns true if the channel is currently active, false if not
 *     *activate   - Sets the channel to active state of trivia
 *     *deactivate - Sets the channel to in-active state of trivia
 *     *getChannel - Returns the channel string of the object
 * 
 * Object: 
 *      TriviaArray
 * - Array of TriviaGames o
 * - Votes can be added to the log using addVote, this allows the object to control
 *   vote expiration and prevent duplicates
 * Methods:
 *     *isActive   - Returns true if the channel is currently active, false if not
 *     *activate   - Sets the channel to active state of trivia
 *     *deactivate - Sets the channel to in-active state of trivia
 *     *getChannel - Returns the channel string of the object
 * 
 */
public class TriviaGames {
    private String channel = null;
    private boolean active = false;
    
    public TriviaGames(String chan, boolean active){
        this.channel = chan;
        this.active = active;
    }
    public void activate(){
        this.active = true;
    }
    public void deactivate(){
        this.active = false;
    }
    public boolean isActive(){
        return active;
    }
    public String getChannel(){
        return this.channel;
    }
    
    
    public class TriviaArray extends Vector<TriviaGames>{
        public boolean isGameActive(String inputChannel) {
            if (!this.isEmpty()){
                for (int i=0;i<this.size();i++){
                    if(this.get(i).isActive()){
                        return true;
                    }
                }
            }
            return(false);
        }
        public int getGameIdx(String channel){
            int idx = -1;
            for(int i = 0; i < this.size(); i++) {
                if (this.get(i).getChannel().equalsIgnoreCase(channel)) {
                    idx = i;
                    break;
                }
            }
            return (idx);
        }
        public TriviaGames getGame(String channel){
            return (this.get(this.getGameIdx(channel)));
        }
    }
}
