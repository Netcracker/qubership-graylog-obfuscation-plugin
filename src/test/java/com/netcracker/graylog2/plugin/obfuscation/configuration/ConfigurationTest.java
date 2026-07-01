package com.netcracker.graylog2.plugin.obfuscation.configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationSystemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationTest {

    private Map<String, Object> configurationMap = ImmutableMap.<String, Object>builder()
            .put("is-obfuscation-enabled", true)
            .put("field-names", ImmutableList.of("message", "other"))
            .build();

    private ConfigurationSerializer configurationSerializer;

    @BeforeEach
    public void setUp() {
        configurationSerializer = new ConfigurationSerializer();
    }

    @Test
    public void serializeDefaultConfigurationTest() {
        Configuration configuration = new Configuration();
        configurationSerializer.deserialize(configuration, configurationMap);

        Map<String, Object> serializedConfiguration = configurationSerializer.serialize(configuration);

        assertEquals(true, serializedConfiguration.get("is-obfuscation-enabled"));
        assertEquals(ImmutableList.of("message", "other"), serializedConfiguration.get("field-names"));
        assertEquals("Static Star Replacer", serializedConfiguration.get("text-replacer"));
    }

    @Test
    public void deserializeDefaultConfigurationTest() {
        Configuration configuration = new Configuration();
        configurationSerializer.deserialize(configuration, configurationMap);

        assertTrue(configuration.isObfuscationEnabled());
        assertEquals(ImmutableList.of("message", "other"), configuration.getFieldNames());
    }

    @Test
    public void deserializeDuplicateFieldNamesTest() {
        Configuration configuration = new Configuration();
        Map<String, Object> invalidConfiguration = ImmutableMap.<String, Object>builder()
                .put("field-names", ImmutableList.of("message", "message"))
                .build();

        assertThrows(ObfuscationSystemException.class,
                () -> configurationSerializer.deserialize(configuration, invalidConfiguration));
    }

    @Test
    public void deserializeInvalidRegularExpressionTest() {
        Configuration configuration = new Configuration();
        Map<String, Object> invalidConfiguration = ImmutableMap.<String, Object>builder()
                .put("sensitive-regular-expressions", ImmutableList.of(ImmutableMap.<String, Object>builder()
                        .put("id", 1)
                        .put("name", "broken")
                        .put("pattern", "[")
                        .put("importance", 1)
                        .build()))
                .build();

        assertThrows(ObfuscationSystemException.class,
                () -> configurationSerializer.deserialize(configuration, invalidConfiguration));
    }
}
