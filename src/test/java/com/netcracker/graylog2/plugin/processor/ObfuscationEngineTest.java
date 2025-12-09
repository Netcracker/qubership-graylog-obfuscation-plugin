package com.netcracker.graylog2.plugin.processor;

import com.carlosbecker.guice.GuiceModules;
import com.carlosbecker.guice.GuiceTestRunner;
import com.google.common.collect.Lists;
import com.netcracker.graylog2.plugin.TestObfuscationModule;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationEngine;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationRequest;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationResponse;
import com.netcracker.graylog2.plugin.obfuscation.SensitiveRegularExpression;
import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;
import com.netcracker.graylog2.plugin.obfuscation.replace.StaticStarTextReplacer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;
import java.util.regex.Pattern;

@RunWith(GuiceTestRunner.class)
@GuiceModules(TestObfuscationModule.class)
public class ObfuscationEngineTest {

    @Inject
    private ObfuscationEngine obfuscationEngine;

    @Inject
    private Configuration configuration;

    @Before
    public void setUp() {
        this.configuration.setObfuscationEnabled(true);
    }

    @Test
    public void simpleSSNObfuscationTest() {
        ObfuscationResponse obfuscationResponse = obfuscationEngine.obfuscateText(new ObfuscationRequest("123-12-1234"));
        Assert.assertEquals("********", obfuscationResponse.getObfuscatedText());
    }

    @Test
    public void obfuscationConflictTest() {
        configuration.setSensitiveRegularExpressions(getConflictedSensitiveRegularExpressions(1, 1));
        ObfuscationResponse obfuscationResponse = obfuscationEngine.obfuscateText(new ObfuscationRequest("121"));

        Assert.assertEquals(StaticStarTextReplacer.OBFUSCATED, obfuscationResponse.getObfuscatedText());
    }

    @Test
    public void obfuscationLeftConflictResolveTest() {
        configuration.setSensitiveRegularExpressions(getConflictedSensitiveRegularExpressions(2, 1));
        ObfuscationResponse obfuscationResponse = obfuscationEngine.obfuscateText(new ObfuscationRequest("121"));

        Assert.assertEquals(StaticStarTextReplacer.OBFUSCATED + "1", obfuscationResponse.getObfuscatedText());
    }

    @Test
    public void obfuscationRightConflictResolveTest() {
        configuration.setSensitiveRegularExpressions(getConflictedSensitiveRegularExpressions(1, 2));
        ObfuscationResponse obfuscationResponse = obfuscationEngine.obfuscateText(new ObfuscationRequest("121"));

        Assert.assertEquals(1 + StaticStarTextReplacer.OBFUSCATED, obfuscationResponse.getObfuscatedText());
    }

    private List<SensitiveRegularExpression> getConflictedSensitiveRegularExpressions(int leftImportance,
                                                                                      int rightImportance) {
        return Lists.newArrayList(
                new SensitiveRegularExpression(1, "Left", Pattern.compile("12"), leftImportance),
                new SensitiveRegularExpression(2, "Right", Pattern.compile("21"), rightImportance)
        );
    }
}

