package com.netcracker.graylog2.plugin.obfuscation;

import java.util.Objects;
import java.util.regex.Pattern;

public class RegularExpression {

    private final int id;

    private final String name;

    private final Pattern pattern;

    public RegularExpression(int id, String name, Pattern pattern) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.pattern = Objects.requireNonNull(pattern);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegularExpression that = (RegularExpression) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

