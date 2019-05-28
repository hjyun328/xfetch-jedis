package xfetch.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.List;

@SuppressWarnings("unchecked")
public class XFetcher extends FetcherBase {

    private static final String SET_SCRIPT = "redis.call('mset', KEYS[1], ARGV[1], KEYS[2], ARGV[2]);" +
                                             "redis.call('expire', KEYS[1], ARGV[3]);" +
                                             "redis.call('expire', KEYS[2], ARGV[3])";
    private static final String GET_SCRIPT = "return {redis.call('mget', KEYS[1], KEYS[2])," +
                                             "redis.call('ttl', KEYS[1])}";

    private double xFetchBeta;

    private int xFetchRandPrecision;

    public XFetcher(JedisPool jedisPool, Recomputer recomputer, double xFetchBeta, int xFetchRandPrecision) {
        super(jedisPool, recomputer);

        this.xFetchBeta = xFetchBeta;
        this.xFetchRandPrecision = xFetchRandPrecision;
    }

    @Override
    public FetchResult fetch() {
        Jedis jedis = jedisPool.getResource();

        FetchResult fetchResult;

        List<Object> ret = (List<Object>) jedis.eval(GET_SCRIPT, 2, Env.DATA_KEY, Env.DELTA_KEY);
        String data = ((List<String>) ret.get(0)).get(0);
        String deltaStr = ((List<String>) ret.get(0)).get(1);

        long delta = 0;
        double rand = 0;
        long expiry = 0;
        double exponential = 0;
        double gap = 0;


        if (data == null || deltaStr == null) {
            fetchResult = FetchResult.MISS;
        } else {
            delta = Long.valueOf(deltaStr);    // milliseconds
            rand = new BigDecimal(Math.random()).setScale(xFetchRandPrecision, BigDecimal.ROUND_CEILING).doubleValue();
            expiry = (long) ret.get(1) * 1000; // milliseconds
            exponential = Math.log(rand);
            gap = -1 * delta * xFetchBeta * exponential;

            if (gap >= expiry) {
                fetchResult = FetchResult.EARLY;
            } else {
                fetchResult = FetchResult.HIT;
                jedis.close();
                return fetchResult;
            }
        }

        deltaStr = String.valueOf(recomputer.recompute());

        if (fetchResult == FetchResult.EARLY) {
            System.out.printf("[%d] Early recompute! (Rand=%f, Exp=%f, Delta=%dms, Gap=%fms, Expiry=%dms)\n",
                Thread.currentThread().getId(), rand, Math.abs(exponential), delta, gap, expiry);
        }

        jedis.eval(SET_SCRIPT, 2, Env.DATA_KEY, Env.DELTA_KEY, Env.DATA, deltaStr, String.valueOf(Env.TTL_S));
        jedis.close();

        return fetchResult;
    }

}
