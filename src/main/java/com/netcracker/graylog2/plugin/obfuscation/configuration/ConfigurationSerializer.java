package com.netcracker.graylog2.plugin.obfuscation.configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netcracker.graylog2.plugin.obfuscation.ObfuscationSystemException;
import com.netcracker.graylog2.plugin.obfuscation.RegularExpression;
import com.netcracker.graylog2.plugin.obfuscation.SensitiveRegularExpression;
import com.netcracker.graylog2.plugin.obfuscation.replace.TextReplacer;
import com.netcracker.graylog2.plugin.obfuscation.replace.TextReplacers;
import com.netcracker.graylog2.plugin.utils.ParameterException;
import com.netcracker.graylog2.plugin.utils.ParameterExtractor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Singleton
@SuppressWarnings("SameParameterValue")
public class ConfigurationSerializer {

    public Map<String, Object> serialize(Configuration configuration) {
        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("is-obfuscation-enabled", configuration.isObfuscationEnabled());

        List<Map<String, Object>> sensitiveRegularExpressions = Lists.newArrayList();
        for (SensitiveRegularExpression sensitiveRegularExpression : configuration.getSensitiveRegularExpressions()) {
            Map<String, Object> serializedSensitiveRegularExpression = Maps.newHashMap();

            serializedSensitiveRegularExpression.put("id", sensitiveRegularExpression.getId());
            serializedSensitiveRegularExpression.put("name", sensitiveRegularExpression.getName());
            serializedSensitiveRegularExpression.put("pattern", sensitiveRegularExpression.getPattern().pattern());
            serializedSensitiveRegularExpression.put("importance", sensitiveRegularExpression.getImportance());

            sensitiveRegularExpressions.add(serializedSensitiveRegularExpression);
        }
        parameters.put("sensitive-regular-expressions", sensitiveRegularExpressions);

        List<Map<String, Object>> whiteRegularExpressions = Lists.newArrayList();
        for (RegularExpression regularExpression : configuration.getWhiteRegularExpressions()) {
            Map<String, Object> serializedWhiteRegularExpression = Maps.newHashMap();

            serializedWhiteRegularExpression.put("id", regularExpression.getId());
            serializedWhiteRegularExpression.put("name", regularExpression.getName());
            serializedWhiteRegularExpression.put("pattern", regularExpression.getPattern().pattern());

            whiteRegularExpressions.add(serializedWhiteRegularExpression);
        }
        parameters.put("white-regular-expressions", whiteRegularExpressions);

        parameters.put("field-names", configuration.getFieldNames());
        parameters.put("stream-titles", configuration.getStreamTitles());
        parameters.put("text-replacer", configuration.getTextReplacer().getName());

        return parameters;
    }

    public void deserialize(Configuration configuration, Map<String, Object> parameters) {
        ParameterExtractor extractor = new ParameterExtractor(parameters);

        try {
            Optional<Boolean> isObfuscationEnabled = extractor.getBoolean("is-obfuscation-enabled");
            Optional<List<SensitiveRegularExpression>> sensitiveRegularExpressions =
                    extractor.getListOfMap("sensitive-regular-expressions", String.class, Object.class)
                    .map(this::deserializeSensitiveRegularExpressions);
            Optional<List<RegularExpression>> whiteRegularExpressions =
                    extractor.getListOfMap("white-regular-expressions", String.class, Object.class)
                    .map(this::deserializeWhiteRegularExpressions);
            Optional<List<String>> fieldNames = extractor.getList("field-names", String.class)
                    .map(this::deserializeFieldNames);
            Optional<List<String>> streamTitles = extractor.getList("stream-titles", String.class)
                    .map(this::deserializeStreamTitles);
            Optional<TextReplacer> textReplacer = extractor.getString("text-replacer")
                    .map(this::deserializeTextReplacer);

            isObfuscationEnabled.ifPresent(configuration::setObfuscationEnabled);
            sensitiveRegularExpressions.ifPresent(configuration::setSensitiveRegularExpressions);
            whiteRegularExpressions.ifPresent(configuration::setWhiteRegularExpressions);
            fieldNames.ifPresent(configuration::setFieldNames);
            streamTitles.ifPresent(configuration::setStreamTitles);
            textReplacer.ifPresent(configuration::setTextReplacer);
        } catch (ParameterException exception) {
            throw new ObfuscationSystemException("The configuration model is invalid. " +
                                                 "Reason: " + exception.getMessage(), exception);
        }
    }

