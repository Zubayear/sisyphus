package com.reddotdigitalit.sisyphus;

import com.reddotdigitalit.sisyphus.auth.JwtService;
import com.reddotdigitalit.sisyphus.external.PgPoolService;
import com.reddotdigitalit.sisyphus.external.RedisClientService;
import com.reddotdigitalit.sisyphus.server.HttpServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Main {
  public static void main(String[] args) {
    DeploymentOptions deploymentOptions = new DeploymentOptions();
    Vertx vertx = Vertx.vertx();
    PgPoolService.initialize(vertx);
    RedisClientService.initialize(vertx);
    JwtService.initialize(vertx);

    vertx.deployVerticle(new HttpServerVerticle(), deploymentOptions, res -> {
      if (res.succeeded()) {
//        logger.info("MainVerticle started");
      } else {
//        logger.severe("MainVerticle failed to start " + res.cause().getMessage());
      }
    });
//    promise.complete();
  }
}
