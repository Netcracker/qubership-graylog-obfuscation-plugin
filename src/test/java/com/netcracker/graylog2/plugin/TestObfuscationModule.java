package com.netcracker.graylog2.plugin;

import com.google.common.collect.Lists;
import com.netcracker.graylog2.plugin.obfuscation.configuration.ConfigurationProvider;
import com.netcracker.graylog2.plugin.obfuscation.configuration.YamlFileConfigurationProvider;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.streams.StreamService;
import org.mockito.Mockito;

public class TestObfuscationModule extends ObfuscationModule {

    @Override
    protected void configure() {
        super.configure();

        Stream streamMock = Mockito.mock(Stream.class);
        Mockito.when(streamMock.getTitle()).thenReturn("Audit logs");

        StreamService streamServiceMock = Mockito.mock(StreamService.class);
        Mockito.when(streamServiceMock.loadAll()).thenReturn(Lists.newArrayList(streamMock));

        bind(StreamService.class).toInstance(streamServiceMock);
    }

    @Override
    protected void bindConfigurationProvider() {
        bind(ConfigurationProvider.class).to(YamlFileConfigurationProvider.class);
    }

    @Override
    protected void bindRestResources() {
        //no-op
    }
}

