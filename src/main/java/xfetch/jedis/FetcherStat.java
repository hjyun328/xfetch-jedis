package xfetch.jedis;

import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FetcherStat implements Runnable {

    private boolean stop = false;

    private int displayStatDelay;

    private AtomicInteger missCount = new AtomicInteger(0);

    private AtomicInteger hitCount = new AtomicInteger(0);

    private AtomicInteger earlyCount = new AtomicInteger(0);

    private AtomicLong requestCount = new AtomicLong(0);

    public FetcherStat(int displayStatDelay) {
        this.displayStatDelay = displayStatDelay;
    }

    public void increaseMissCount() {
        missCount.incrementAndGet();
    }

    public void increaseHitCount() {
        hitCount.incrementAndGet();
    }

    public void increaseEarlyCount() {
        earlyCount.incrementAndGet();
    }

    public void increaseRequestCount() {
        requestCount.incrementAndGet();
    }

    public void stop() {
        stop = true;
    }

    @Override
    public void run() {
        stop = false;

        int count = 1;

        StopWatch stopWatch = StopWatch.createStarted();

        while (!stop) {
            if (stopWatch.getTime() >= displayStatDelay) {
                System.out.printf("[%6dms] miss=%d, early=%d, hit=%d, req=%d\n", displayStatDelay * count++, missCount.get(), earlyCount.get(), hitCount.get(), requestCount.get());
                stopWatch.stop();
                stopWatch.reset();
                stopWatch.start();
            }
        }
    }
}
