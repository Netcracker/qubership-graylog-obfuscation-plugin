package com.netcracker.graylog2.plugin;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Collection;
import java.util.Collections;

public class ObfuscationPlugin implements Plugin {

    @Override
    public PluginMetaData metadata() {
        return new ObfuscationPluginMetaData();
    }

    @Override
    public Collection<PluginModule> modules() {
        return Collections.singletonList(new ObfuscationModule());
    }
}

