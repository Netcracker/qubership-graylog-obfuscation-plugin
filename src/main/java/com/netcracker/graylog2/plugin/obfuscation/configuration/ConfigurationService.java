package com.netcracker.graylog2.plugin.obfuscation.configuration;

import com.google.common.collect.Lists;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationSystemException;
import com.netcracker.graylog2.plugin.obfuscation.RegularExpression;
import com.netcracker.graylog2.plugin.obfuscation.SensitiveRegularExpression;
import com.netcracker.graylog2.plugin.utils.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationService.class);

    private final Configuration currentConfiguration;

    private final ConfigurationProvider defaultConfigurationProvider;

    private final ConfigurationProvider configurationProvider;

    private final ConfigurationSerializer configurationSerializer;

    @Inject
    public ConfigurationService(Configuration currentConfiguration,
                                ConfigurationProvider configurationProvider,
                                ConfigurationSerializer configurationSerializer,
                                ResourceLoader resourceLoader) {
        this.currentConfiguration = currentConfiguration;
        this.configurationProvider = configurationProvider;
        this.configurationSerializer = configurationSerializer;
        this.defaultConfigurationProvider = new YamlFileConfigurationProvider(configurationSerializer, resourceLoader);
    }

    @Inject
    //@PostConstruct
    public void initialize() {
        try {
            configurationProvider.uploadConfiguration(this.currentConfiguration);
        } catch (ObfuscationSystemException exception) {
            log.error("The configuration cannot be upload. Reset to default configuration", exception);
            resetConfiguration();
        }
    }

    public ConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }

    public synchronized void resetConfiguration() {
        defaultConfigurationProvider.uploadConfiguration(currentConfiguration);
        configurationProvider.storeConfiguration(currentConfiguration);
    }

    public synchronized void installConfiguration(Map<String, Object> configurationParameters) {
        configurationSerializer.deserialize(currentConfiguration, configurationParameters);
        configurationProvider.storeConfiguration(currentConfiguration);
    }

    public Map<String, Object> getSerializedConfiguration() {
        return configurationSerializer.serialize(currentConfiguration);
    }

    public synchronized void synchronizeConfiguration(SynchronizationMode syncMode) {
        switch (syncMode) {
            case ONLY_CREATE:
            case FORCE_UPDATE:
                Configuration defaultConfiguration = new Configuration();
                defaultConfigurationProvider.uploadConfiguration(defaultConfiguration);

                synchronizedFiledNames(currentConfiguration, defaultConfiguration, syncMode);
                synchronizedStreamIds(currentConfiguration, defaultConfiguration, syncMode);
                synchronizedSensitiveRegularExpressions(currentConfiguration, defaultConfiguration, syncMode);
                synchronizedWhiteRegularExpressions(currentConfiguration, defaultConfiguration, syncMode);

                configurationProvider.storeConfiguration(currentConfiguration);
                break;
            case SKIP:
                break;
            default:
                throw new ObfuscationSystemException("Unexpected synchronization mode: " + syncMode);
        }
    }

    private void synchronizedSensitiveRegularExpressions(Configuration currentConfiguration,
                                                         Configuration defaultConfiguration,
                                                         SynchronizationMode synchronizationMode) {
        List<SensitiveRegularExpression> updatedExpressions = synchronizeLists(
                currentConfiguration.getSensitiveRegularExpressions(),
                defaultConfiguration.getSensitiveRegularExpressions(),
                synchronizationMode);

        currentConfiguration.setSensitiveRegularExpressions(updatedExpressions);
    }

    private void synchronizedWhiteRegularExpressions(Configuration currentConfiguration,
                                                     Configuration defaultConfiguration,
                                                     SynchronizationMode synchronizationMode) {
        List<RegularExpression> updatedExpressions = synchronizeLists(
                currentConfiguration.getWhiteRegularExpressions(),
                defaultConfiguration.getWhiteRegularExpressions(),
                synchronizationMode);

        currentConfiguration.setWhiteRegularExpressions(updatedExpressions);
    }

    private <T> List<T> synchronizeLists(List<T> sourceList, List<T> defaultList, SynchronizationMode syncMode) {
        List<T> updatedExpressions = Lists.newArrayList();

        for (T item : sourceList) {
            Optional<T> optional = getFromList(defaultList, item);
            if (optional.isPresent()) {
                T defaultItem = optional.get();
                if (syncMode == SynchronizationMode.FORCE_UPDATE) {
                    updatedExpressions.add(defaultItem);
                } else if (syncMode == SynchronizationMode.ONLY_CREATE) {
                    updatedExpressions.add(item);
                } else {
                    throw new ObfuscationSystemException("Unexpected synchronization mode: " + syncMode);
                }
            } else {
                updatedExpressions.add(item);
            }
        }

        for (T defaultExpression : defaultList) {
            if (!updatedExpressions.contains(defaultExpression)) {
                updatedExpressions.add(defaultExpression);
            }
        }

        return updatedExpressions;
    }

    private void synchronizedFiledNames(Configuration currentConfiguration,
                                        Configuration defaultConfiguration,
                                        SynchronizationMode syncMode) {
        List<String> fieldNames = synchronizeLists(
                currentConfiguration.getFieldNames(),
                defaultConfiguration.getFieldNames(),
                syncMode);

        currentConfiguration.setFieldNames(fieldNames);
    }

    private void synchronizedStreamIds(Configuration currentConfiguration,
                                       Configuration defaultConfiguration,
                                       SynchronizationMode syncMode) {
        List<String> streamIds = synchronizeLists(
                currentConfiguration.getStreamTitles(),
                defaultConfiguration.getStreamTitles(),
                syncMode);

        currentConfiguration.setStreamTitles(streamIds);
    }

    private <T> Optional<T> getFromList(List<T> expressions, T item) {
        for (T expression : expressions) {
            if (expression.equals(item)) {
                return Optional.of(expression);
            }
        }

        return Optional.empty();
    }

    public Configuration getCurrentConfiguration() {
        return currentConfiguration;
    }
}

