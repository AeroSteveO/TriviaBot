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
    public static final String CONFIG_LOCATION = System.getenv().getOrDefault("BOT_CONFIG_FOLDER", "/config/");
    public static final String NICK = System.getenv().getOrDefault("BOT_NICK", "TriviaBot");
    public static final String PASSWORD = System.getenv().getOrDefault("BOT_PASSWORD", "password");
    public static final String OWNER_NICK = System.getenv().getOrDefault("BOT_OWNER_NICK", "Steve-O");
    public static final String IRC_PORT = System.getenv().getOrDefault("BOT_IRC_PORT", "6667");
    public static final String LOGIN = System.getenv().getOrDefault("BOT_LOGIN", "Derpy");
    public static final String IRC_ADDRESS = System.getenv().getOrDefault("BOT_IRC_ADDRESS", "irc.rapternet.us");
    public static final String[] CHANNEL_LIST = System.getenv().getOrDefault("BOT_CHANNEL_LIST", "#testing,#rapterverse").split(",");
}
