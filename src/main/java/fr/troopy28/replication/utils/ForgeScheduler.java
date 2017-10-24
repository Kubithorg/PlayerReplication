package fr.troopy28.replication.utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Creation: 24/10/2017.
 *
 * @author troopy28
 * @since 1.0.0
 */
public class ForgeScheduler {

    private ForgeScheduler() {
    }

    public static Timer runTaskLater(Runnable task, long delayMilliseconds) {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, delayMilliseconds);
        return t;
    }

    public static Timer runRepeatingTaskLater(Runnable task, long delayMilliseconds, long periodMilliseconds) {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, delayMilliseconds, periodMilliseconds);
        return t;
    }
}
