/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Objects;

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
 *     *giveT - Gives a thread to the object
 *     *end   - Ends the automatic updating, isn't an instant stop, but it won't
 *              send any more updates to the channel after this is used
 *     *run   - The heart of the auto-updating, a looped thread
 * 
 * 
 */
public class QuestionUpdater implements Runnable{
    private PircBotX bot;
    private Thread t;
    private boolean running = false;
    private String channel = null;
    private Answer answer;
    private Question question;
    private int time;
    
    QuestionUpdater(MessageEvent event, Answer answer, Question question, int time){
        this.bot = event.getBot();
        this.channel = event.getChannel().getName();
        this.answer = answer;
        this.question = question;
        this.running = true;
        this.time = time; // Seconds
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
//        this.bot.sendIRC().message(this.channel,"Question:");
//        this.bot.sendIRC().message(this.channel, this.question.getQuestion());
        this.bot.sendIRC().message(this.channel,"Clue: "+this.answer.getClue());
        int counter = 1;
        try{
            this.t.sleep(this.time*1000);
        }
        catch(Exception ex){
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        while (this.running&&counter<4){
            try {
//                this.bot.sendIRC().message(this.channel, this.question.getQuestion());
                this.bot.sendIRC().message(this.channel,"Clue: "+this.answer.giveClue());
                this.t.sleep(this.time*1000); 
                counter++; // just to make sure the queue stops before giving out more hints than it should
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                System.out.println(ex.getMessage());
            }
        }
    }
}