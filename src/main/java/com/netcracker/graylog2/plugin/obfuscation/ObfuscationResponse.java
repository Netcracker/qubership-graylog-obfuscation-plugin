package com.netcracker.graylog2.plugin.obfuscation;

import com.netcracker.graylog2.plugin.obfuscation.search.SensitiveData;
import java.util.List;
import java.util.stream.Collectors;

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

  public List<FoundSensitiveData> getFoundSensitiveData() {
    return foundSensitiveData.stream()
        .map(
            sensitiveData ->
                new FoundSensitiveData(
                    sensitiveData.getStart(),
                    sensitiveData.getEnd(),
                    sensitiveData.getSensitiveText(),
                    sensitiveData.getFinder().getFullName()))
        .collect(Collectors.toList());
  }

  public static class FoundSensitiveData {

    private final int start;

    private final int end;

    private final String sensitiveText;

    private final String finder;

    public FoundSensitiveData(int start, int end, String sensitiveText, String finder) {
      this.start = start;
      this.end = end;
      this.sensitiveText = sensitiveText;
      this.finder = finder;
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

    public String getFinder() {
      return finder;
    }
  }
}
