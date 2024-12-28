package com.reddotdigitalit.sisyphus.util;


import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;


public class GeneralUtil {
  public static final CharsetEncoder LATIN_CHARSET_ENCODER = StandardCharsets.ISO_8859_1.newEncoder();
  public static final String SUCCESS_CODE_A2P = "1000";
  public static final String A2P_ACCOUNT_BARRED = "Account Barred";
  public static final String PROCESS_NUMBERS_CP_API = "process_numbers_cp_api";
  public static final String PROCESS_NUMBERS_DC_V2_API = "process_numbers_dc_v2_api";
  public static final String PROCESS_NUMBERS_DC_API = "process_numbers_dc_api";
  public static final String PROCESS_NUMBERS_DC_API_MULTI = "process_numbers_dc_api_multi";
  public static final String PROCESS_NUMBERS_DC_SAPI = "process_numbers_dc_sapi";
  public static final String INVALID_USERNAME = "Invalid Username";
  public static final String INVALID_PASS = "Invalid Password";
  public static final String INVALID_CLI = "CLI/Masking Invalid";
  public static final String SUCCESS = "Success";
  public static final String INVALID_TRANSACTION_ID = "Invalid transaction ID";
  public static final String PARAMETER_MISSING = "Parameter missing";
  public static final String DUPLICATE_TRANSACTION_ID = "Duplicate Transaction ID";
  public static final String INSUFFICIENT_BALANCE = "Insufficient Balance";
  public static final String INVALID_PARAMETER = "Invalid Parameter";
  public static final String MESSAGE_LENGTHS_EXCEED = "Message lengths exceed";
  public static final String TOO_MANY_REQ_MSG = "Too Many Request. Try in a minute.";
  public static final String INVALID_MSISDN = "Invalid MSISDN";
  public static final String MSISDN_LIMIT_EXCEEDED = "MSISDN Limit Exceeded";
  public static final String SUBMISSION_RECORD_NOT_FOUND = "Submission record not found";
  public static final String ERROR_OCCURRED = "Error occurred";
  public static final String NOT_ENOUGH_CREDIT = "not_enough_credit";
  public static final String NO_VALID_NUMBERS = "no_valid_numbers";
  public static final String SUCCESSFULLY_TRANSMITTED = "Successfully transmitted";
  public static final String PENDING = "Pending";
  public static final String DELIVERY_PENDING = "Delivery Pending";
  public static final String INVALID_PARAMETER_CODE_A2P = "1005";
  public static final String DUPLICATE_TRANSACTION_ID_CODE_A2P = "1011";
  public static final String INVALID_TRANSACTION_CODE_A2P = "1053";
  public static final String INVALID_MSISDN_CODE_A2P = "1010";
  public static final String PARAMETER_MISSING_CODE_A2P = "1004";
  public static final String MSISDN_LIMIT_EXCEEDED_CODE_A2P = "1054";
  public static final String INVALID_USERNAME_CODE_A2P = "1002";
  public static final String MESSAGE_LENGTHS_EXCEED_CODE_A2P = "1012";
  public static final String INVALID_PASS_CODE_A2P = "1003";
  public static final String INVALID_CLI_CODE_A2P = "1006";
  public static final String ACCOUNT_BARRED_CODE_A2P = "1007";
  public static final String INSUFFICIENT_BALANCE_CODE_A2P = "1008";
  public static final String API_NOT_ALLOWED_FOR_USER = "API Not allowed for user";
  public static final String API_NOT_ALLOWED_FOR_USER_CODE_A2P = "1017";
  public static final String NO_REQUEST_FOUND = "No Request found";
  public static final String DELIVERED = "Delivered";
  public static final String UNDELIVERED = "UnDelivered";
  public static final String DND_USER = "DND User";
  public static final String NO_REQUEST_FOUND_CODE_A2P = "1013";
  public static final String DND_USER_CODE_A2P = "1009";
  public static final String NEW_API_NOT_ALLOWED = "New api not allowed";
  public static final String NEW_API_AUTH_FAIL = "New api auth fail";
  public static final String NEW_API_OTHER_ERROR = "New api Other Error";
  public static final String DUPLICATE_MID = "Duplicate MID";


  public static final String INVALID_PARAMETER_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<ArrayOfServiceClass>\n" +
    "    <ServiceClass>\n" +
    "        <MessageId>0</MessageId>\n" +
    "        <Status>-1</Status>\n" +
    "        <StatusText>Error occurred</StatusText>\n" +
    "        <ErrorCode>1504</ErrorCode>\n" +
    "        <ErrorText>Invalid Parameter</ErrorText>\n" +
    "        <SMSCount>0</SMSCount>\n" +
    "        <CurrentCredit>0.0</CurrentCredit>\n" +
    "    </ServiceClass>\n" +
    "</ArrayOfServiceClass>";

  public static String errorResponse(int code, String text) {
    String templateString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<ArrayOfServiceClass>\n" +
      "    <ServiceClass>\n" +
      "        <MessageId>0</MessageId>\n" +
      "        <Status>-1</Status>\n" +
      "        <StatusText>Error occurred</StatusText>\n" +
      "        <ErrorCode>%d</ErrorCode>\n" +
      "        <ErrorText>%s</ErrorText>\n" +
      "        <SMSCount>0</SMSCount>\n" +
      "        <CurrentCredit>0.0</CurrentCredit>\n" +
      "    </ServiceClass>\n" +
      "</ArrayOfServiceClass>";

    return String.format(templateString, code, text);
  }

  public static List<String> filterValidNumbers(List<String> numbers) {
    return numbers
      .parallelStream()
      .map(x -> {
        StringBuilder sb = new StringBuilder();
        if (Pattern.matches("^\\d+", x) && x.startsWith("01") && x.length() == 11) {
          sb.append("88").append(x);
        } else if (Pattern.matches("^\\d+", x) && x.startsWith("8801") && x.length() == 13) {
          sb.append(x);
        } else {
          return null;
        }
        return sb.toString();
      })
      .filter(Objects::nonNull)
      .distinct()
      .filter(x -> x.startsWith("8801") && x.length() == 13)
      .toList();
  }
}

