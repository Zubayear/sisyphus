package com.reddotdigitalit.sisyphus.util;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ResponseUtil {

  public static void sendSmsResponse(RoutingContext context, String statusCode, String desc, String transactionId, String refCode) {

    if (context.response().ended()) {
      System.out.println("context ended");
      return;
    }
    // Manually convert SmsResponse into JsonObject
    JsonObject responseJson = new JsonObject()
      .put("statusInfo", new JsonObject()
        .put("statusCode", statusCode)
        .put("errordescription", desc)
        .put("clienttransid", transactionId)
        .put("serverReferenceCode", refCode)
      );

    // Send response via RoutingContext
    context.response()
      .setStatusCode(200)
      .end(responseJson.encode());
  }
}
