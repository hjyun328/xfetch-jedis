package xfetch.jedis;

import redis.clients.jedis.JedisPool;

public abstract class FetcherBase {

    protected JedisPool jedisPool;

    protected Recomputer recomputer;

    public FetcherBase(JedisPool jedisPool, Recomputer recomputer) {
        this.jedisPool = jedisPool;
        this.recomputer = recomputer;
    }

    public abstract FetchResult fetch();

}
