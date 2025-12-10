package com.netcracker.graylog2.plugin.obfuscation;

import org.apache.commons.lang3.StringUtils;

public class ObfuscationRequest {

    private final String sourceText;

    public ObfuscationRequest(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getSourceText() {
        return sourceText;
    }

    public boolean isNotEmpty() {
        return StringUtils.isNotEmpty(sourceText);
    }
}

