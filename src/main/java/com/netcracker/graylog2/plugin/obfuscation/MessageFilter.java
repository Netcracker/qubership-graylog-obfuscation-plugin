package com.netcracker.graylog2.plugin.obfuscation;

import org.graylog2.plugin.Message;

import java.util.function.Predicate;

public interface MessageFilter extends Predicate<Message> {

    boolean isAccepted(Message message);

    @Override
    default boolean test(Message message) {
        return isAccepted(message);
    }
}

