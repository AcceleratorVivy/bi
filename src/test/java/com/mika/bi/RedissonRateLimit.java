package com.mika.bi;

import org.junit.jupiter.api.Test;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedissonRateLimit {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void testRedissonLimit() throws Exception{
// acquire 3 permits or block until they became available

    }


}
