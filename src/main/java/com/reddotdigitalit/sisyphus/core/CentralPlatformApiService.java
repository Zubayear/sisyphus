package com.reddotdigitalit.sisyphus.core;

import com.reddotdigitalit.sisyphus.util.GeneralUtil;
import com.reddotdigitalit.sisyphus.util.ResponseUtil;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class CentralPlatformApiService {
  private static final Logger logger = Logger.getLogger(CentralPlatformApiService.class.getName());

  private static final List<String> MANDATORY_FIELDS = Arrays.asList("username", "password", "apicode", "msisdn", "countrycode", "cli", "messagetype", "message", "clienttransid", "tran_type", "request_type", "rn_code", "bill_msisdn");

  public void deliveryRequestHandler(RoutingContext context, JsonObject request, Pool pool, RedisAPI redisAPI) {

  }

  public void creditHandler(RoutingContext context, JsonObject request, Pool pool, RedisAPI redisAPI) {

  }

  public void cliHandler(RoutingContext context, JsonObject request, Pool pool, RedisAPI redisAPI) {

  }

  public void smsHandler(RoutingContext context, JsonObject request, Pool pool, RedisAPI redisAPI) {
    checkMissingParameter(context, request);
    if (context.response().ended()) return;
    if (!isValidTransactionId(request.getString("clienttransid"))) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_TRANSACTION_CODE_A2P,
        GeneralUtil.INVALID_TRANSACTION_ID,
        request.getString("clienttransid"),
        "");
    }
    Object msisdn = request.getValue("msisdn");
    if (msisdn instanceof String) {
      request.put("msisdn", new JsonArray().add(msisdn));
    }
    if (context.response().ended()) return;
    invalidParamValidation(context, request);
    if (context.response().ended()) return;
    checkNumbers(context, request);
    if (context.response().ended()) return;
    checkCredential(context, request, pool, redisAPI);
    if (context.response().ended()) {
      logger.info("Context ended, so i will not do further checking");
      return;
    }
    logger.info("will do the duplicate transaction id check");
    isDuplicateTransactionId(context, request, redisAPI);
    if (context.response().ended()) return;
    callStoredProcedure(context, request, pool);
  }

  /*
   * Crucial methods
   * */
  private boolean isValidTransactionId(final String id) {
    return id.length() >= 10 && id.length() <= 36 && id.matches("^[a-zA-Z0-9]+$");
  }

  private void invalidParamValidation(RoutingContext context, JsonObject request) {
    // Implement parameter validation logic
    if (isAnyFieldMissing(request)) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_PARAMETER_CODE_A2P,
        GeneralUtil.INVALID_PARAMETER,
        request.getString("clienttransid"),
        "");
    }

    String tranType = request.getString("tran_type");
    if (!"T".equals(tranType) && !"P".equals(tranType)) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_PARAMETER_CODE_A2P,
        GeneralUtil.INVALID_PARAMETER,
        request.getString("clienttransid"),
        "");
    }

    String msgType = request.getString("messagetype");
    if (!"1".equals(msgType) && !"3".equals(msgType)) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_PARAMETER_CODE_A2P,
        GeneralUtil.INVALID_PARAMETER,
        request.getString("clienttransid"),
        "");
    }

    // unicode message can't be more than 1600 character
    if ("1".equals(msgType) && request.getString("message").length() > 1600) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.MESSAGE_LENGTHS_EXCEED_CODE_A2P,
        GeneralUtil.MESSAGE_LENGTHS_EXCEED,
        request.getString("clienttransid"),
        "");
      return;
    }

    // unicode message can't be more than 700 character
    if ("3".equals(msgType) && request.getString("message").length() > 700) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.MESSAGE_LENGTHS_EXCEED_CODE_A2P,
        GeneralUtil.MESSAGE_LENGTHS_EXCEED,
        request.getString("clienttransid"),
        "");
      return;
    }

    String requestType = request.getString("request_type");
    JsonArray msisdnArr = request.getJsonArray("msisdn");
    if ("S".equals(requestType) && msisdnArr.size() > 1) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_PARAMETER_CODE_A2P,
        GeneralUtil.INVALID_PARAMETER,
        request.getString("clienttransid"),
        "");
      return;
    }

    if ("T".equals(tranType) && "B".equals(requestType)) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_PARAMETER_CODE_A2P,
        GeneralUtil.INVALID_PARAMETER,
        request.getString("clienttransid"),
        "");
      return;
    }

    // promotional message can't be english
    if ("P".equals(tranType) && "1".equals(msgType)) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_PARAMETER_CODE_A2P,
        GeneralUtil.INVALID_PARAMETER,
        request.getString("clienttransid"),
        "");
      return;
    }

    // promotional message can have at most 1000 number
    if ("P".equals(tranType) && msisdnArr.size() > 1000) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.MSISDN_LIMIT_EXCEEDED_CODE_A2P,
        GeneralUtil.MSISDN_LIMIT_EXCEEDED,
        request.getString("clienttransid"),
        "");
      return;
    }
    String cli = request.getString("cli");

    if (cli.startsWith("88") && cli.length() != 13) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_PARAMETER_CODE_A2P,
        GeneralUtil.INVALID_PARAMETER,
        request.getString("clienttransid"),
        "");
    }
  }

  private boolean isAnyFieldMissing(JsonObject request) {
    return MANDATORY_FIELDS.stream().anyMatch(field -> isFieldEmpty(getFieldValue(request, field)));
  }

  private Object getFieldValue(JsonObject request, String fieldName) {
    return switch (fieldName) {
      case "username", "password", "apicode", "msisdn", "countrycode", "cli", "messagetype", "message",
           "clienttransid", "bill_msisdn", "tran_type", "request_type", "rn_code" -> request.getValue(fieldName);
      default -> null;
    };
  }

  private boolean isFieldEmpty(Object value) {
    return (value instanceof String && ((String) value).isEmpty());
  }

  private void checkNumbers(RoutingContext context, JsonObject request) {
    JsonArray msisdnArr = request.getJsonArray("msisdn");
    if ("T".equals(request.getString("tran_type"))) {
      String str = msisdnArr.getString(0);
      if (Pattern.matches("^\\d+", str) && str.startsWith("01") && str.length() == 11) {
        str = "88" + str;
        request.put("msisdn", new JsonArray().add(str));
      }
    }
    List<String> validNumbers = msisdnArr.stream()
      .map(Object::toString)
      .parallel()
      .distinct()
      .filter(x -> Pattern.matches("^\\d+", x) && x.startsWith("01") && (x.length() == 11))
      .map(s -> "88" + s)
      .toList();
    if (validNumbers.isEmpty()) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_MSISDN_CODE_A2P,
        GeneralUtil.INVALID_MSISDN,
        request.getString("clienttransid"),
        "");
    }
  }

  private void checkCredential(RoutingContext context, JsonObject request, Pool pool, RedisAPI redisAPI) {
    String username = request.getString("username");
    String key = "user:" + username;
    redisAPI.hgetall(key)
      .onSuccess(res -> {
        logger.info("response is " + res);
        if (res != null && res.size() > 0) {
          processUserInfo(context, request, username, res.get("username").toString(), res.get("password").toString(), res.get("cli").toString(), res.get("status").toInteger(), res.get("cpApiEnabled").toString());
        } else {
          pool.withConnection(conn -> conn.preparedQuery("SELECT username, password, mt_port, status, acl_list ->> 'c11_campaign_api' AS c11_campaign_api, acl_list ->> 'c15_campaign_cpapi' AS c15_campaign_cpapi, acl_list ->> 'c12_campaign_sapi' AS c12_campaign_sapi, acl_list ->> 'c18_campaign_v2_api' AS campaign_api_v2, mid_expiry_time FROM adareach.tbl_user WHERE username=$1")
            .execute(Tuple.of(username))
            .onSuccess(s -> {
              RowSet<Row> row = s.value();
              row.forEach(v -> {
                processUserInfo(context, request, username, v.getString("username"), v.getString("password"), v.getString("mt_port"), v.getInteger("status"), v.getString("c15_campaign_cpapi"));
                redisAPI.hset(List.of(key,
                    "username", username,
                    "password", v.getString("password"),
                    "cli", v.getString("mt_port"),
                    "status", String.valueOf(v.getInteger("status")),
                    "cpApiEnabled", v.getString("c15_campaign_cpapi"),
                    "dcApiEnabled", v.getString("c11_campaign_api"),
                    "specialApiEnabled", v.getString("c12_campaign_sapi"),
                    "dcApiV2Enabled", v.getString("campaign_api_v2"),
                    "midExpiryTime", String.valueOf(v.getLong("mid_expiry_time"))))
                  .compose(c -> redisAPI.expire(List.of(key, "3600")))
                  .onSuccess(response -> {
                    if ("1".equals(response.toString())) {
                      logger.info("TTL set successfully for the hash key!");
                    } else {
                      logger.info("Failed to set TTL or key does not exist.");
                    }
                  }).onFailure(err -> logger.severe("Failed to set hash with TTL: " + err.getMessage()));
              });
            })
            .onFailure(e -> {
              logger.severe(e.getLocalizedMessage());
              ResponseUtil.sendSmsResponse(context,
                GeneralUtil.INVALID_PARAMETER_CODE_A2P,
                "Error occurred, Try Later",
                request.getString("clienttransid"),
                "");
            })
          );
        }
      })
      .onFailure(e -> {
        logger.severe("error occurred " + e.getLocalizedMessage());
        ResponseUtil.sendSmsResponse(context,
          GeneralUtil.INVALID_PARAMETER_CODE_A2P,
          "Error occurred, Try Later",
          request.getString("clienttransid"),
          "");
      });
  }

  private void processUserInfo(RoutingContext context, JsonObject request, String username, String username2, String password, String cli, Integer status, String cpApiEnabled) {
    if (!username.equals(username2)) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_USERNAME_CODE_A2P,
        GeneralUtil.INVALID_USERNAME,
        request.getString("clienttransid"),
        "");
    }
    logger.info("username is okay");
    if (!DigestUtils.md5Hex(request.getString("password")).equals(password)) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_PASS_CODE_A2P,
        GeneralUtil.INVALID_PASS,
        request.getString("clienttransid"),
        "");
    }
    logger.info("password is okay");
    Set<String> sources = new HashSet<>(Arrays.asList(cli.split(",")));
    if (!sources.contains(request.getString("cli"))) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.INVALID_CLI_CODE_A2P,
        GeneralUtil.INVALID_CLI,
        request.getString("clienttransid"),
        "");
    }
    logger.info("cli is okay");
    if (status == -1) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.ACCOUNT_BARRED_CODE_A2P,
        GeneralUtil.A2P_ACCOUNT_BARRED,
        request.getString("clienttransid"),
        "");
    }
    logger.info("status is okay");
    if (!cpApiEnabled.equals("true")) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.API_NOT_ALLOWED_FOR_USER_CODE_A2P,
        GeneralUtil.API_NOT_ALLOWED_FOR_USER,
        request.getString("clienttransid"),
        "");
    }
    logger.info("api is okay");
  }

  private void isDuplicateTransactionId(RoutingContext context, JsonObject request, RedisAPI redisAPI) {
    String key = request.getString("username") + ":" + request.getString("clienttransid");
    redisAPI.exists(List.of(key))
      .onSuccess(s -> {
        logger.info("s is " + s);
        if (s.toInteger() > 0) {
          ResponseUtil.sendSmsResponse(context,
            GeneralUtil.DUPLICATE_TRANSACTION_ID,
            "",
            request.getString("clienttransid"),
            "");
        } else {
          redisAPI.setex(key, "120", "")
            .onSuccess(response -> {
              if ("OK".equals(response.toString())) {
                logger.info("TTL set successfully for the hash key!");
              } else {
                logger.info("Failed to set TTL or key does not exist.");
              }
            }).onFailure(err -> logger.severe("Failed to set hash with TTL: " + err.getMessage()));
        }
      })
      .onFailure(e -> {
        logger.severe(e.getLocalizedMessage());
        ResponseUtil.sendSmsResponse(context,
          GeneralUtil.INVALID_PARAMETER_CODE_A2P,
          "Error occurred, Try Later",
          request.getString("clienttransid"),
          "");
      });
  }

  private void checkMissingParameter(RoutingContext context, JsonObject request) {
    if (paramMissing(request)) {
      ResponseUtil.sendSmsResponse(context,
        GeneralUtil.PARAMETER_MISSING_CODE_A2P,
        GeneralUtil.PARAMETER_MISSING,
        request.getString("clienttransid"),
        "");
    }
  }

  private boolean paramMissing(JsonObject request) {
    return MANDATORY_FIELDS.stream().anyMatch(x -> isFieldNull(getFieldValue(request, x)));
  }

  private boolean isFieldNull(Object value) {
    return value == null;
  }

  private void callStoredProcedure(RoutingContext context, JsonObject request, Pool pool) {
    JsonArray msisdnArr = request.getJsonArray("msisdn");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < msisdnArr.size(); i++) {
      sb.append(msisdnArr.getString(i)).append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    String nums = sb.toString();
    String username = request.getString("username");
    String content = request.getString("message");
    String source = request.getString("cli");
    int apiCode = Integer.parseInt(request.getString("apicode"));
    int msgType = "T".equals(request.getString("tran_type")) ? 1 : 2;
    int isUnicode = Objects.equals(request.getString("messagetype"), "3") ? 1 : 0;
    Long refCode = 12l;
    String reqId = UUID.randomUUID().toString();

    pool.withConnection(conn -> conn.preparedQuery("CALL adareach.process_numbers_cp_api ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)")
      .execute(Tuple.of(source, username, "", nums, apiCode, msgType, isUnicode, content.length(), reqId))
      .onSuccess(s -> s.forEach(v -> {
        JsonObject entry = new JsonObject(v.toString());
        String msg = entry.getString("msg");
        if (msg.equals(GeneralUtil.NO_VALID_NUMBERS)) {
          ResponseUtil.sendSmsResponse(context,
            GeneralUtil.INVALID_PARAMETER_CODE_A2P,
            GeneralUtil.INVALID_PARAMETER,
            "",
            "");
        } else if (msg.equals(GeneralUtil.NOT_ENOUGH_CREDIT)) {
          ResponseUtil.sendSmsResponse(context,
            GeneralUtil.INSUFFICIENT_BALANCE_CODE_A2P,
            GeneralUtil.INSUFFICIENT_BALANCE,
            "",
            "");
        } else {
          ResponseUtil.sendSmsResponse(context,
            GeneralUtil.SUCCESS_CODE_A2P,
            GeneralUtil.SUCCESS,
            request.getString("clienttransid"),
            String.valueOf(refCode));
        }
      })));
  }
}
