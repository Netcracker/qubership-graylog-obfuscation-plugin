package com.netcracker.graylog2.plugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.netcracker.graylog2.plugin.obfuscation.SensitiveRegularExpression;
import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;
import com.netcracker.graylog2.plugin.processor.ObfuscationMessageProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class Graylog6WiringTest {

  @Test
  public void guice7LoadsDefaultRulesAndCreatesTheMessageProcessor() {
    Injector injector = Guice.createInjector(new TestObfuscationModule());
    assertNotNull(injector.getInstance(ObfuscationMessageProcessor.class));

    Configuration configuration = injector.getInstance(Configuration.class);
    assertTrue(matches(configuration, "Social Security Number", "123-12-1234"));
    assertFalse(matches(configuration, "Social Security Number", "000-12-1234"));
    assertTrue(matches(configuration, "Password Number", "AB1234567"));
    assertTrue(matches(configuration, "Password Number", "123456789"));
    assertTrue(matches(configuration, "ICCID", "8944501234567890123"));
    assertFalse(matches(configuration, "ICCID", "894450123456789012345"));
  }

  @Test
  public void restResourcesUseJakartaAnnotations() throws IOException {
    String obfuscationResource =
        classFile("com.netcracker.graylog2.plugin.rest.resources.ObfuscationResource");
    String configurationResource =
        classFile("com.netcracker.graylog2.plugin.rest.resources.ConfigurationResource");
    String messageProcessor =
        classFile("com.netcracker.graylog2.plugin.processor.ObfuscationMessageProcessor");

    assertTrue(obfuscationResource.contains("jakarta/ws/rs/Path"));
    assertTrue(obfuscationResource.contains("jakarta/inject/Inject"));
    assertTrue(configurationResource.contains("jakarta/ws/rs/Path"));
    assertTrue(configurationResource.contains("jakarta/validation/constraints/NotNull"));
    assertTrue(messageProcessor.contains("jakarta/inject/Inject"));
    assertFalse(obfuscationResource.contains("javax/ws/rs/Path"));
    assertFalse(messageProcessor.contains("javax/inject/Inject"));
  }

  private static boolean matches(Configuration configuration, String ruleName, String text) {
    for (SensitiveRegularExpression rule : configuration.getSensitiveRegularExpressions()) {
      if (ruleName.equals(rule.getName())) {
        return rule.getPattern().matcher(text).find();
      }
    }
    throw new AssertionError("Missing default rule " + ruleName);
  }

  private static String classFile(String binaryName) throws IOException {
    String resource = binaryName.replace('.', '/') + ".class";
    try (InputStream input =
        Graylog6WiringTest.class.getClassLoader().getResourceAsStream(resource)) {
      assertNotNull(input, resource);
      return new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
    }
  }
}
