package com.netcracker.graylog2.plugin.obfuscation;

import com.google.re2j.Pattern;
import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

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
