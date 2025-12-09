package com.netcracker.graylog2.plugin.obfuscation.search;

/**
 * Provides the interface for special entities, which can find the sensitive data in plain text
 * The collection of finders is relates with {@link SensitiveDataSearcher}.
 * The finder contains only information about desired data.
 * The sensitive data search are performed by {@link SensitiveDataSearcher}.
 */
public interface Finder {

    /**
     * The ID of finder
     *
     * @return identifier
     */
    int getId();

    /**
     * The meaningful name of finder.
     * Usual it reflects the type of sensitive data.
     *
     * @return name of finder.
     */
    String getName();

    /**
     * The readable name of finder
     *
     * @return full finder name
     */
    String getFullName();

    /**
     * The parameter of finder is related by importance of finder.
     * That parameter is necessary for cases, when in search exists collision and two finders
     * found sensitive values and engine cannot resolve that conflict.
     *
     * @return importance of finder
     */
    int getImportance();

    /**
     * Return the search type. Can be obtained from {@link SensitiveDataSearcher}
     *
     * @return search type
     */
    String getSearchType();
}

