package com.netcracker.graylog2.plugin.obfuscation;

import com.netcracker.graylog2.plugin.obfuscation.configuration.Configuration;
import com.netcracker.graylog2.plugin.obfuscation.configuration.ConfigurationService;
import com.netcracker.graylog2.plugin.obfuscation.replace.TextReplacer;
import com.netcracker.graylog2.plugin.obfuscation.search.SensitiveData;
import com.netcracker.graylog2.plugin.obfuscation.search.SensitiveDataSearcher;
import org.apache.commons.collections4.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Singleton
public class ObfuscationEngine {

    private final ConfigurationService configurationService;

    private final Set<SensitiveDataSearcher> searchers;

    private final SensitiveDataResolver sensitiveDataResolver;

    @Inject
    public ObfuscationEngine(ConfigurationService configurationService,
                             Set<SensitiveDataSearcher> searchers,
                             SensitiveDataResolver sensitiveDataResolver) {
        this.configurationService = Objects.requireNonNull(configurationService);
        this.searchers = Objects.requireNonNull(searchers);
        this.sensitiveDataResolver = Objects.requireNonNull(sensitiveDataResolver);
    }

    public ObfuscationResponse obfuscateText(ObfuscationRequest request) throws ObfuscationException {
        Configuration configuration = configurationService.getCurrentConfiguration();

        if (request.isNotEmpty() && configuration.isObfuscationEnabled()) {
            List<List<SensitiveData>> searchResults = new ArrayList<>(searchers.size());
            for (SensitiveDataSearcher searcher : searchers) {
                List<SensitiveData> searchResult = searcher.search(request);
                if (CollectionUtils.isNotEmpty(searchResult)) {
                    searchResults.add(searchResult);
                }
            }

            if (!searchResults.isEmpty()) {
                List<SensitiveData> sensitiveData = joinResults(request, searchResults);
                String obfuscatedText = updateSourceText(request, sensitiveData);

                return new ObfuscationResponse(obfuscatedText, sensitiveData);
            }
        }

        return new ObfuscationResponse(request.getSourceText(), Collections.emptyList());
    }

    private List<SensitiveData> joinResults(ObfuscationRequest request, List<List<SensitiveData>> searchResults) {
        List<SensitiveData> sensitiveData = new ArrayList<>();

        for (List<SensitiveData> searchResult : searchResults) {
            sensitiveData.addAll(searchResult);
        }

        return sensitiveDataResolver.resolveConflicts(request, sensitiveData);
    }

    private String updateSourceText(ObfuscationRequest request, List<SensitiveData> sensitiveDataList) {
        String sourceText = request.getSourceText();
        StringBuilder stringBuilder = new StringBuilder(sourceText.length());

        Configuration configuration = configurationService.getCurrentConfiguration();
        TextReplacer textReplacer = configuration.getTextReplacer();

        int index = 0;
        for (SensitiveData sensitiveData : sensitiveDataList) {
            int start = sensitiveData.getStart();
            if (index != start) {
                stringBuilder.append(sourceText, index, start);
            }

            String obfuscatedText = textReplacer.replace(sensitiveData.getSensitiveText(), sensitiveData.getFinder());
            stringBuilder.append(obfuscatedText);
            index = sensitiveData.getEnd();
        }

        if (index != sourceText.length()) {
            stringBuilder.append(sourceText, index, sourceText.length());
        }

        return stringBuilder.toString();
    }
}

