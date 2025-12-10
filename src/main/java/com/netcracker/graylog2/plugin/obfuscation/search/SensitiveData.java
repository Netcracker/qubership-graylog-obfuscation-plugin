package com.netcracker.graylog2.plugin.obfuscation.search;

import java.util.Objects;

public class SensitiveData {

    private final int start;

    private final int end;

    private final String sensitiveText;

    private final Finder finder;

    public SensitiveData(int start, int end, String sensitiveText, Finder finder) {
        this.start = rangeCheck(start);
        this.end = rangeCheck(end);
        this.sensitiveText = sensitiveTextCheck(sensitiveText, start, end);
        this.finder = Objects.requireNonNull(finder);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getSensitiveText() {
        return sensitiveText;
    }

    public Finder getFinder() {
        return finder;
    }

    private int rangeCheck(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("The range cannot be less, than 0");
        }

        return value;
    }

    private String sensitiveTextCheck(String sensitiveText, int start, int end) {
        int length = end - start;
        if (Objects.isNull(sensitiveText) || sensitiveText.length() != length) {
            throw new IllegalArgumentException("Illegal sensitive data length. " +
                                               "The text length is " + sensitiveText.length() + ", " +
                                               "but length in source text is " + length);
        }

        return sensitiveText;
    }
}

