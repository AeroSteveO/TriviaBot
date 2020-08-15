/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rapternet.irc.bots.triviabot;

/**
 *
 * @author thest
 */
public class Env {
    public static final String CONFIG_LOCATION = getVarOrDefault("BOT_CONFIG_FOLDER", "/config/");
    public static final String NICK = getVarOrDefault("BOT_NICK", "TriviaBot");
    public static final String PASSWORD = getVarOrDefault("BOT_PASSWORD", "password");
    public static final String OWNER_NICK = getVarOrDefault("BOT_OWNER_NICK", "Steve-O");
    public static final String IRC_PORT = getVarOrDefault("BOT_IRC_PORT", "6667");
    public static final String LOGIN = getVarOrDefault("BOT_LOGIN", "Derpy");
    public static final String IRC_ADDRESS = getVarOrDefault("BOT_IRC_ADDRESS", "irc.rapternet.us");
    public static final String[] CHANNEL_LIST = getVarOrDefault("BOT_CHANNEL_LIST", "#testing,#rapterverse").split(",");
    
    static String getVarOrDefault(String key, String def) {
        String javaProp = System.getProperty(key, def);
        return System.getenv().getOrDefault(key, javaProp);
    }
}
