/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Objects;

import TriviaBot.Global;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Steve-O
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
    
    QuestionUpdater(MessageEvent event, Answer answer, int time){
        this.bot = event.getBot();
        this.channel = event.getChannel().getName();
        this.answer = answer;
        this.running = true;
        this.time = time; // Seconds
        this.event = event;
    }
    
    QuestionUpdater(MessageEvent event, Answer answer, int time, int key){
        this.bot = event.getBot();
        this.channel = event.getChannel().getName();
        this.answer = answer;
        this.running = true;
        this.time = time; // Seconds
        this.key = key;
        this.event = event;
    }
    
    QuestionUpdater(MessageEvent event, int time, int endKey, int updateKey){
        this.bot = event.getBot();
        this.channel = event.getChannel().getName();
        this.running = true;
        this.time = time; // Seconds
        this.key = endKey;
        this.event = event;
        this.updateKey = updateKey;
    }
    
    public int getCount(){
        return this.counter;
    }
    
    public void giveT(Thread t) {
        this.t = t;
    }
    
    public void end() throws InterruptedException{
        this.running = false;
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
        catch(Exception ex){
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        
        while (this.running&&this.counter<4){
            
            try {
                if (this.running && this.updateKey==0)
                    this.bot.sendIRC().message(this.channel,"Clue: "+this.answer.giveClue());
                
                if (this.running && this.updateKey!=0){
                    bot.getConfiguration().getListenerManager().dispatchEvent(new MessageEvent(Global.bot,event.getChannel(),event.getBot().getUserBot(),Integer.toString(updateKey)));
                }
                
                Thread.sleep(this.time*1000);
                this.counter++; // just to make sure the queue stops before giving out more hints than it should
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println(ex.getMessage());
            }
        }
        
        if (key != 0 && this.running){
            bot.getConfiguration().getListenerManager().dispatchEvent(new MessageEvent(Global.bot,event.getChannel(),event.getBot().getUserBot(),Integer.toString(key)));
        }
    }
}