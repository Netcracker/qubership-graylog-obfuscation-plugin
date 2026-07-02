package com.netcracker.graylog2.plugin.rest.resources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.netcracker.graylog2.plugin.utils.RegularExpressionValidator;
import java.util.Map;
import java.util.regex.PatternSyntaxException;
import org.json.JSONArray;

final class RegularExpressionCompileTester {

  private RegularExpressionCompileTester() {}

  static Map<String, Map<String, Object>> testCompile(JSONArray expressions) {
    Map<String, Map<String, Object>> compilationFailedExpressions = Maps.newHashMap();

    for (int i = 0; i < expressions.length(); i++) {
      String expression = expressions.getString(i);
      try {
        RegularExpressionValidator.compile(expression);
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
}
