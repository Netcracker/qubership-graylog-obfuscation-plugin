package com.netcracker.graylog2.plugin.obfuscation;

import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;
import org.apache.commons.collections4.CollectionUtils;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.streams.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class StreamMessageFilter implements MessageFilter {

    private final Configuration configuration;

    @Inject
    public StreamMessageFilter(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean isAccepted(Message message) {
        Set<String> streams = getMessageStreamTitles(message);

        if (CollectionUtils.isNotEmpty(streams)) {
            List<String> streamTitles = configuration.getStreamTitles();
            for (String streamTitle : streamTitles) {
                if (streams.contains(streamTitle)) {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    private Set<String> getMessageStreamTitles(Message message) {
        Set<Stream> streams = message.getStreams();
        Set<String> streamTitles = new HashSet<>(streams.size());

        for (Stream stream : streams) {
            streamTitles.add(stream.getTitle());
        }

        return streamTitles;
    }
}

