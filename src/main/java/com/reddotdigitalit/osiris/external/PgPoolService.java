package com.reddotdigitalit.osiris.external;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

public class PgPoolService {

  private static Pool pool;

  private PgPoolService() {
    // Private constructor to prevent instantiation
  }

  /**
   * Initialize the PgPool service.
   * This should be called once, typically in the main application.
   */
  public static synchronized void initialize(Vertx vertx) {
    if (pool == null) {
      PgConnectOptions connectOptions = new PgConnectOptions()
        .setPort(5432)
        .setHost("127.0.0.1")
        .setDatabase("big_data")
        .setUser("postgres")
        .setPassword("");

      PoolOptions poolOptions = new PoolOptions().setMaxSize(10);

      pool = PgBuilder.pool()
        .connectingTo(connectOptions)
        .with(poolOptions)
        .using(vertx)
        .build();
    }
  }

  /**
   * Get the shared PgPool instance.
   *
   * @return The PgPool instance.
   */
  public static Pool getPool() {
    if (pool == null) {
      throw new IllegalStateException("PgPoolService not initialized. Call initialize() first.");
    }
    return pool;
  }

  public static synchronized void shutdown() {
    if (pool != null) {
      pool.close();
    }
  }
}
