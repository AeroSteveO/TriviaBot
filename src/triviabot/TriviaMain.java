 /**
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */

package triviabot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Steve-O
 *
 *
 */
public class TriviaMain extends ListenerAdapter{
    @Override
    public void onMessage(MessageEvent event) throws InterruptedException, Exception {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        if (message.startsWith(Global.commandPrefix)){
            String command = message.split(Global.commandPrefix)[1].toLowerCase();
            if (command.equalsIgnoreCase("getFileList")){
                ArrayList<File> files = getQuestionFileList();
                for (int i=0;i<files.size();i++)
                    event.getBot().sendIRC().message(event.getChannel().getName(), files.get(i).toString());
            }
            if (command.equalsIgnoreCase("getQuestion")){
                event.respond(loadRandomQuestionFromFile());
            }
        }
        
        
        
        
//<theDoctor> Steve-O!!! make a kick function for the channel
//<theDoctor> !kick burg
//<theDoctor> then it starts a trivia that only that person can answer
//<theDoctor> if they get it right quickly
//<theDoctor> like reduced time
//<theDoctor> then it will kick the person
//<Steve-O> if you don't get the question, you get kicked, if you get the question, they get kicked
//<theDoctor> trivia WARS
//<theDoctor> hell, make the stakes higher to use it
//<theDoctor> 1min ban
//<theDoctor> for the kicker
//<theDoctor> no ban on kickee
        
        // get 3 votes to start trivia without an op, otherwise ops can start, ops or norm can stop
        
        // local and global score, one for current trivia session, one for overall points scored in trivia
        
    }
    
    // IMPLEMENTED IN QUESTION OBJECT
    private String loadRandomQuestionFromFile(){
//        ArrayList<String> questions = new ArrayList<>();
        ArrayList<File> fileList = getQuestionFileList();
        File randomFile = fileList.get((int) (Math.random()*fileList.size()-1));
        ArrayList<String> questions = loadQuestionFile(randomFile);
        String randomQuestion = questions.get((int) (Math.random()*questions.size()-1));
        return randomQuestion;
    }
    // IMPLEMENTED IN QUESTION OBJECT
    private ArrayList<File> getQuestionFileList(){
//        ArrayList<String> questionFiles = new ArrayList<>();
        File folder = new File("questions/");
        File[] listOfFilesAndFolders = folder.listFiles();
        ArrayList<File> listOfFiles = new ArrayList<>();// = new File[];
        for (int i = 0; i < listOfFilesAndFolders.length; i++) {
            if (listOfFilesAndFolders[i].isFile()) {
//                System.out.println("File " + listOfFilesAndFolders[i].getName());
                listOfFiles.add(listOfFilesAndFolders[i]);
            } else if (listOfFilesAndFolders[i].isDirectory()) {
                System.out.println("Directory " + listOfFilesAndFolders[i].getName());
            }
        }
        return listOfFiles;
    }
    // IMPLEMENTED IN QUESTION OBJECT
    private ArrayList<String> loadQuestionFile(File file) {
        try{
            Scanner wordfile = new Scanner(file);
            ArrayList<String> wordls = new ArrayList<String>();
            while (wordfile.hasNext()){
                wordls.add(wordfile.nextLine());
            }
            wordfile.close();
            return (wordls);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
