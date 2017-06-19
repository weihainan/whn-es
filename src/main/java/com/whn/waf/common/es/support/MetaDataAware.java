package com.whn.waf.common.es.support;

import org.elasticsearch.search.highlight.HighlightField;

import java.util.Map;

/**
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */
public interface MetaDataAware {

    void setIndex(String index);

    void setType(String type);

    void setScore(Float score);

    void setHighlight(Map<String, HighlightField> highlightFields);
}
