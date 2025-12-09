package com.netcracker.graylog2.plugin;

import com.google.inject.multibindings.Multibinder;
import com.netcracker.graylog2.plugin.obfuscation.MessageFilter;
import com.netcracker.graylog2.plugin.obfuscation.StreamMessageFilter;
import com.netcracker.graylog2.plugin.obfuscation.configuration.ConfigurationProvider;
import com.netcracker.graylog2.plugin.obfuscation.configuration.MongoDatabaseConfigurationProvider;
import com.netcracker.graylog2.plugin.obfuscation.search.RegularExpressionSensitiveDataSearcher;
import com.netcracker.graylog2.plugin.obfuscation.search.SensitiveDataSearcher;
import com.netcracker.graylog2.plugin.processor.ObfuscationMessageProcessor;
import com.netcracker.graylog2.plugin.processor.ObfuscationMessageProcessor.ObfuscationDescriptor;
import com.netcracker.graylog2.plugin.rest.resources.ConfigurationResource;
import com.netcracker.graylog2.plugin.rest.resources.ObfuscationResource;
import org.graylog2.plugin.PluginConfigBean;
import org.graylog2.plugin.PluginModule;

import java.util.Collections;
import java.util.Set;

public class ObfuscationModule extends PluginModule {

    @Override
    public Set<? extends PluginConfigBean> getConfigBeans() {
        return Collections.emptySet();
    }

    @Override
    protected void configure() {
        Multibinder<SensitiveDataSearcher> serviceBinder = Multibinder.newSetBinder(binder(), SensitiveDataSearcher.class);
        serviceBinder.addBinding().to(RegularExpressionSensitiveDataSearcher.class);

        Multibinder<MessageFilter> filterBinder = Multibinder.newSetBinder(binder(), MessageFilter.class);
        filterBinder.addBinding().to(StreamMessageFilter.class);

        addMessageProcessor(ObfuscationMessageProcessor.class, ObfuscationDescriptor.class);
        bindConfigurationProvider();
        bindRestResources();
    }

    protected void bindConfigurationProvider() {
        bind(ConfigurationProvider.class).to(MongoDatabaseConfigurationProvider.class);
    }

    protected void bindRestResources() {
        addRestResource(ConfigurationResource.class);
        addRestResource(ObfuscationResource.class);
    }
}

