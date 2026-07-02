package com.netcracker.graylog2.plugin.utils;

import com.google.re2j.Pattern;

public final class RegularExpressionValidator {

  public static final int MAX_REGULAR_EXPRESSION_LENGTH = 4096;

  private RegularExpressionValidator() {}

  public static Pattern compile(String expression) {
    validateLength(expression);
    return Pattern.compile(expression);
  }

  private static void validateLength(String expression) {
    if (expression.length() > MAX_REGULAR_EXPRESSION_LENGTH) {
      throw new IllegalArgumentException(
          "Pattern length must not exceed " + MAX_REGULAR_EXPRESSION_LENGTH + " characters");
    }
  }
}
