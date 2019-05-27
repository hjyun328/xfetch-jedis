package xfetch.jedis;

import org.apache.commons.lang3.time.StopWatch;

public class Recomputer {

    private long delta;

    public Recomputer(long delta) {
        this.delta = delta;
    }

    public long recompute() {
        StopWatch stopWatch = StopWatch.createStarted();
        while (true) {
            if (stopWatch.getTime() >= delta) {
                stopWatch.stop();
                return delta;
            }
        }
    }

}
