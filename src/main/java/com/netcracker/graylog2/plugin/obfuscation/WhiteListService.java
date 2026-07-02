package com.netcracker.graylog2.plugin.obfuscation;

import com.google.re2j.Pattern;
import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

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
