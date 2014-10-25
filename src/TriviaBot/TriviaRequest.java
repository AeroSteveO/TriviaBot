/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package TriviaBot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Stephen
 * 
 * 
 */
public class TriviaRequest extends ListenerAdapter {
    ArrayList<String> questionRequests = getDefinitions();
    ArrayList<String> words = getWordsFromDefs(questionRequests);
    String requestFileName = "questionRequests.txt";
    String requestLogName   = "requestLog.txt";
    
    @Override
    public void onMessage(MessageEvent event) throws FileNotFoundException, InterruptedException {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        if (message.startsWith(Global.commandPrefix)){
            String command = message.split(Global.commandPrefix)[1].toLowerCase();
            
            if (command.equalsIgnoreCase("list requests")){
                String wordList = "";
                for (int i=0;i<words.size();i++){
                    wordList = wordList + words.get(i)+", ";
                }
                event.getBot().sendIRC().message(event.getUser().getNick(),wordList);
            }
            
            // ADDING DEFINITIONS
            else if((command.startsWith("addques")||command.startsWith("addquestion"))&&command.split("`").length==2){
                String addition = message.split(" ",2)[1]+" @ "+getUID()+"@"+event.getUser().getNick();
                
                try{
                    File file =new File(requestFileName);
                    //if file doesnt exists, then create it
                    if(!file.exists()){
                        file.createNewFile();
                    }
                    //true = append file
                    FileWriter fileWritter = new FileWriter(file.getName(),true);
                    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                    bufferWritter.write("\n"+addition);
                    bufferWritter.close();
                    event.getBot().sendIRC().notice(event.getUser().getNick(),"Success: "+addition+" was added to "+ requestFileName);
                }catch(IOException e){
                    e.printStackTrace();
                    event.getBot().sendIRC().notice(event.getUser().getNick(),"SOMETHING BROKE: FILE NOT UPDATED");
                }
                questionRequests = getDefinitions();
                words = getWordsFromDefs(questionRequests);
            }
            else if((message.startsWith("addques")||message.startsWith("!addquestion"))&&!(message.split("`").length==2)){
                event.getBot().sendIRC().notice(event.getUser().getNick(),"Improperly formed defintion add command: !adddef word or phrase @ definition phrase");
            }
            
            // REMOVING DEFINITIONS
            if((command.startsWith("delques")||command.startsWith("deletequestion")||command.startsWith("deleteques")||command.startsWith("delquestion"))&&containsIgnoreCase(words,message.split(" ",2)[1])&&event.getUser().getNick().equalsIgnoreCase(Global.botOwner)){
                int index = indexOfIgnoreCase(words, message.split(" ",2)[1]);
                try{
                    File log = new File(requestLogName);
                    if(!log.exists()){
                        log.createNewFile();
                    }
                    FileWriter fileWritter = new FileWriter(log.getName(),true);
                    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                    bufferWritter.write("\n"+questionRequests.get(index));
                    bufferWritter.close();
                }catch (Exception e){
                    event.getBot().sendIRC().notice(event.getUser().getNick(),"SOMETHING BROKE: LOG NOT UPDATED");
                }
                
                questionRequests.remove(index);
                words.remove(index);
                File fnew=new File(requestFileName);
                try{
                    FileWriter f2 = new FileWriter(fnew, false);
                    for (int i=0;i<questionRequests.size()-1;i++)
                        f2.write(questionRequests.get(i)+"\n");
                    
                    f2.write(questionRequests.get(questionRequests.size()-1));
                    f2.close();
                    event.getBot().sendIRC().notice(event.getUser().getNick(),"Success: "+message.split(" ",2)[1]+" was removed from "+requestFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    event.getBot().sendIRC().notice(event.getUser().getNick(),"SOMETHING BROKE: DEF NOT DELETED");
                }
                
            }
            else if ((message.startsWith("!deldef")||message.startsWith("!deletedef"))&&event.getUser().getNick().equalsIgnoreCase(Global.botOwner)){
                event.getBot().sendIRC().notice(event.getUser().getNick(),"Definition not found");
            }
            else if (message.startsWith("!deldef")||message.startsWith("!deletedef")){
                event.getBot().sendIRC().notice(event.getUser().getNick(),"You do not have access to this function");
            }
            
            // Updating questionRequests already in the db
            if((message.startsWith("!updatedef")||message.startsWith("!updef"))&&message.split("@").length==2&&containsIgnoreCase(words,message.split(" ",2)[1].split("@")[0].trim())&&event.getUser().getNick().equalsIgnoreCase(Global.botOwner)){
                int index = indexOfIgnoreCase(words, message.split(" ",2)[1].split("@")[0].trim());
                try{
                    File log = new File(requestLogName);
                    if(!log.exists()){
                        log.createNewFile();
                    }
                    FileWriter fileWritter = new FileWriter(log.getName(),true);
                    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                    bufferWritter.write("\n"+questionRequests.get(index));
                    bufferWritter.close();
                }catch (Exception e){
                    event.getBot().sendIRC().notice(event.getUser().getNick(),"SOMETHING BROKE: LOG NOT UPDATED");
                }
                questionRequests.remove(index);
                words.remove(index);
                questionRequests.add(message.split(" ",2)[1].trim());
                words = getWordsFromDefs(questionRequests);
                File fnew=new File(requestFileName);
                try {
                    FileWriter f2 = new FileWriter(fnew, false);
                    for (int i=0;i<questionRequests.size()-1;i++)
                        f2.write(questionRequests.get(i)+"\n");
                    
                    f2.write(questionRequests.get(questionRequests.size()-1));
                    f2.close();
                    event.getBot().sendIRC().notice(event.getUser().getNick(),"Success: "+message.split(" ",2)[1].split("@")[0].trim()+" was updated in "+requestFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    event.getBot().sendIRC().notice(event.getUser().getNick(),"SOMETHING BROKE: FILE NOT UPDATED");
                }
            }
            else if ((message.startsWith("!updatedef")||message.startsWith("!updef"))&&event.getUser().getNick().equalsIgnoreCase(Global.botOwner)){
                event.getBot().sendIRC().notice(event.getUser().getNick(),"Improperly formed update command: !updef word phrase @ definition phrase");
            }
            else if (message.startsWith("!updatedef")||message.startsWith("!updef")){
                event.getBot().sendIRC().notice(event.getUser().getNick(),"You do not have access to this function");
            }
        }
    }
    private ArrayList<String> getDefinitions() {
        try{
            Scanner wordfile = new Scanner(new File(requestFileName));
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
    private ArrayList<String> getWordsFromDefs(ArrayList<String> definitions){
        ArrayList<String> words = new ArrayList<String>();
        for (int i=0;i<definitions.size();i++){
            words.add(definitions.get(i).split("@")[0].trim());
        }
        return words;
    }
    private boolean containsIgnoreCase(ArrayList<String> o,String thing) {
        for (String s : o) {
            if (thing.equalsIgnoreCase(s)) return true;
        }
        return false;
    }
    private int indexOfIgnoreCase(ArrayList<String> o,String thing) {
        for (int i=0;i<o.size();i++) {
            if (thing.equalsIgnoreCase(o.get(i))) return i;
        }
        return -1;
    }

    private String getUID() {
        if (questionRequests.isEmpty()){
            return Integer.toString(1);
        }
        else {
            int UID = 0;
            for (int i=0;i<questionRequests.size();i++){
                int tempUID = Integer.parseInt(questionRequests.get(i).split("@")[1]);
                if (tempUID>UID){
                    UID = tempUID;
                }
            }
            UID++;
            return Integer.toString(UID);
        }
    }
}
