/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Objects;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Steve-O
 *
 *
 */
public class Question {
    private String question=null;
    private String answer=null;
    private Thread t;
    QuestionUpdater runnable;
    
    public Question(){
        getNewQuestion();
    }
    public Question(MessageEvent event,Answer answer, Question question, int time ){
        getNewQuestion();
        QuestionUpdater runnable = new QuestionUpdater(event,answer, this, time);
        this.t = new Thread(runnable);
        runnable.giveT(t);
        t.start();
    }
    
    public void startQuestionUpdates(MessageEvent event,Answer answer, Question question, int time){
        this.runnable = new QuestionUpdater(event,answer, this, time);
        this.t = new Thread(runnable);
        this.runnable.giveT(t);
        t.start();
    }
    public void endQuestionUpdates() throws InterruptedException{
        this.runnable.end();
    }
    public void getNewQuestion(){
        String[] tmp = loadRandomQuestionFromFile().split("`");
        this.answer=tmp[1];
        this.question = tmp[0];
    }
    public String getAnswer(){
        return this.answer;
    }
    public String getQuestion(){
        return this.question;
    }
    
    private String loadRandomQuestionFromFile(){
        ArrayList<File> fileList = getQuestionFileList();
        File randomFile = fileList.get((int) (Math.random()*fileList.size()-1));
        ArrayList<String> questions = loadQuestionFile(randomFile);
        String randomQuestion = questions.get((int) (Math.random()*questions.size()-1));
        return randomQuestion;
    }
    private ArrayList<File> getQuestionFileList(){
        File folder = new File("questions/");
        File[] listOfFilesAndFolders = folder.listFiles();
        ArrayList<File> listOfFiles = new ArrayList<>();// = new File[];
        for (int i = 0; i < listOfFilesAndFolders.length; i++) {
            if (listOfFilesAndFolders[i].isFile()) {
                listOfFiles.add(listOfFilesAndFolders[i]);
            } else if (listOfFilesAndFolders[i].isDirectory()) {
                System.out.println("Directory " + listOfFilesAndFolders[i].getName());
            }
        }
        return listOfFiles;
    }
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
