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

/**
 * HttpServerVerticle is responsible for initializing and starting an HTTP server using Vert.x.
 * It provides predefined endpoints, handles incoming HTTP requests, and manages dependencies
 * such as a PostgreSQL connection pool, Redis API, and JWT authentication.
 *
 * The server includes the following functionalities:
 * - A health check endpoint for monitoring the status of the application.
 * - A protected endpoint using JWT authentication for retrieving hardcoded user information.
 *
 * This verticle demonstrates integration with external services and libraries including:
 * - PostgreSQL connection pool management through {@code PgPoolService}.
 * - Redis client API through {@code RedisClientService}.
 * - JWT authentication via {@code JWTAuth}.
 *
 * Internal handlers are implemented for specific routes to handle HTTP requests.
 */
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

  /**
   * Handles the "/name" route by responding with a JSON object containing hardcoded user information
   * and the current date and time. The response is returned with a content type of "application/json"
   * and an HTTP status code of 200.
   *
   * @param context The routing context that contains the request and provides methods to construct the response.
   */
  private void nameHandler(RoutingContext context) {
    context.response()
      .putHeader("content-type", "application/json")
      .setStatusCode(200)
      .end(new JsonObject()
        .put("name", "Syed, Syed Ibna Zubayear")
        .put("data", new Date().toString())
        .encode());
  }

  /**
   * Handles the health check endpoint by responding with the current application status.
   * This endpoint provides a JSON response indicating the application's status,
   * an informational message, and the current server date and time.
   *
   * @param context The routing context for the current HTTP request, which provides
   *                access to the HTTP response and other request-related data.
   */
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

  /**
   * Stops the HTTP server and marks the provided promise as completed.
   * This method is invoked during the verticle shutdown process to ensure the HTTP server is stopped properly.
   *
   * @param promise The promise to be completed after the HTTP server has been stopped.
   */
  @Override
  public void stop(Promise<Void> promise) {
    logger.info("HTTP server stopped");
    promise.complete();
  }
}
