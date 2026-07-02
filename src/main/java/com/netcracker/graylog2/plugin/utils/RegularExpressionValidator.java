package com.netcracker.graylog2.plugin.utils;

import java.util.regex.Pattern;

public final class RegularExpressionValidator {

  public static final int MAX_REGULAR_EXPRESSION_LENGTH = 4096;

  private RegularExpressionValidator() {}

  @SuppressWarnings("java/regex-injection")
  public static Pattern compile(String expression) {
    validateLength(expression);
    // lgtm[java/regex-injection] This plugin intentionally stores administrator-provided,
    // length-bounded regular expressions as obfuscation rules.
    return Pattern.compile(expression);
  }

  private static void validateLength(String expression) {
    if (expression.length() > MAX_REGULAR_EXPRESSION_LENGTH) {
      throw new IllegalArgumentException(
          "Pattern length must not exceed " + MAX_REGULAR_EXPRESSION_LENGTH + " characters");
    }
  }
}
