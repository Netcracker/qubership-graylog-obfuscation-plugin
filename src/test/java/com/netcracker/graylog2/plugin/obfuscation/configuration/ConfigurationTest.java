package com.netcracker.graylog2.plugin.obfuscation.configuration;

import com.carlosbecker.guice.GuiceModules;
import com.carlosbecker.guice.GuiceTestRunner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netcracker.graylog2.plugin.TestObfuscationModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Map;

@RunWith(GuiceTestRunner.class)
@GuiceModules(TestObfuscationModule.class)
public class ConfigurationTest {

    private Map<String, Object> configurationMap = ImmutableMap.<String, Object>builder()
            .put("is-obfuscation-enabled", true)
            .put("field-names", ImmutableList.of("message", "other"))
            .build();

    @Inject
    private ConfigurationSerializer configurationSerializer;

    @Test
    public void serializeDefaultConfigurationTest() {

    }

    @Test
    public void deserializeDefaultConfigurationTest() {
        Configuration configuration = new Configuration();
        configurationSerializer.deserialize(configuration, configurationMap);
    }
}

