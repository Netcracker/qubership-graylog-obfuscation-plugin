package com.netcracker.graylog2.plugin.obfuscation.configuration;

/**
 * Interface provides access to store and re-store configuration for plugin
 */
public interface ConfigurationProvider {

    /**
     * Re-store configuration and fill the configuration object
     *
     * @param configuration current configuration
     */
    void uploadConfiguration(Configuration configuration);

    /**
     * Store configuration object
     *
     * @param configuration current configuration
     */
    void storeConfiguration(Configuration configuration);

    void restoreConfiguration(Configuration configuration);
}

