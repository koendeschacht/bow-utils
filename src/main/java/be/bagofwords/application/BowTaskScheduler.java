package be.bagofwords.application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Koen Deschacht (koendeschacht@gmail.com) on 14/11/14.
 */

public class BowTaskScheduler implements CloseableComponent {

    private ScheduledExecutorService scheduledExecutorService;

    /**
     * @param task
     * @param period In milliseconds
     */

    public synchronized void schedulePeriodicTask(Runnable task, long period) {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(5);
        }
        scheduledExecutorService.scheduleAtFixedRate(task, 0, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public void terminate() {
        scheduledExecutorService.shutdown();
    }
}
