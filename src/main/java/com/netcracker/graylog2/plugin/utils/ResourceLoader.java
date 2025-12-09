package com.netcracker.graylog2.plugin.utils;

import org.apache.commons.io.IOUtils;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Singleton
public class ResourceLoader {

    public InputStream getResource(String resourcePath) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResourceAsStream(resourcePath);
    }

    public String getResourceAsString(String resourcePath) {
        try (InputStream inputStream = getResource(resourcePath)) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        } catch (IOException exception) {
            throw new UncheckedIOException("Unexpected IO exception in read resource operation", exception);
        }
    }
}

