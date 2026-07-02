package com.netcracker.graylog2.plugin.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.re2j.Pattern;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationEngine;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationRequest;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationResponse;
import com.netcracker.graylog2.plugin.obfuscation.SensitiveDataResolver;
import com.netcracker.graylog2.plugin.obfuscation.SensitiveRegularExpression;
import com.netcracker.graylog2.plugin.obfuscation.WhiteListService;
import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;
import com.netcracker.graylog2.plugin.obfuscation.configuration.ConfigurationProvider;
import com.netcracker.graylog2.plugin.obfuscation.configuration.ConfigurationSerializer;
import com.netcracker.graylog2.plugin.obfuscation.configuration.ConfigurationService;
import com.netcracker.graylog2.plugin.obfuscation.replace.StaticStarTextReplacer;
import com.netcracker.graylog2.plugin.obfuscation.search.RegularExpressionSensitiveDataSearcher;
import com.netcracker.graylog2.plugin.obfuscation.search.SensitiveDataSearcher;
import com.netcracker.graylog2.plugin.utils.ResourceLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObfuscationEngineTest {

  private static final String SSN_PATTERN =
      "\\b(?:[0-6][0-4]\\d)-(?:0[1-9]|[1-9]\\d)-(?:000[1-9]|00[1-9]\\d|0[1-9]\\d\\d|[1-9]\\d{3})\\b";

  private ObfuscationEngine obfuscationEngine;

  private Configuration configuration;

  @BeforeEach
  public void setUp() {
    this.configuration = new Configuration();
    this.configuration.setObfuscationEnabled(true);
    this.configuration.setSensitiveRegularExpressions(
        Collections.singletonList(
            new SensitiveRegularExpression(
                1, "Social Security Number", Pattern.compile(SSN_PATTERN), 1)));
    this.configuration.setWhiteRegularExpressions(Collections.emptyList());

    ConfigurationService configurationService =
        new ConfigurationService(
            configuration,
            new NoOpConfigurationProvider(),
            new ConfigurationSerializer(),
            new ResourceLoader());
    WhiteListService whiteListService = new WhiteListService(configuration);
    Set<SensitiveDataSearcher> searchers =
        Collections.singleton(
            new RegularExpressionSensitiveDataSearcher(configuration, whiteListService));

    this.obfuscationEngine =
        new ObfuscationEngine(configurationService, searchers, new SensitiveDataResolver());
  }

  @Test
  public void simpleSSNObfuscationTest() {
    ObfuscationResponse obfuscationResponse =
        obfuscationEngine.obfuscateText(new ObfuscationRequest("123-12-1234"));
    assertEquals("********", obfuscationResponse.getObfuscatedText());
    assertEquals(
        "RegularExpression#Social Security Number",
        obfuscationResponse.getFoundSensitiveData().get(0).getFinder());
  }

  @Test
  public void obfuscationConflictTest() {
    configuration.setSensitiveRegularExpressions(getConflictedSensitiveRegularExpressions(1, 1));
    ObfuscationResponse obfuscationResponse =
        obfuscationEngine.obfuscateText(new ObfuscationRequest("121"));

    assertEquals(StaticStarTextReplacer.OBFUSCATED, obfuscationResponse.getObfuscatedText());
  }

  @Test
  public void obfuscationLeftConflictResolveTest() {
    configuration.setSensitiveRegularExpressions(getConflictedSensitiveRegularExpressions(2, 1));
    ObfuscationResponse obfuscationResponse =
        obfuscationEngine.obfuscateText(new ObfuscationRequest("121"));

    assertEquals(StaticStarTextReplacer.OBFUSCATED + "1", obfuscationResponse.getObfuscatedText());
  }

  @Test
  public void obfuscationRightConflictResolveTest() {
    configuration.setSensitiveRegularExpressions(getConflictedSensitiveRegularExpressions(1, 2));
    ObfuscationResponse obfuscationResponse =
        obfuscationEngine.obfuscateText(new ObfuscationRequest("121"));

    assertEquals(1 + StaticStarTextReplacer.OBFUSCATED, obfuscationResponse.getObfuscatedText());
  }

  private List<SensitiveRegularExpression> getConflictedSensitiveRegularExpressions(
      int leftImportance, int rightImportance) {
    return Arrays.asList(
        new SensitiveRegularExpression(1, "Left", Pattern.compile("12"), leftImportance),
        new SensitiveRegularExpression(2, "Right", Pattern.compile("21"), rightImportance));
  }

  private static class NoOpConfigurationProvider implements ConfigurationProvider {

    @Override
    public void uploadConfiguration(Configuration configuration) {}

    @Override
    public void storeConfiguration(Configuration configuration) {}

    @Override
    public void restoreConfiguration(Configuration configuration) {}
  }
}
