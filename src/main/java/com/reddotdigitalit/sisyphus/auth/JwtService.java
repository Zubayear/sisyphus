package com.reddotdigitalit.sisyphus.auth;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

/**
 * JwtService is a utility class that provides functionality to initialize and
 * manage a shared instance of JWTAuth for token authentication.
 *
 * This class uses the Vert.x framework to configure and create the JWTAuth
 * instance. It employs a singleton pattern to ensure there is only one shared
 * instance of JWTAuth throughout the application lifecycle.
 *
 * The initialize method must be called before attempting to retrieve the JWTAuth
 * instance. If the instance is accessed before initialization, an
 * IllegalStateException will be thrown.
 */
public class JwtService {
  private static JWTAuth jwtAuth;

  private JwtService() {
  }

  /**
   * Initializes the JWT authentication service with the specified Vert.x instance.
   * This method creates a shared instance of JWTAuth using a keystore configuration.
   * If the service is already initialized, it does nothing.
   *
   * @param vertx The Vert.x instance used to configure and create the JWTAuth instance.
   */
  public static synchronized void initialize(Vertx vertx) {
    if (jwtAuth == null) {
      var config = new JWTAuthOptions()
        .setKeyStore(new KeyStoreOptions()
          .setPath("keystore.jceks")
          .setType("jceks")
          .setPassword("secret"));
      jwtAuth = JWTAuth.create(vertx, config);
    }
  }

  /**
   * Returns the shared instance of the JWTAuth object.
   * This method provides access to the JWT authentication service,
   * which must be initialized before calling this method.
   *
   * @return The shared JWTAuth instance.
   * @throws IllegalStateException if the JWT authentication service has not been initialized.
   */
  public static JWTAuth getJwtAuth() {
    if (jwtAuth == null) {
      throw new IllegalStateException("JWT Auth not initialized. Call initialize() first.");
    }
    return jwtAuth;
  }
}
