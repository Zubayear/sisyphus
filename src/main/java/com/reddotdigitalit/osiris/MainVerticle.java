package com.reddotdigitalit.osiris;

import com.reddotdigitalit.osiris.auth.JwtService;
import com.reddotdigitalit.osiris.external.PgPoolService;
import com.reddotdigitalit.osiris.external.RedisClientService;
import com.reddotdigitalit.osiris.server.HttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;

import java.util.logging.Logger;

public class MainVerticle extends AbstractVerticle {
  private static final Logger logger = Logger.getLogger(MainVerticle.class.getName());

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

  @Override
  public void stop(Promise<Void> stopPromise) {
    logger.info("MainVerticle stopped");
    stopPromise.complete();
  }
}
