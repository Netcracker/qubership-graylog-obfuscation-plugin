package com.netcracker.graylog2.plugin;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus.Capability;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class ObfuscationPluginMetaData implements PluginMetaData {

    private static final String PLUGIN_PROPERTIES = "com.netcracker.graylog-obfuscation-plugin/graylog-plugin.properties";

    private static final Version UNKNOWN_VERSION = Version.from(0, 0, 0, "unknown");

    @Override
    public String getUniqueId() {
        return ObfuscationPlugin.class.getName();
    }

    @Override
    public String getName() {
        return "ObfuscationPlugin";
    }

    @Override
    public String getAuthor() {
        return "Netcracker";
    }

    @Override
    public URI getURL() {
        return URI.create("https://github.com/Netcracker/qubership-graylog-obfuscation-plugin");
    }

    @Override
    public Version getVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "version", UNKNOWN_VERSION);
    }

    @Override
    public String getDescription() {
        return "Plugin for obfuscation input messages";
    }

    @Override
    public Version getRequiredVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "graylog.version", UNKNOWN_VERSION);
    }

    @Override
    public Set<Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}

