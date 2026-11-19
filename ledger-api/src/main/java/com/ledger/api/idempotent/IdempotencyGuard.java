package com.ledger.api.idempotent;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提交幂等：同一份提交带着同一个 X-Idempotency-Key 只处理一次。
 * <p>
 * 为什么不用数据库：这是“防止手抖/重试”的短时去重，10 分钟就够，丢了也没关系，
 * 用内存表最省事；应用是单实例部署，重启后清空不影响业务正确性。
 */
@Component
public class IdempotencyGuard {

    /** key 保留多久（毫秒）：10 分钟内同一个 key 只认一次 */
    private static final long TTL_MILLIS = 10 * 60 * 1000L;

    /** 超过这个数量就顺手清理过期 key，避免长跑后无限增长 */
    private static final int CLEAN_THRESHOLD = 2000;

    private final Map<String, Long> keys = new ConcurrentHashMap<>();

    /**
     * 抢占一个 key。
     *
     * @return true 表示第一次见到（可以继续处理）；false 表示重复提交
     */
    public boolean tryAcquire(String key) {
        if (key == null || key.isBlank()) {
            return true;
        }
        cleanIfNeeded();
        long now = System.currentTimeMillis();
        Long previous = keys.putIfAbsent(key, now);
        if (previous == null) {
            return true;
        }
        if (now - previous > TTL_MILLIS) {
            // 过期的旧 key：替换成新时间，允许再次处理
            keys.put(key, now);
            return true;
        }
        return false;
    }

    /**
     * 处理失败时释放 key，让用户能原样重试。
     */
    public void release(String key) {
        if (key != null) {
            keys.remove(key);
        }
    }

    private void cleanIfNeeded() {
        if (keys.size() < CLEAN_THRESHOLD) {
            return;
        }
        long now = System.currentTimeMillis();
        keys.entrySet().removeIf(entry -> now - entry.getValue() > TTL_MILLIS);
    }
}
