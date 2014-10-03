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
 * Methods:
 *      isActive   - Returns true if the channel is currently active, false if not
 *      activate   - Sets the channel to active state of trivia
 *      deactivate - Sets the channel to in-active state of trivia
 *      getChannel - Returns the channel string of the object
 *
 * Object:
 *      TriviaArray
 * - Array of TriviaGames
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
    private void activate(){
        this.active = true;
    }
    private void deactivate(){
        this.active = false;
    }
    private boolean isActive(){
        return active;
    }
    private String getChannel(){
        return this.channel;
    }
    
    
    public static class TriviaArray extends Vector<TriviaGames>{
        public boolean isGameActive(String inputChannel) {
            if (!this.isEmpty()){
                for (int i=0;i<this.size();i++){
                    if(this.get(i).isActive()){
                        return true;
                    }
                    else
                        return false;
                }
            }
            this.add(new TriviaGames(inputChannel, false));
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
        public void activate(String channel){
            this.get(this.getGameIdx(channel)).activate();
        }
        public void deactivate(String channel){
            this.get(this.getGameIdx(channel)).deactivate();
        }
    }
}
