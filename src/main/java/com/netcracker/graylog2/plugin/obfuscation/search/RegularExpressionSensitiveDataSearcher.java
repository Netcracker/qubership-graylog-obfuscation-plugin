package com.netcracker.graylog2.plugin.obfuscation.search;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationRequest;
import com.netcracker.graylog2.plugin.obfuscation.SensitiveRegularExpression;
import com.netcracker.graylog2.plugin.obfuscation.WhiteListService;
import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RegularExpressionSensitiveDataSearcher implements SensitiveDataSearcher {

  public static final String SEARCH_TYPE = "RegularExpression";

  private final Configuration configuration;

  private final WhiteListService whiteListService;

  @Inject
  public RegularExpressionSensitiveDataSearcher(
      Configuration configuration, WhiteListService whiteListService) {
    this.configuration = Objects.requireNonNull(configuration);
    this.whiteListService = Objects.requireNonNull(whiteListService);
  }

  @Override
  public List<SensitiveData> search(ObfuscationRequest request) {
    List<SensitiveData> list = new ArrayList<>(5);
    String sourceText = request.getSourceText();

    List<SensitiveRegularExpression> regularExpressions =
        configuration.getSensitiveRegularExpressions();
    for (SensitiveRegularExpression regularExpression : regularExpressions) {
      Pattern pattern = regularExpression.getPattern();
      Matcher matcher = pattern.matcher(sourceText);
      while (matcher.find()) {
        int start = matcher.start();
        int end = matcher.end();
        if (start != end) {
          String sensitiveText = sourceText.substring(start, end);
          if (!whiteListService.isWhiteWord(sensitiveText)) {
            list.add(new SensitiveData(start, end, sensitiveText, regularExpression));
          }
        }
      }
    }

    if (!list.isEmpty()) {
      return list;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public String getSearchType() {
    return SEARCH_TYPE;
  }

  @Override
  public List<Finder> getFinders() {
    List<SensitiveRegularExpression> regularExpressions =
        configuration.getSensitiveRegularExpressions();
    return regularExpressions.stream().map(Finder.class::cast).collect(Collectors.toList());
  }
}
