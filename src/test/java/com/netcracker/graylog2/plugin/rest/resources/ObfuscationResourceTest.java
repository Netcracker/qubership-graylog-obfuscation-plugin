package com.netcracker.graylog2.plugin.rest.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.netcracker.graylog2.plugin.utils.RegularExpressionValidator;
import java.util.Map;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

public class ObfuscationResourceTest {

  @Test
  public void testCompileRegularExpressionsAcceptsValidPattern() {
    Map<String, Map<String, Object>> entity =
        RegularExpressionCompileTester.testCompile(new JSONArray("[\"[0-9]{3}-[0-9]{2}\"]"));

    assertTrue(entity.isEmpty());
  }

  @Test
  public void testCompileRegularExpressionsReportsInvalidPattern() {
    Map<String, Map<String, Object>> entity =
        RegularExpressionCompileTester.testCompile(new JSONArray("[\"[\"]"));

    assertTrue(entity.containsKey("["));
    assertEquals("missing closing ]", entity.get("[").get("description"));
  }

  @Test
  public void testCompileRegularExpressionsRejectsUnsupportedJavaRegexSyntax() {
    Map<String, Map<String, Object>> entity =
        RegularExpressionCompileTester.testCompile(new JSONArray("[\"(?=secret)secret\"]"));

    assertTrue(entity.containsKey("(?=secret)secret"));
  }

  @Test
  public void testCompileRegularExpressionsRejectsTooLongPattern() {
    String expression = "a".repeat(RegularExpressionValidator.MAX_REGULAR_EXPRESSION_LENGTH + 1);
    Map<String, Map<String, Object>> entity =
        RegularExpressionCompileTester.testCompile(new JSONArray("[\"" + expression + "\"]"));

    assertTrue(entity.containsKey(expression));
    assertEquals(-1, entity.get(expression).get("index"));
  }
}
