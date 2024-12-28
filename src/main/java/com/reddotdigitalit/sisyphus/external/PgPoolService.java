package com.reddotdigitalit.sisyphus.external;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

/**
 * The PgPoolService class provides a singleton-based shared connection pool
 * for interacting with a PostgreSQL database using Vert.x.
 * It is designed to be initialized once during the application lifecycle
 * and provides methods for obtaining and safely shutting down the connection pool.
 */
public class PgPoolService {

  private static Pool pool;

  private PgPoolService() {
    // Private constructor to prevent instantiation
  }

  /**
   * Initializes the PostgreSQL connection pool using the provided Vert.x instance.
   * This method must be called once during the application lifecycle before any
   * database operations are performed.
   *
   * @param vertx The Vert.x instance used for creating the connection pool.
   */
  public static synchronized void initialize(Vertx vertx) {
    if (pool == null) {
      PgConnectOptions connectOptions = new PgConnectOptions()
        .setPort(5678)
        .setHost("10.101.74.221")
        .setDatabase("mobireach_stage_27032024")
        .setUser("stage_mobireach")
        .setPassword("mb56%thgd]gR");

      PoolOptions poolOptions = new PoolOptions().setMaxSize(10);

      pool = PgBuilder.pool()
        .connectingTo(connectOptions)
        .with(poolOptions)
        .using(vertx)
        .build();
    }
  }

  /**
   * Retrieves the instance of the PostgreSQL connection pool.
   * This method ensures that the pool has been initialized before returning it.
   * If the pool has not been initialized, an IllegalStateException is thrown.
   *
   * @return The configured instance of the PostgreSQL connection pool.
   * @throws IllegalStateException if the pool has not been initialized by calling {@code initialize}.
   */
  public static Pool getPool() {
    if (pool == null) {
      throw new IllegalStateException("PgPoolService not initialized. Call initialize() first.");
    }
    return pool;
  }

  /**
   * Gracefully shuts down the PostgreSQL connection pool.
   * This method ensures that all resources associated with the pool are
   * released and that the pool is closed if it has been initialized.
   * It is safe to call this method multiple times, as the operation
   * has no effect if the pool has already been closed or was never initialized.
   */
  public static synchronized void shutdown() {
    if (pool != null) {
      pool.close();
    }
  }
}
