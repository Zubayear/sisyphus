package com.reddotdigitalit.sisyphus;

import com.reddotdigitalit.sisyphus.auth.JwtService;
import com.reddotdigitalit.sisyphus.external.PgPoolService;
import com.reddotdigitalit.sisyphus.external.RedisClientService;
import com.reddotdigitalit.sisyphus.server.HttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;

import java.util.logging.Logger;

/**
 * The MainVerticle class extends the AbstractVerticle and represents the main entry point
 * of the application. It is responsible for initializing required services and deploying
 * the HTTPServerVerticle. Additionally, it handles the lifecycle methods for startup and
 * shutdown processes.
 */
public class MainVerticle extends AbstractVerticle {
  private static final Logger logger = Logger.getLogger(MainVerticle.class.getName());

  /**
   * Starts the main verticle by initializing required services and deploying the HTTP server verticle.
   *
   * @param promise The promise to mark the completion of the verticle start method.
   *                It should be completed after all initializations and deployments are handled.
   */
  @Override
  public void start(Promise<Void> promise) {
    DeploymentOptions deploymentOptions = new DeploymentOptions();
    PgPoolService.initialize(vertx);
    RedisClientService.initialize(vertx);
    JwtService.initialize(vertx);

    vertx.deployVerticle(new HttpServerVerticle(), deploymentOptions, res -> {
      if (res.succeeded()) {
        logger.info("MainVerticle started");
      } else {
        logger.severe("MainVerticle failed to start " + res.cause().getMessage());
      }
    });
    promise.complete();
  }

  /**
   * Stops the MainVerticle and completes the provided stop promise.
   * This method is typically invoked when the verticle is undeployed or the application is shutting down.
   *
   * @param stopPromise The promise that should be completed when the verticle has been stopped.
   */
  @Override
  public void stop(Promise<Void> stopPromise) {
    logger.info("MainVerticle stopped");
    stopPromise.complete();
  }
}
