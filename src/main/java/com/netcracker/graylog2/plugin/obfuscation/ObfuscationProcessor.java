package com.netcracker.graylog2.plugin.obfuscation;

import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;
import com.netcracker.graylog2.plugin.processor.ObfuscationMessageProcessor;
import org.apache.commons.lang3.StringUtils;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Singleton
public class ObfuscationProcessor {

    private static final Logger log = LoggerFactory.getLogger(ObfuscationMessageProcessor.class);

    private final ObfuscationEngine obfuscationEngine;

    private final Configuration configuration;

    private final Set<MessageFilter> filterChain;

    @Inject
    public ObfuscationProcessor(ObfuscationEngine obfuscationEngine,
                                Configuration configuration,
                                Set<MessageFilter> filterChain) {
        this.obfuscationEngine = Objects.requireNonNull(obfuscationEngine);
        this.configuration = Objects.requireNonNull(configuration);
        this.filterChain = Objects.requireNonNull(filterChain);
    }

    public void obfuscate(Messages messages) {
        if (configuration.isObfuscationEnabled()) {
            processMessages(messages);
        }
    }

    //MessageCollection may filter some messages in iterator by Message#getFilterOut()
    private void processMessages(Messages messages) {
        List<String> fieldNames = configuration.getFieldNames();

        for (Message message : messages) {
            if (isMessageAccepted(message)) {
                for (String fieldName : fieldNames) {
                    Optional<String> optional = extractMessage(message, fieldName);
                    if (optional.isPresent()) {
                        String logMessage = optional.get();
                        if (StringUtils.isNotBlank(logMessage)) {
                            try {
                                ObfuscationRequest obfuscationRequest = new ObfuscationRequest(logMessage);
                                ObfuscationResponse obfuscationResponse = obfuscationEngine.obfuscateText(obfuscationRequest);
                                putMessage(message, obfuscationResponse.getObfuscatedText(), fieldName);
                            } catch (ObfuscationException exception) {
                                log.error("The log message cannot be obfuscation by exception reason", exception);
                                message.removeField(fieldName);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isMessageAccepted(Message message) {
        for (MessageFilter messageFilter : filterChain) {
            if (!messageFilter.isAccepted(message)) {
                return false;
            }
        }

        return true;
    }

    private Optional<String> extractMessage(Message message, String fieldName) {
        if (message.hasField(fieldName)) {
            Object field = message.getField(fieldName);
            if (field instanceof String) {
                String logMessage = (String) field;
                return Optional.of(logMessage);
            } else {
                if (field != null) {
                    log.error("Inconsistent field " + fieldName + " type from log message. " +
                              "Expected type: String. Actual type: " + field.getClass());
                } else {
                    log.warn("Field " + fieldName + " from log message is null");
                }
            }
        }

        return Optional.empty();
    }

    private void putMessage(Message message, String obfuscatedMessage, String fieldName) {
        message.addField(fieldName, obfuscatedMessage);
    }
}

