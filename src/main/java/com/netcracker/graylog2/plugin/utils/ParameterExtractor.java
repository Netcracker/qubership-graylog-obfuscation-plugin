package com.netcracker.graylog2.plugin.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ParameterExtractor {

    private final Map<String, Object> parameters;

    public ParameterExtractor(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Optional<Boolean> getBoolean(String parameterName) {
        return getParameter(parameterName, Boolean.class);
    }

    public int getRequiredInteger(String parameterName) {
        return getRequiredParameter(parameterName, Integer.class);
    }

    public String getRequiredString(String parameterName) {
        return getRequiredParameter(parameterName, String.class);
    }

    public Pattern getRequiredPattern(String parameterName) {
        String rawPattern = getRequiredString(parameterName);

        try {
            return Pattern.compile(rawPattern);
        } catch (PatternSyntaxException exception) {
            throw new ParameterException("The pattern \"" + rawPattern + "\" have invalid syntax");
        }
    }

    public <T> T getRequiredParameter(String parameterName, Class<T> parameterType) {
        Optional<T> optionalParameter = getParameter(parameterName, parameterType);
        if (optionalParameter.isPresent()) {
            return optionalParameter.get();
        } else {
            if (isParameterExists(parameterName)) {
                throw new ParameterException("The parameter \"" + parameterName + "\" is empty");
            } else {
                throw new ParameterException("The required parameter \"" + parameterName + "\" " +
                                             "is not found in parameters");
            }
        }
    }

    public Optional<String> getString(String parameterName) {
        return getParameter(parameterName, String.class);
    }

    public <T> Optional<T> getParameter(String parameterName, Class<T> parameterType) {
        if (isParameterExists(parameterName)) {
            Object object = parameters.get(parameterName);
            if (parameterType.isInstance(object)) {
                return Optional.of(parameterType.cast(object));
            }
        }

        return Optional.empty();
    }

    public boolean isParameterExists(String parameterName) {
        return parameters.containsKey(parameterName);
    }

    @SuppressWarnings("rawtypes")
    public <T> Optional<List<T>> getList(String parameterName, Class<T> subtype) {
        Optional<Collection> optionalCollection = getParameter(parameterName, Collection.class);
        if (optionalCollection.isPresent()) {
            List<T> resultList = Lists.newArrayList();

            Collection anyCollection = optionalCollection.get();
            for (Object rawObject : anyCollection) {
                if (!subtype.isInstance(rawObject)) {
                    String subtypeName = subtype.getSimpleName();
                    throw new ParameterException("The element in collection of " + subtypeName + " " +
                                                 "is not " + subtypeName + ". " +
                                                 "Element type: " + getClass(rawObject));
                } else {
                    resultList.add(subtype.cast(rawObject));
                }
            }

            return Optional.of(resultList);
        }

        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    public <K, V> Optional<List<Map<K, V>>> getListOfMap(String parameterName,
                                                         Class<K> keySubtype,
                                                         Class<V> valueSubtype) {
        Optional<Collection> optionalCollection = getParameter(parameterName, Collection.class);
        if (optionalCollection.isPresent()) {
            Collection collection = optionalCollection.get();
            List<Map<K, V>> resultList = Lists.newArrayList();

            for (Object rawMap : collection) {
                if (rawMap instanceof Map) {
                    Map<K, V> resultMap = Maps.newHashMap();

                    Map map = (Map) rawMap;
                    for (Object rawEntry : map.entrySet()) {
                        Entry entry = (Entry) rawEntry;
                        Object rawKey = entry.getKey();
                        Object rawValue = entry.getValue();

                        K key;
                        V value;
                        if (keySubtype.isInstance(rawKey)) {
                            key = keySubtype.cast(rawKey);
                        } else {
                            String keyClassName = keySubtype.getSimpleName();
                            throw new ParameterException("The key in the map of " +
                                                         "\"" + keyClassName + "\" keys " +
                                                         "is not " + keyClassName + ". " +
                                                         "Key type: " + getClass(rawKey));
                        }

                        if (valueSubtype.isInstance(rawValue)) {
                            value = valueSubtype.cast(rawValue);
                        } else if (rawValue == null) {
                            value = null;
                        } else {
                            String valueClassName = valueSubtype.getSimpleName();
                            throw new ParameterException("The value in the map of " +
                                                         "\"" + valueClassName + "\" values " +
                                                         "is not " + valueClassName + ". " +
                                                         "Value type: " + getClass(rawValue));
                        }

                        resultMap.put(key, value);
                    }

                    resultList.add(resultMap);
                } else {
                    throw new ParameterException("The element in collection of maps is not map. " +
                                                 "Element type: " + getClass(rawMap));
                }
            }

            return Optional.of(resultList);
        }

        return Optional.empty();
    }

    private String getClass(Object object) {
        if (object != null) {
            return object.getClass().getName();
        } else {
            return "null";
        }
    }
}

