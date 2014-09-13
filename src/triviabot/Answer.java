package triviabot;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Steve-O
 */
public class Answer {
    String answer=null;
    String maskedAnswer=null;
    
    
    public Answer(String inputAnswer){
        this.answer=inputAnswer;
        this.maskedAnswer=mask(answer);
    }
    private static String mask(String input){
        String blanks = new String();
        for (int i = 0; i<input.length(); i++){
            if (input.charAt(i)==' '){
                blanks = blanks+" ";
            }
            else if(Character.isDigit(input.charAt(i))||Character.isLetter(input.charAt(i))){
                blanks = blanks + "*";
            }
            else {
                blanks = blanks + input.charAt(i);
            }
        }
        return(blanks);
    }
    private String giveClue(){
        
        return "";
    }
    public String getCurrentClue(){
        return this.maskedAnswer;
    }
    public void setAnswer(String newAnswer){
        this.answer=newAnswer;
    }
    public String reveal(){
        return(this.answer);
    }
}
