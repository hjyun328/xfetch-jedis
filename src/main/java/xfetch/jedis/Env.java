package xfetch.jedis;

@SuppressWarnings("PointlessArithmeticExpression")
public class Env {

    public static final FetcherType FETCHER_TYPE = FetcherType.XFETCHER;

    public static final int THREAD_POOL_SIZE = 30;

    public static final double XFETCH_BETA = 1.0;
    public static final int XFETCH_RAND_PRECISION = 3;

    public static final int TTL_S = 20;
    public static final int RUNNING_TIME_MS = 5 * 60 * 1000;
    public static final int RECOMPUTE_DELTA_MS = 500;
    public static final int PRINT_STAT_DELAY_MS = 1000;
    public static final int REQUEST_DELAY_MS = 100;

    public static final String DATA_KEY = "key";
    public static final String DELTA_KEY = "delta";
    public static final String DATA = "hello";

}
