package com.easysoftwareinput.common.config;

import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;

public class MyPropertySourceFactory implements PropertySourceFactory {
    /**
     * parse yaml.
     */
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) throws IOException {
        return new YamlPropertySourceLoader().load(name, encodedResource.getResource()).get(0);
    }
}