    private List<SensitiveRegularExpression> deserializeSensitiveRegularExpressions(
            List<Map<String, Object>> rawSensitiveRegularExpressions) {
        List<SensitiveRegularExpression> sensitiveRegularExpressions = Lists.newArrayList();

        for (Map<String, Object> rawSensitiveRegularExpression : rawSensitiveRegularExpressions) {
            ParameterExtractor extractor = new ParameterExtractor(rawSensitiveRegularExpression);

            int id = getId(extractor);
            String name = getName(extractor);
            Pattern pattern = getPattern(extractor);
            int importance = extractor.getRequiredInteger("importance");

            SensitiveRegularExpression regularExpression = new SensitiveRegularExpression(id, name, pattern, importance);
            if (sensitiveRegularExpressions.contains(regularExpression)) {
                throw new ObfuscationSystemException("The sensitive regular expressions " +
                                                     "with id " + id + " already exists");
            } else {
                sensitiveRegularExpressions.add(regularExpression);
            }
        }

        return sensitiveRegularExpressions;
    }

    private List<RegularExpression> deserializeWhiteRegularExpressions(List<Map<String, Object>> rawWhiteRegularExpressions) {
        List<RegularExpression> whiteRegularExpressions = Lists.newArrayList();

        for (Map<String, Object> rawWhiteRegularExpression : rawWhiteRegularExpressions) {
            ParameterExtractor extractor = new ParameterExtractor(rawWhiteRegularExpression);

            int id = getId(extractor);
            String name = getName(extractor);
            Pattern pattern = getPattern(extractor);

            RegularExpression regularExpression = new RegularExpression(id, name, pattern);
            if (whiteRegularExpressions.contains(regularExpression)) {
                throw new ObfuscationSystemException("The white regular expressions " +
                                                     "with id " + id + " already exists");
            } else {
                whiteRegularExpressions.add(regularExpression);
            }
        }

        return whiteRegularExpressions;
    }

    private int getId(ParameterExtractor extractor) {
        int id = extractor.getRequiredInteger("id");
        if (id < 0) {
            throw new ObfuscationSystemException("The id of sensitive regular expressions cannot lower than 0");
        }

        return id;
    }

    private String getName(ParameterExtractor extractor) {
        String name = extractor.getRequiredString("name");
        if (StringUtils.isEmpty(name)) {
            throw new ObfuscationSystemException("The name of sensitive regular expressions cannot be empty");
        }

        return name;
    }

    private Pattern getPattern(ParameterExtractor extractor) {
        Pattern pattern = extractor.getRequiredPattern("pattern");
        if (StringUtils.isEmpty(pattern.pattern())) {
            throw new ObfuscationSystemException("The pattern of sensitive regular expression cannot be empty");
        }

        return pattern;
    }

    private List<String> deserializeStreamTitles(List<String> streamTitles) {
        for (String streamTitle : streamTitles) {
            if (StringUtils.isEmpty(streamTitle)) {
                throw new ObfuscationSystemException("The stream title cannot be empty");
            }
        }

        for (String streamTitle : streamTitles) {
            if (CollectionUtils.select(streamTitles, title -> StringUtils.equals(title, streamTitle)).size() > 1) {
                throw new ObfuscationSystemException("The stream titles have duplicate title \"" + streamTitle + "\"");
            }
        }

        return streamTitles;
    }

    private List<String> deserializeFieldNames(List<String> fieldNames) {
        for (String fieldName : fieldNames) {
            if (StringUtils.isEmpty(fieldName)) {
                throw new ObfuscationSystemException("The field name cannot be empty");
            }
        }

        for (String fieldName : fieldNames) {
            if (CollectionUtils.select(fieldNames, name -> StringUtils.equals(name, fieldName)).size() > 1) {
                throw new ObfuscationSystemException("The field names have duplicate name \"" + fieldName + "\"");
            }
        }

        return fieldNames;
    }

    private TextReplacer deserializeTextReplacer(String textReplacerName) {
        Optional<TextReplacer> optionalTextReplacer = TextReplacers.getByName(textReplacerName);
        if (optionalTextReplacer.isPresent()) {
            return optionalTextReplacer.get();
        } else {
            throw new ObfuscationSystemException("The text replacer with name " +
                                                 "\"" + textReplacerName + "\" is not found");
        }
    }
}

