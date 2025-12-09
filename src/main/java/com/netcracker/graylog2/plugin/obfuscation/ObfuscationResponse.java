package com.netcracker.graylog2.plugin.obfuscation;

import com.netcracker.graylog2.plugin.obfuscation.search.SensitiveData;

import java.util.List;

public class ObfuscationResponse {

    private final String obfuscatedText;

    private final List<SensitiveData> foundSensitiveData;

    public ObfuscationResponse(String obfuscatedText, List<SensitiveData> foundSensitiveData) {
        this.obfuscatedText = obfuscatedText;
        this.foundSensitiveData = foundSensitiveData;
    }

    public String getObfuscatedText() {
        return obfuscatedText;
    }

    public List<SensitiveData> getFoundSensitiveData() {
        return foundSensitiveData;
    }
}

