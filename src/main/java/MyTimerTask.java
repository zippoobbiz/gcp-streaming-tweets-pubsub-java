import java.util.Timer;

public class MyTimerTask {

    public static void main(String args[]) {
        TwittsPublisher myPublisher = new TwittsPublisher(args);
        //running timer task as daemon thread
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(myPublisher, 0, 60 * 1000);
        System.out.println("TimerTask started");
        //cancel after sometime
        try {
            Thread.sleep(3000 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.cancel();
        System.out.println("TimerTask cancelled");
    }

}