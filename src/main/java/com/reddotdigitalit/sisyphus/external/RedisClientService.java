package com.reddotdigitalit.sisyphus.external;

import io.vertx.core.Vertx;
import io.vertx.redis.client.*;

import java.util.logging.Logger;

public class RedisClientService {

  private static final Logger logger = Logger.getLogger(RedisClientService.class.getName());

  private static Redis redisClient;
  private static RedisAPI redisAPI;

  private RedisClientService() {
    // Private constructor to prevent instantiation
  }

  /**
   * Initializes the Redis client in Sentinel mode.
   * @param vertx Vertx instance
   */
  public static synchronized void initialize(Vertx vertx) {
    if (redisClient == null) {
      RedisOptions options = new RedisOptions()
        .setType(RedisClientType.SENTINEL)
        .addConnectionString("redis://127.0.0.1:26379")
        .addConnectionString("redis://redis-sentinel-2:26380")
        .addConnectionString("redis://redis-sentinel-3:26381")
        .setMasterName("mymaster")
        .setRole(RedisRole.MASTER)
        .setMaxPoolSize(10);

      redisClient = Redis.createClient(vertx, options);
      redisAPI = RedisAPI.api(redisClient);
    }
  }

  /**
   * Provides access to the RedisAPI for command execution.
   * @return RedisAPI instance
   */
  public static RedisAPI getRedisAPI() {
    if (redisAPI == null) {
      throw new IllegalStateException("Redis client not initialized. Call initialize() first.");
    }
    return redisAPI;
  }

  /**
   * Gracefully closes the Redis client.
   */
  public static synchronized void close() {
    if (redisClient != null) {
      redisClient.close();
      redisClient = null;
      redisAPI = null;
      logger.info("Redis client has been closed successfully.");
    }
  }
}
