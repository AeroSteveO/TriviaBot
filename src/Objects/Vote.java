/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Objects;

import java.util.ArrayList;
import org.joda.time.DateTime;

/**
 *
 * @author Steve-O
 * Object: 
 *      Vote
 * - Vote requires name of voter to prevent multiple votes by the same person
 * - Votes expire 10 minutes after being added, to prevent a game from starting 
 *   when the votes occured days ago
 * 
 * Methods:
 *      isAfterExpiration - Checks to see if a vote is after the expiration time
 *      getChan           - Returns the channel of the vote object
 * 
 * Object: 
 *      VoteLog
 * - An ArrayList of votes for simple control of the object
 * - Votes can be added to the log using addVote, this allows the object to control
 *   vote expiration and prevent duplicates
 * 
 * Methods:
 *     *start        - Returns true if there are enough votes to start the game
 *      containsVote - Returns true if the nickname already voted in this round of voting
 *     *addVote      - Adds the vote to the vote log only if the user hasn't voted yet in this round
 *      purge        - Removes all votes that are after expiration
 *
 * Note: Only commands marked with a * are available for use outside the object
 */
public class Vote {
    private DateTime expiration;
    private String voter;
    private String channel;
    private static int numVotesToRun = 3;
    
    public Vote(String nick){
        this.expiration = new DateTime().plusMinutes(10);
        this.voter = nick;
    }
    public Vote(String nick, String channel){
        this.expiration = new DateTime().plusMinutes(10);
        this.voter = nick;
        this.channel = channel;
    }
    private boolean isAfterExpiration(){
        if (new DateTime().isAfter(expiration)){
            return(true);
        }
        return(false);
    }
    private String getChan(){
        return this.channel;
    }
    
    public static class VoteLog extends ArrayList<Vote>{
        
        public boolean start(){
            this.purge();
            return (this.size()>=numVotesToRun);
        }
        private boolean containsVote(String toCheck){
            for(int i = 0; i < this.size(); i++) {
                if (this.get(i).voter.equalsIgnoreCase(toCheck)) {
                    return(true);
                }
            }
            return (false);
        }
        public void addVote(String nick){
            this.purge();
            if (!containsVote(nick)){
                this.add(new Vote(nick));
            }
        }
        private void purge(){
            for (int i=0;i<this.size();i++){
                if(this.get(i).isAfterExpiration()){
                    this.remove(i);
                    i--;
                }
            }
        }
        public boolean start(String chan){
            this.purge();
            int count = 0;
            for (int i=0;i<this.size();i++){
                if (this.get(i).getChan().equalsIgnoreCase(chan)){
                    count++;
                }
            }
            return (count>=numVotesToRun);
        }
        private boolean containsVote(String nick,String chan){
            for(int i = 0; i < this.size(); i++) {
                if (this.get(i).voter.equalsIgnoreCase(nick)&&this.get(i).getChan().equalsIgnoreCase(chan)) {
                    return(true);
                }
            }
            return (false);
        }
        public void addVote(String nick, String chan){
            this.purge();
            if (!containsVote(nick)){
                this.add(new Vote(nick,chan));
            }
        }
    }
}