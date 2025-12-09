package com.netcracker.graylog2.plugin.obfuscation.configuration;

import com.google.common.collect.ImmutableList;
import com.netcracker.graylog2.plugin.obfuscation.RegularExpression;
import com.netcracker.graylog2.plugin.obfuscation.SensitiveRegularExpression;
import com.netcracker.graylog2.plugin.obfuscation.replace.TextReplacer;
import com.netcracker.graylog2.plugin.obfuscation.replace.TextReplacers;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

//TODO: Try to use Configuration as Persistent object in Graylog terms
@Singleton
public class Configuration {

    private volatile boolean isObfuscationEnabled = false;

    private volatile List<SensitiveRegularExpression> sensitiveRegularExpressions = Collections.emptyList();

    private volatile List<RegularExpression> whiteRegularExpressions = Collections.emptyList();

    private volatile List<String> fieldNames = Collections.emptyList();

    private volatile List<String> streamTitles = Collections.emptyList();

    private volatile TextReplacer textReplacer = TextReplacers.DEFAULT;

    public boolean isObfuscationEnabled() {
        return isObfuscationEnabled;
    }

    public void setObfuscationEnabled(boolean obfuscationEnabled) {
        isObfuscationEnabled = obfuscationEnabled;
    }

    public List<SensitiveRegularExpression> getSensitiveRegularExpressions() {
        return sensitiveRegularExpressions;
    }

    public void setSensitiveRegularExpressions(List<SensitiveRegularExpression> sensitiveRegularExpressions) {
        this.sensitiveRegularExpressions = ImmutableList.copyOf(Objects.requireNonNull(sensitiveRegularExpressions));
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = ImmutableList.copyOf(Objects.requireNonNull(fieldNames));
    }

    public List<String> getStreamTitles() {
        return streamTitles;
    }

    public void setStreamTitles(List<String> streamNames) {
        this.streamTitles = ImmutableList.copyOf(Objects.requireNonNull(streamNames));
    }

    public TextReplacer getTextReplacer() {
        return textReplacer;
    }

    public void setTextReplacer(TextReplacer textReplacer) {
        this.textReplacer = Objects.requireNonNull(textReplacer);
    }

    public List<RegularExpression> getWhiteRegularExpressions() {
        return whiteRegularExpressions;
    }

    public void setWhiteRegularExpressions(List<RegularExpression> whiteRegularExpressions) {
        this.whiteRegularExpressions = ImmutableList.copyOf(Objects.requireNonNull(whiteRegularExpressions));
    }
}

