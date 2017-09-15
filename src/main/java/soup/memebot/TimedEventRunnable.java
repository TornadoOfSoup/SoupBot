package soup.memebot;

import static soup.memebot.Utils.getTimestampFull;

/**
 * Created by RPGenius on 9/12/2017.
 */
public class TimedEventRunnable implements Runnable {

    volatile boolean timedBool;
    String threadName;
    int timerLength;
    private Thread thread;

    public TimedEventRunnable(String threadName, int timerLengthInSeconds) {
        this.threadName = threadName;
        this.timerLength = timerLengthInSeconds;
        System.out.println(getTimestampFull() + " Creating " + this.getClass().getName() + " thread called " + threadName);
    }

    @Override
    public void run() {
        this.timedBool = true;
        try {
            while (true) {
                System.out.println(getTimestampFull() + " " + threadName + " timer has reset!");
                Thread.sleep(timerLength * 1000);
                this.timedBool = true;
            }
        } catch (InterruptedException e) {
            System.out.println(getTimestampFull() + " " + this.threadName + " was interrupted!");
        }
    }

    public void start () {
        System.out.println(getTimestampFull() + " Starting " +  threadName + "!");
        if (thread == null) {
            thread = new Thread (this, threadName);
            thread.start();
        }
    }

    public void setTimedBool(boolean timedBool) {
        this.timedBool = timedBool;
    }

    public boolean getTimedBool() {
        return this.timedBool;
    }
}
