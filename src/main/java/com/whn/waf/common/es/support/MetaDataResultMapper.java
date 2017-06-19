package com.whn.waf.common.es.support;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.DefaultEntityMapper;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.mapping.context.MappingContext;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */
public class MetaDataResultMapper extends DefaultResultMapper {

    public MetaDataResultMapper(
            MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext,
            EntityMapper entityMapper) {
        super(mappingContext, entityMapper);
    }

    public MetaDataResultMapper() {
        super(new DefaultEntityMapper());
    }

    @Override
    public <T> Page<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
        Page<T> result = super.mapResults(response, clazz, pageable);
        if (!MetaDataAware.class.isAssignableFrom(clazz)) {
            return result;
        }
        Iterator<T> it = result.iterator();
        for (SearchHit hit : response.getHits()) {
            if (null == hit) {
                continue;
            }
            setMetaData((MetaDataAware) it.next(), hit.getIndex(), hit.getType(),
                    hit.getScore(), hit.getHighlightFields());
        }
        return result;
    }

    @Override
    public <T> T mapResult(GetResponse response, Class<T> clazz) {
        T result = super.mapResult(response, clazz);
        if (MetaDataAware.class.isAssignableFrom(clazz) && null != result) {
            setMetaData((MetaDataAware) result, response.getIndex(), response.getType());
        }
        return result;
    }

    @Override
    public <T> LinkedList<T> mapResults(MultiGetResponse responses, Class<T> clazz) {
        LinkedList<T> result = super.mapResults(responses, clazz);
        if (!MetaDataAware.class.isAssignableFrom(clazz)) {
            return result;
        }
        Iterator<T> it = result.iterator();
        for (MultiGetItemResponse response : responses) {
            if (null == response) {
                continue;
            }
            setMetaData((MetaDataAware) it.next(), response.getIndex(), response.getType());
        }
        return result;
    }

    private void setMetaData(MetaDataAware next, String index, String type) {
        next.setIndex(index);
        next.setType(type);
    }

    private void setMetaData(MetaDataAware next, String index, String type, Float score) {
        next.setIndex(index);
        next.setType(type);
        next.setScore(score);
    }

    private void setMetaData(MetaDataAware next, String index, String type,
                             Float score, Map<String, HighlightField> highlightFields) {
        next.setIndex(index);
        next.setType(type);
        next.setScore(score);
        next.setHighlight(highlightFields);
    }
}
