package com.netcracker.graylog2.plugin.obfuscation.replace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Optional;

public class TextReplacers {

    public static final TextReplacer STATIC_STAR_REPLACER = new StaticStarTextReplacer();

    public static final TextReplacer DEFAULT = STATIC_STAR_REPLACER;

    private static final ImmutableMap<String, TextReplacer> REPLACERS = ImmutableMap.<String, TextReplacer>builder()
            .put(STATIC_STAR_REPLACER.getName(), STATIC_STAR_REPLACER)
            .build();

    public static Optional<TextReplacer> getByName(String replacerName) {
        if (REPLACERS.containsKey(replacerName)) {
            TextReplacer textReplacer = REPLACERS.get(replacerName);
            return Optional.of(textReplacer);
        }

        return Optional.empty();
    }

    public static List<TextReplacer> getAllTextReplacers() {
        return ImmutableList.copyOf(REPLACERS.values());
    }
}

