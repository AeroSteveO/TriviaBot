/**
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Objects;

/**
 *
 * @author Steve-O
 *   This class implements storage for an answer you want to conceal
 *   and give clues 1 letter at a time.
 *
 *   Methods:
 *
 *   giveClue(): returns the masked string after revealing a letter and saving the mask.
 *   getClue(): returns the masked string.
 *   setAnswer(String): makes this object reusable, sets a new answer and clue mask.
 *   reveal(): returns the answer string.
 *   mask(String): returns the masked answer
 *
 */
public class Answer {
    String answer=null;
    String maskedAnswer=null;
    
    public Answer(String inputAnswer){
        this.answer=inputAnswer;
        this.maskedAnswer=mask(this.answer);
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
    public String giveClue(){
//        int charLocation = (int) (Math.random()*this.answer.length()-1);
        boolean charChange = false;
        int numCharsLeft = 0;
        for (int i=0;i<this.maskedAnswer.length();i++){
            if (this.maskedAnswer.charAt(i)=='*'){
                numCharsLeft++;
            }
        }
        if (numCharsLeft>1){
//            int charLocation = (int) (Math.random()*numCharsLeft-1);
            int i=(int) (Math.random()*numCharsLeft-1)-1;
            if (i<0)
                i=0;
            while (i<this.maskedAnswer.length()&&!charChange){
                if (this.maskedAnswer.charAt(i)=='*'){
                    if (i<=answer.length()-2)
                        this.maskedAnswer=this.maskedAnswer.substring(0,i)+this.answer.charAt(i)+this.maskedAnswer.substring(i+1);
                    else
                        this.maskedAnswer=this.maskedAnswer.substring(0,i)+this.answer.charAt(i);
                    charChange = true;
                }
                i++;
            }
        }
        else{
            for (int i=0;i<this.maskedAnswer.length();i++){
                if (this.maskedAnswer.charAt(i)=='*'){
                    if (i<=answer.length()-2)
                        this.maskedAnswer=this.maskedAnswer.substring(0,i)+this.answer.charAt(i)+this.maskedAnswer.substring(i+1);
                    else
                        this.maskedAnswer=this.maskedAnswer.substring(0,i)+this.answer.charAt(i);
                    charChange = true;
                    break;
                }
            }
        }
//        while (!charChange){
//            try{
////                if (!Character.isDigit(maskedAnswer.charAt(charLocation))&&!Character.isLetter(maskedAnswer.charAt(charLocation))){
////                }
//                if (this.maskedAnswer.charAt(charLocation)=='*'){
//                    if (charLocation<=answer.length()-2)
//                        this.maskedAnswer=this.maskedAnswer.substring(0,charLocation)+this.answer.charAt(charLocation)+this.maskedAnswer.substring(charLocation+1);
//                    else
//                        this.maskedAnswer=this.maskedAnswer.substring(0,charLocation)+this.answer.charAt(charLocation);
//                    charChange = true;
//                }
//            }
//            catch(Exception ex){
//                ex.printStackTrace();
//                System.out.println(ex.getMessage());
//            }
//            
//            charLocation = (int) (Math.random()*this.answer.length()-1);
//        }
        return this.maskedAnswer;
    }
    public String getClue(){
        return this.maskedAnswer;
    }
    public void setAnswer(String newAnswer){
        this.answer=newAnswer;
        this.maskedAnswer=mask(this.answer);
    }
    public String reveal(){
        return(this.answer);
    }
}
