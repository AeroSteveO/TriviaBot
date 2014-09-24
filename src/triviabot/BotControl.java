/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package triviabot;

import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

/**
 *
 * @author Steve-O
 * Part code from RoyalBot -- http://www.royalcraft.org/royaldev/royalbot
 * Rest of the code is from Wheatley, another bot coded by me
 *
 * Activate Commands with:
 *      TriviaBot, join #[channel]
 *          Makes the bot join the given channel
 *      TriviaBot, part #[channel]
 *          Makes the bot part the given channel
 *      TriviaBot, leave
 *          Makes the bot part the channel its currently in
 *      TriviaBot, fix yourself
 *          Ghosts/Recovers nick, rejoins channels it was disconnected from
 *      TriviaBot, whats your IP
 *          Gives the current external IP address of the bot
 *      TriviaBot, shutdown
 *          Shuts down the bot
 *
 */
public class BotControl extends ListenerAdapter{
    
    @Override
    public void onMessage(MessageEvent event) throws InterruptedException, Exception {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        
        if (message.equalsIgnoreCase(Global.mainNick+", fix yourself")&&(event.getUser().getNick().equalsIgnoreCase(Global.botOwner)||event.getUser().getNick().equalsIgnoreCase("theDoctor"))){
            
            event.getBot().sendIRC().message("NickServ", "ghost " + Global.mainNick + " " + Global.nickPass);  //ghost is a depricated command, if it doesn't work, the next command should work
            event.getBot().sendIRC().message("NickServ", "recover " + Global.mainNick + " " + Global.nickPass);//sends both commands, NS can yell about one and do the other
            
            Thread.sleep(5000); // wait between killing the ghost to changing nick and registering
            event.getBot().sendIRC().changeNick(Global.mainNick);
            event.getBot().sendIRC().message("NickServ", "identify " + Global.nickPass);
//            Global.channels.removeDupes();
//            for (int i=0;i<Global.channels.size();i++){
//                event.getBot().sendIRC().joinChannel(Global.channels.get(i).toString());
//            }
        }
        
        if (message.equalsIgnoreCase(Global.mainNick+", please shutdown")||message.equalsIgnoreCase("!shutdown")||message.equalsIgnoreCase(Global.mainNick+", shutdown")) {
            if (event.getUser().getNick().equals(Global.botOwner)){
                Global.reconnect = false;
                event.getBot().sendIRC().message(event.getChannel().getName(), "I am the weakest link, goodbye");
                event.getBot().sendIRC().quitServer("This is triviabot, signing off.");
                System.exit(0);
            }
            else
                event.getChannel().send().kick(event.getUser(), "You are out of lifelines"); // kick people for trying to kill the bot
        }
        
        // command the bot to join channels
        if ((message.toLowerCase().startsWith(Global.mainNick.toLowerCase()+", join ")||message.toLowerCase().startsWith(Global.mainNick.toLowerCase()+", please join "))&&(event.getUser().getNick().equals(Global.botOwner)||event.getUser().getNick().equalsIgnoreCase("theDoctor"))){ //message.toLowerCase().startsWith("!join ")
            String[] chan = message.split("#");
            if (message.toLowerCase().contains("#")){
                event.getBot().sendIRC().message(event.getChannel().getName(),"Joining #" + chan[1]);
                event.getBot().sendIRC().joinChannel("#" + chan[1]);
//                Global.channels.add(new ChannelStore("#"+chan[1]));
            }
            else
                event.getBot().sendIRC().message(event.getChannel().getName(),chan[chan.length-1] + " is not a channel");
        }
        
        // command the bot to part a different channel from where you are
        if ((message.toLowerCase().startsWith(Global.mainNick.toLowerCase()+", leave")||message.toLowerCase().startsWith(Global.mainNick.toLowerCase()+", please leave"))){//message.toLowerCase().startsWith("!part")
            if (message.toLowerCase().contains("#")&&(event.getUser().getNick().equals(Global.botOwner)||event.getUser().getNick().equalsIgnoreCase("theDoctor"))) {
                String[] chan = message.split("#");
                Channel c = event.getBot().getUserChannelDao().getChannel("#"+chan[1]);
                if (!event.getBot().getUserBot().getChannels().contains(c)) {
                    event.respond("Not in that channel!");
                }
                else {
                    c.send().part();
                    event.respond("Parted from " + chan[1] + ".");
//                    Global.channels.remove(Global.channels.getChanIdx("#"+chan[1]));
                }
            } // command the bot to part the current channel that the command was sent from
            else if ((event.getChannel().isOwner(event.getUser())||event.getUser().getNick().equals(Global.botOwner))&&(message.endsWith("leave"))){//||message.equalsIgnoreCase("!part"))){
                
                event.getChannel().send().part("Goodbye");
//                Global.channels.remove(Global.channels.getChanIdx(event.getChannel().getName().toString()));
            }
        }
    }
    @Override
    public void onPrivateMessage(PrivateMessageEvent event) throws InterruptedException{
        String message = Colors.removeFormattingAndColors(event.getMessage());
        if (message.equalsIgnoreCase(Global.mainNick+", fix yourself")&&(event.getUser().getNick().equalsIgnoreCase(Global.botOwner)||event.getUser().getNick().equalsIgnoreCase("theDoctor"))){
            
            event.getBot().sendIRC().message("NickServ", "ghost " + Global.mainNick + " " + Global.nickPass);  //ghost is a depricated command, if it doesn't work, the next command should work
            event.getBot().sendIRC().message("NickServ", "recover " + Global.mainNick + " " + Global.nickPass);//sends both commands, NS can yell about one and do the other
            
            Thread.sleep(5000); // wait between killing the ghost to changing nick and registering
            event.getBot().sendIRC().changeNick(Global.mainNick);
            event.getBot().sendIRC().message("NickServ", "identify " + Global.nickPass);
//            for (int i=0;i<Global.channels.size();i++){
//                event.getBot().sendIRC().joinChannel(Global.channels.get(i).toString());
//            }
        }
    }
}