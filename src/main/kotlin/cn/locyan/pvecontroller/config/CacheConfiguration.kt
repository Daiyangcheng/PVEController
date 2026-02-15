package cn.locyan.pvecontroller.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

/**
 * Redis Caching Configuration
 * 
 * Caching Strategy:
 * - DataCenter (1 hour TTL): Critical for every API call, rarely changes
 * - Node status (5 minutes TTL): Synced periodically, used in Server creation
 * - Template metadata (24 hours TTL): VM templates, rarely change
 * - IPv4 pool (30 minutes TTL): Available IPs for allocation
 * - IPv6 ranges (30 minutes TTL): IPv6 block status
 * 
 * Pattern: @Cacheable(value="cacheName", key="#dcId")
 *          @CacheEvict(value="cacheName", key="#dcId")
 */
@Configuration
@EnableCaching
class CacheConfiguration {

    /**
     * Redis Cache Manager with datacenter-scoped TTL configuration
     */
    @Bean
    @ConditionalOnBean(RedisConnectionFactory::class)
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))  // Default 30 min

        val cacheConfigs = mapOf(
            // Core infrastructure (1 hour TTL)
            "dataCenters" to defaultConfig.entryTtl(Duration.ofHours(1)),
            
            // Node and cluster (5 minutes TTL - refreshed frequently)
            "nodes" to defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "nodeGroups" to defaultConfig.entryTtl(Duration.ofMinutes(10)),
            
            // Template metadata (24 hours TTL - rarely changes)
            "templates" to defaultConfig.entryTtl(Duration.ofHours(24)),
            "templateGroups" to defaultConfig.entryTtl(Duration.ofHours(24)),
            
            // IP address management (30 minutes TTL - moderate volatility)
            "ipv4Pools" to defaultConfig.entryTtl(Duration.ofMinutes(30)),
            "ipv4s" to defaultConfig.entryTtl(Duration.ofMinutes(5)),  // Single IP lookup (frequent)
            "ipv6Ranges" to defaultConfig.entryTtl(Duration.ofMinutes(30)),
            "ipv6Allocations" to defaultConfig.entryTtl(Duration.ofMinutes(10)),
            
            // Server metadata (5 minutes TTL)
            "servers" to defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "serverGroups" to defaultConfig.entryTtl(Duration.ofMinutes(10)),
            
            // Storage and resource management (10 minutes TTL)
            "storage" to defaultConfig.entryTtl(Duration.ofMinutes(10)),
            "resourceQuotas" to defaultConfig.entryTtl(Duration.ofMinutes(5)),
            
            // Monitoring and alerts (5 minutes TTL - should be fresh)
            "alerts" to defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "auditLogs" to defaultConfig.entryTtl(Duration.ofMinutes(1)),  // Audit should be very fresh
            
            // Billing (no cache - always fetch latest)
            "billing" to defaultConfig.entryTtl(Duration.ZERO)
        )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(30)))
            .withInitialCacheConfigurations(cacheConfigs)
            .build()
    }

    /**
     * Fallback in-memory cache manager for development/testing without Redis
     */
    @Bean(name = ["fallbackCacheManager"])
    @ConditionalOnMissingBean(CacheManager::class)
    fun fallbackCacheManager(): CacheManager {
        return ConcurrentMapCacheManager(
            "dataCenters", "nodes", "nodeGroups",
            "templates", "templateGroups",
            "ipv4Pools", "ipv4s", "ipv6Ranges", "ipv6Allocations",
            "servers", "serverGroups",
            "storage", "resourceQuotas",
            "alerts", "auditLogs", "billing"
        )
    }
}
