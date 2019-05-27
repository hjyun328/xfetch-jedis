package xfetch.jedis;

import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FetcherExecutor {

    private int threadPoolSize;

    private int requestDelayMs;

    private ExecutorService fetcherExecutor;

    private ExecutorService displayStatExecutor;

    private ExecutorService runningTimeExecutor;

    private FetcherBase fetcherBase;

    private FetcherStat fetcherStat;

    public FetcherExecutor(int threadPoolSize, int requestDelayMs, FetcherBase fetcherBase, FetcherStat fetcherStat) {
        this.threadPoolSize = threadPoolSize;
        this.requestDelayMs = requestDelayMs;
        this.fetcherBase = fetcherBase;
        this.fetcherStat = fetcherStat;
    }

    public void execute(int runningTime) {
        executeFetcher();
        executeDisplayStat();
        executeRunningTime(runningTime);
    }

    private void executeFetcher() {
        if (fetcherExecutor != null) {
            return;
        }

        fetcherExecutor = Executors.newFixedThreadPool(threadPoolSize);

        for (int i = 0; i < threadPoolSize; i++) {
            fetcherExecutor.execute(() -> {
                StopWatch stopWatch = StopWatch.createStarted();

                while (!fetcherExecutor.isShutdown()) {
                    if (stopWatch.getTime() < requestDelayMs) {
                        continue;
                    }

                    FetchResult result = fetcherBase.fetch();

                    fetcherStat.increaseRequestCount();

                    switch (result) {
                        case MISS:
                            fetcherStat.increaseMissCount();
                            break;
                        case EARLY:
                            fetcherStat.increaseEarlyCount();
                            break;
                        case HIT:
                            fetcherStat.increaseHitCount();
                            break;
                    }

                    stopWatch.stop();
                    stopWatch.reset();
                    stopWatch.start();
                }
            });
        }
    }

    public void executeDisplayStat() {
        displayStatExecutor = Executors.newSingleThreadExecutor();
        displayStatExecutor.execute(fetcherStat);
    }

    public void executeRunningTime(int runningTime) {
        runningTimeExecutor = Executors.newSingleThreadExecutor();
        runningTimeExecutor.execute(() -> {
            StopWatch stopWatch = StopWatch.createStarted();
            while (!runningTimeExecutor.isShutdown()) {
                if (stopWatch.getTime() >= runningTime) {
                    fetcherExecutor.shutdown();
                    fetcherStat.stop();
                    runningTimeExecutor.shutdown();
                }
            }
        });
    }

}
