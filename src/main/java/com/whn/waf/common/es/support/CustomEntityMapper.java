package com.whn.waf.common.es.support;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.elasticsearch.core.EntityMapper;

import java.io.IOException;

/**
 * 自定义ES实体映射
 *
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */
public class CustomEntityMapper implements EntityMapper {

    private ObjectMapper objectMapper;

    public CustomEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String mapToString(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }

    @Override
    public <T> T mapToObject(String source, Class<T> clazz) throws IOException {
        return objectMapper.readValue(source, clazz);
    }
}