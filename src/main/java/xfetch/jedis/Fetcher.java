package xfetch.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

@SuppressWarnings("unchecked")
public class Fetcher extends FetcherBase {

    public Fetcher(JedisPool jedisPool, Recomputer recomputer) {
        super(jedisPool, recomputer);
    }

    @Override
    public FetchResult fetch() {
        Jedis jedis = jedisPool.getResource();

        FetchResult fetchResult;

        String data = jedis.get(Env.DATA_KEY);

        if (data != null) {
            fetchResult = FetchResult.HIT;
        } else {
            recomputer.recompute();
            jedis.set(Env.DATA_KEY, Env.DATA, SetParams.setParams().ex(Env.TTL_S));
            fetchResult = FetchResult.MISS;
        }

        jedis.close();

        return fetchResult;
    }

}
