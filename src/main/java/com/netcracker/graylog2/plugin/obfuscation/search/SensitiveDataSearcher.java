package com.netcracker.graylog2.plugin.obfuscation.search;

import com.netcracker.graylog2.plugin.obfuscation.ObfuscationRequest;

import java.util.List;

/**
 * The interface provides methods by sensitive data search in plain text.
 */
public interface SensitiveDataSearcher {

    /**
     * Perform search sensitive data in {@param data}.
     *
     * @param request the obfuscation request with information
     * @return list of founded sensitive data.
     */
    List<SensitiveData> search(ObfuscationRequest request);

    /**
     * The type of sensitive data searches
     *
     * @return search type
     */
    String getSearchType();

    /**
     * Get the all finders are related with current searcher
     *
     * @return list of finders
     */
    List<Finder> getFinders();
}

