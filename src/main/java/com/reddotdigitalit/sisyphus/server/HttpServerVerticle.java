package com.reddotdigitalit.sisyphus.server;

import com.reddotdigitalit.sisyphus.auth.JwtService;
import com.reddotdigitalit.sisyphus.external.PgPoolService;
import com.reddotdigitalit.sisyphus.external.RedisClientService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;

import java.util.Date;
import java.util.logging.Logger;

public class HttpServerVerticle extends AbstractVerticle {
  private static final Logger logger = Logger.getLogger(HttpServerVerticle.class.getName());

  @Override
  public void start(Promise<Void> promise) {
    Pool pool = PgPoolService.getPool();
    RedisAPI redisAPI = RedisClientService.getRedisAPI();
    JWTAuth jwtAuth = JwtService.getJwtAuth();


    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());


    router.get("/health").handler(this::healthHandler);
    router.get("/name").handler(JWTAuthHandler.create(jwtAuth))
      .failureHandler(ctx -> ctx.response().setStatusCode(401)
        .end(new JsonObject()
          .put("msg", "Unauthorized access, please provide a valid token.")
          .encode()))
        .handler(this::nameHandler);

    // Start Server
    server.requestHandler(router)
      .listen(42069)
      .onSuccess(result -> logger.info("HTTP server started on port " + result.actualPort()))
      .onFailure(error -> logger.severe("HTTP server failed to start: " + error.getMessage()));

    promise.complete();
  }

  private void nameHandler(RoutingContext context) {
    context.response()
      .putHeader("content-type", "application/json")
      .setStatusCode(200)
      .end(new JsonObject()
        .put("name", "Syed, Syed Ibna Zubayear")
        .put("data", new Date().toString())
        .encode());
  }

  private void healthHandler(RoutingContext context) {
    context.response()
      .putHeader("content-type", "application/json")
      .setStatusCode(200)
      .end(new JsonObject()
        .put("status", "UP")
        .put("message", "OK")
        .put("data", new Date().toString())
        .encode());
  }

  @Override
  public void stop(Promise<Void> promise) {
    logger.info("HTTP server stopped");
    promise.complete();
  }
}
