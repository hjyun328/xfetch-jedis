package xfetch.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SuppressWarnings("unchecked")
public class Main {

    public static void main(String[] args) {

        // initialize jedis
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(Env.THREAD_POOL_SIZE);
        jedisPoolConfig.setMaxTotal(Env.THREAD_POOL_SIZE);
        JedisPool pool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 4000);
        Jedis jedis = pool.getResource();
        jedis.del(Env.DATA_KEY);
        jedis.del(Env.DELTA_KEY);
        jedis.close();

        // initialize fetcher base
        FetcherBase fetcherBase;
        if (Env.FETCHER_TYPE == FetcherType.FETCHER) {
            fetcherBase = new Fetcher(pool, new Recomputer(Env.RECOMPUTE_DELTA_MS));
        } else {
            fetcherBase = new XFetcher(pool, new Recomputer(Env.RECOMPUTE_DELTA_MS), Env.XFETCH_BETA, Env.XFETCH_RAND_PRECISION);
        }

        // start fetcher executor
        FetcherExecutor executor = new FetcherExecutor(Env.THREAD_POOL_SIZE, Env.REQUEST_DELAY_MS, fetcherBase, new FetcherStat(Env.PRINT_STAT_DELAY_MS));
        executor.execute(Env.RUNNING_TIME_MS);

    }

}




