package com.netcracker.graylog2.plugin.obfuscation.configuration;

import com.netcracker.graylog2.plugin.obfuscation.ObfuscationSystemException;
import com.netcracker.graylog2.plugin.utils.ResourceLoader;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Singleton
public class YamlFileConfigurationProvider implements ConfigurationProvider {

    public static final String CONFIGURATION_FILE = "configuration/default-configuration.yml";

    public static final Charset CONFIGURATION_CHARSET = StandardCharsets.UTF_8;

    private final ConfigurationSerializer configurationSerializer;

    private final ResourceLoader resourceLoader;

    private final Map<String, Object> defaultParameters;

    @Inject
    public YamlFileConfigurationProvider(ConfigurationSerializer configurationSerializer,
                                         ResourceLoader resourceLoader) {
        this.configurationSerializer = Objects.requireNonNull(configurationSerializer);
        this.resourceLoader = Objects.requireNonNull(resourceLoader);
        this.defaultParameters = getDefaultConfiguration();
    }

    @Override
    public void uploadConfiguration(Configuration configuration) {
        configurationSerializer.deserialize(configuration, defaultParameters);
    }

    @Override
    public void storeConfiguration(Configuration configuration) {
        //No-op
    }

    @Override
    public void restoreConfiguration(Configuration configuration) {
        //no-op
    }

    private Map<String, Object> getDefaultConfiguration() {
        Yaml yaml = new Yaml();

        try (InputStream resource = resourceLoader.getResource(CONFIGURATION_FILE)) {
            try (Reader reader = new BufferedReader(new InputStreamReader(resource, CONFIGURATION_CHARSET))) {
                Map<String, Map<String, Object>> parameters = yaml.load(reader);
                if (parameters.containsKey("configuration")) {
                    return parameters.get("configuration");
                } else {
                    throw new ObfuscationSystemException("The default parameters is invalid as " +
                                                         "not contained the \"configuration\" top parameter key");
                }
            }
        } catch (IOException exception) {
            throw new ObfuscationSystemException("The unexpected IO exception in reading" +
                                                 " default obfuscation configuration!", exception);
        }
    }
}

