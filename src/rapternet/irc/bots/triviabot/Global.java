/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package rapternet.irc.bots.triviabot;

import java.util.ArrayList;
import org.pircbotx.PircBotX;

/**
 *
 * @author Steve-O
 * botOwner      - User with all powers over the bot, and ability to shut the bot down
 * mainNick      - The intended nickname for the bot, not necessarily the current nickname
 * nickPass      - The nickServ password for the bot
 * botAdmins     - Administrators of the bot, ability to use some commands
 * commandPrefix - The character that signals the bot that a command is being sent
 * reconnect     - Boolean to activate the aggressive server reconnect loop
 * bot           - Current PircBotX bot object
 * activeGames   - Currently active channels with trivia running in them
 *
 */
public class Global {
    public static String botOwner; 
    public static ArrayList<String> botAdmins;
    public static String mainNick;     
    public static String nickPass; 
    public static boolean reconnect = true;
    public static PircBotX bot;
    public static String commandPrefix;
//    public static TriviaArray activeGames = new TriviaArray();
    
    private static ArrayList<String> getBotAdmins() {
        ArrayList<String> admins = new ArrayList<>();
        admins.add(botOwner);
        admins.add("Jnick");
        admins.add("theDoctor");
        admins.add("burg");
        return(admins);
    }
}