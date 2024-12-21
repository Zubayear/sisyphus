package com.reddotdigitalit.osiris.auth;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

public class JwtService {
  private static JWTAuth jwtAuth;

  private JwtService() {
  }

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

  public static JWTAuth getJwtAuth() {
    if (jwtAuth == null) {
      throw new IllegalStateException("JWT Auth not initialized. Call initialize() first.");
    }
    return jwtAuth;
  }
}
