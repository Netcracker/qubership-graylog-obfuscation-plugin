package com.netcracker.graylog2.plugin.processor;

import com.netcracker.graylog2.plugin.obfuscation.ObfuscationProcessor;
import org.graylog2.plugin.Messages;
import org.graylog2.plugin.messageprocessors.MessageProcessor;

import javax.inject.Inject;

public class ObfuscationMessageProcessor implements MessageProcessor {

    private final ObfuscationProcessor obfuscationProcessor;

    @Inject
    public ObfuscationMessageProcessor(ObfuscationProcessor obfuscationProcessor) {
        this.obfuscationProcessor = obfuscationProcessor;
    }

    @Override
    public Messages process(Messages messages) {
        obfuscationProcessor.obfuscate(messages);
        return messages;
    }

    public static class ObfuscationDescriptor implements Descriptor {

        @Override
        public String name() {
            return "Message Obfuscator";
        }

        @Override
        public String className() {
            return ObfuscationMessageProcessor.class.getCanonicalName();
        }
    }
}

