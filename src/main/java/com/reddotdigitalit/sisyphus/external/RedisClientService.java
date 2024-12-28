package com.reddotdigitalit.sisyphus.external;

import io.vertx.core.Vertx;
import io.vertx.redis.client.*;

import java.util.logging.Logger;

/**
 * The RedisClientService class provides a utility for managing a Redis client
 * connection in a singleton-like manner. It is designed to operate in Sentinel
 * mode, supporting master-slave configurations with high availability.
 *
 * The service initializes a Redis client only once during the application lifecycle,
 * offering access to RedisAPI for executing Redis commands. It also allows
 * graceful shutdown of the client to release associated resources.
 */
public class RedisClientService {

  private static final Logger logger = Logger.getLogger(RedisClientService.class.getName());

  private static Redis redisClient;
  private static RedisAPI redisAPI;

  private RedisClientService() {
    // Private constructor to prevent instantiation
  }

  /**
   * Initializes the Redis client and RedisAPI in Sentinel mode. This method ensures
   * that the client is created only once throughout the application's lifecycle.
   *
   * @param vertx The Vert.x instance used for configuring and creating the Redis client.
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

      RedisOptions opt = new RedisOptions()
        .setType(RedisClientType.STANDALONE)
//        .setMasterName("master")
//        .setRole(RedisRole.MASTER)
        .setPassword("uaS1eegaih3AeYoh")
        .setConnectionString("redis://uaS1eegaih3AeYoh@10.101.73.72:32008");

      redisClient = Redis.createClient(vertx, opt);
      redisAPI = RedisAPI.api(redisClient);
    }
  }

  /**
   * Retrieves the RedisAPI instance for executing Redis commands.
   * This method ensures that the RedisAPI is properly initialized before returning it.
   * If the RedisAPI instance is not initialized, an IllegalStateException is thrown.
   *
   * @return The configured instance of RedisAPI.
   * @throws IllegalStateException if the RedisAPI has not been initialized by calling {@code initialize}.
   */
  public static RedisAPI getRedisAPI() {
    if (redisAPI == null) {
      throw new IllegalStateException("Redis client not initialized. Call initialize() first.");
    }
    return redisAPI;
  }

  /**
   * Closes the Redis client and releases associated resources.
   *
   * This method shuts down the Redis client and the RedisAPI, ensuring
   * that all resources are properly released. The operation is synchronized
   * to prevent concurrent attempts to close the client. If the Redis client
   * has already been closed or is uninitialized, the method does nothing.
   * A log entry is generated upon successful closure.
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
