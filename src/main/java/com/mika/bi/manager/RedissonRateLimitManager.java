package com.mika.bi.manager;

import com.mika.bi.common.ErrorCode;
import com.mika.bi.exception.ThrowUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RedissonRateLimitManager {

    @Resource
    private RedissonClient redissonClient;

    public void doRateLimit(String key){
        RRateLimiter limiter = redissonClient.getRateLimiter(key);
// Initialization required only once.
// 5 permits per 2 seconds
        limiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
        boolean canOp =  limiter.tryAcquire(1);
        ThrowUtils.throwIf(!canOp, ErrorCode.TOO_MANY_REQUSET);
    }
}
