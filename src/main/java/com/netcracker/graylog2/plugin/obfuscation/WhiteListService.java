package com.netcracker.graylog2.plugin.obfuscation;

import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.regex.Pattern;

@Singleton
public class WhiteListService {

    private final Configuration configuration;

    @Inject
    public WhiteListService(Configuration configuration) {
        this.configuration = configuration;
    }

    public boolean isWhiteWord(String anyText) {
        List<RegularExpression> whiteRegularExpressions = configuration.getWhiteRegularExpressions();
        for (RegularExpression whiteRegularExpression : whiteRegularExpressions) {
            Pattern pattern = whiteRegularExpression.getPattern();
            if (pattern.matcher(anyText).matches()) {
                return true;
            }
        }

        return false;
    }
}

