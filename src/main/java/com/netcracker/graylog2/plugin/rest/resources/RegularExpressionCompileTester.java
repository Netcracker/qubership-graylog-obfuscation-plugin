package com.netcracker.graylog2.plugin.rest.resources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.json.JSONArray;

final class RegularExpressionCompileTester {

  static final int MAX_REGULAR_EXPRESSION_LENGTH = 4096;

  private RegularExpressionCompileTester() {}

  @SuppressWarnings("java/regex-injection")
  static Map<String, Map<String, Object>> testCompile(JSONArray expressions) {
    Map<String, Map<String, Object>> compilationFailedExpressions = Maps.newHashMap();

    for (int i = 0; i < expressions.length(); i++) {
      String expression = expressions.getString(i);
      try {
        validateRegularExpressionLength(expression);
        // lgtm[java/regex-injection] This endpoint intentionally compiles bounded
        // administrator-provided regular expressions to validate configuration syntax.
        Pattern.compile(expression);
      } catch (PatternSyntaxException exception) {
        compilationFailedExpressions.put(
            expression,
            ImmutableMap.of(
                "description", exception.getDescription(), "index", exception.getIndex()));
      } catch (IllegalArgumentException exception) {
        compilationFailedExpressions.put(
            expression, ImmutableMap.of("description", exception.getMessage(), "index", -1));
      }
    }

    return compilationFailedExpressions;
  }

  private static void validateRegularExpressionLength(String expression) {
    if (expression.length() > MAX_REGULAR_EXPRESSION_LENGTH) {
      throw new IllegalArgumentException(
          "Pattern length must not exceed " + MAX_REGULAR_EXPRESSION_LENGTH + " characters");
    }
  }
}
