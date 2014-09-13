/**
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package triviabot;

/**
 *
 * @author Steve-O
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
        int charLocation = (int) (Math.random()*this.answer.length()-1);
        
        boolean charChange = false;
        while (!charChange){
            try{
//                if (!Character.isDigit(maskedAnswer.charAt(charLocation))&&!Character.isLetter(maskedAnswer.charAt(charLocation))){
//                    
//                    
//                }
                if (this.maskedAnswer.charAt(charLocation)=='*'){
                    if (charLocation<answer.length()-2)
                        this.maskedAnswer=this.maskedAnswer.substring(0,charLocation)+this.answer.charAt(charLocation)+this.maskedAnswer.substring(charLocation+1);
                    else
                        this.maskedAnswer=this.maskedAnswer.substring(0,charLocation)+this.answer.charAt(charLocation);
                    charChange = true;
                }
            }
            catch(Exception ex){
                ex.printStackTrace();
                System.out.println(ex.getMessage());
            }
            charLocation = (int) (Math.random()*this.answer.length()-1);
        }
        return this.maskedAnswer;
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
