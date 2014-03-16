/**
 * 
 */
package cz.mtheory.example.concurent;


/**
 * M-theory project
 * 
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class Test {
    static String message;

    private static class CorrectorThread extends Thread {

        public void run() {
            try {
                sleep(1000); 
            } catch (InterruptedException e) {}
            //Key statement 1:
            message = "Mares do eat oats."; 
        }
    }

    public static void main(String args[]) throws InterruptedException {
        (new CorrectorThread()).start();
        message = "Mares do not eat oats.";
        Thread.sleep(510);
        //Key statement 2:
        System.out.println(message);
    }
}
