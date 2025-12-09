package com.netcracker.graylog2.plugin.obfuscation.replace;

import com.netcracker.graylog2.plugin.obfuscation.search.Finder;

public interface TextReplacer {

    String getName();

    String getExample();

    String replace(String sourceText, Finder finder);
}

