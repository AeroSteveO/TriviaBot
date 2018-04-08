/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package rapternet.irc.bots.triviabot.objects;

import rapternet.irc.bots.triviabot.Global;
import org.pircbotx.PircBotX;
import org.pircbotx.Utils;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Steve-O
 * 
 * Requirements:
 * - APIs
 *    N/A
 * - Custom Objects
 *    N/A
 * - Linked Classes
 *    Global
 * 
 * Object:
 *      QuestionUpdater
 * - Requires multiple inputs to properly send out updates to the channel
 * - Automatically gets a new clue to the question and sends that clue to the channel
 *
 * Methods:
 *     *giveT    - Gives a thread to the object
 *     *end      - Ends the automatic updating, isn't an instant stop, but it won't
 *                 send any more updates to the channel after this is used
 *     *run      - The heart of the auto-updating, a looped thread
 *     *getCount - Gets the current counter value for the number of clues given
 *
 * Note: Only commands marked with a * are available for use outside the object
 */
public class QuestionUpdater implements Runnable{
    private PircBotX bot; // Bot object to use to send out updates to IRC
    private Thread t;     // Thread for the updater to do its thing in
    private boolean running = true;
    private String channel = null;  // Channel to send updates to
    private Answer answer;
    private int time;     // Amount of time between question updates
    private int key = 0;  // Key to send out to stop the current message queue
    private MessageEvent event;
    private int counter;  // Number of clues given by the question updater
    private int updateKey = 0;
    
    /**
     * 
     * @param event The event to send a response back through for signaling the question cycle and clue cycle
     * @param time The amount of time to wait between clues (Seconds)
     * @param endKey This key is sent out in an event to signal the end of the trivia question cycle
     * @param updateKey This key is sent out in an event to signal the trivia system to give another clue
     */
    QuestionUpdater(MessageEvent event, int time, int endKey, int updateKey){
        this.bot = event.getBot();
        this.channel = event.getChannel().getName();
        this.running = true;
        this.time = time; // Seconds
        this.key = endKey;
        this.event = event;
        this.updateKey = updateKey;
    }
        
    public void giveT(Thread t) {
        this.t = t;
    }
    
    public void end() throws InterruptedException{
        this.running = false;
        this.t.interrupt();
        t.join(1000); //Ensure the thread also closes
    }
    
    @Override
    public void run() {
        this.running = true;
        
        if (this.updateKey==0)
            this.bot.sendIRC().message(this.channel,"Clue: "+this.answer.getClue());
        
        this.counter = 1;
        
        try{
            Thread.sleep(this.time*1000);
        }
        catch(InterruptedException ex){
            
        }
        
        while (this.running&&this.counter<4){
            
            try {
                if (this.running && this.updateKey==0)
                    this.bot.sendIRC().message(this.channel,"Clue: "+this.answer.giveClue());
                
                if (this.running && this.updateKey!=0){
                    Utils.dispatchEvent(Global.bot, new MessageEvent(Global.bot,event.getChannel(),event.getChannel().getName(), bot.getUserBot(), event.getBot().getUserBot(),Integer.toString(updateKey), event.getTags()));
                }
                
                Thread.sleep(this.time*1000);
                this.counter++; // just to make sure the queue stops before giving out more hints than it should
                
            } catch (InterruptedException ex) {
                
            }
        }
        
        if (key != 0 && this.running){
            Utils.dispatchEvent(Global.bot, new MessageEvent(Global.bot,event.getChannel(),event.getChannel().getName(), bot.getUserBot(), event.getBot().getUserBot(),Integer.toString(key), event.getTags()));
        }
    }
}