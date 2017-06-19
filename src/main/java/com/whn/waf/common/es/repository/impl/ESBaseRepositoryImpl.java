package com.whn.waf.common.es.repository.impl;


import com.whn.waf.common.es.domain.ESCommonDomain;
import com.whn.waf.common.es.repository.ESBaseRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;

public class ESBaseRepositoryImpl<T> extends SimpleElasticsearchRepository<T>
        implements ESBaseRepository<T> {


    public ESBaseRepositoryImpl(ElasticsearchEntityInformation<T, String> metadata,
                                ElasticsearchOperations elasticsearchOperation) {
        super(metadata, elasticsearchOperation);
    }

    public boolean putMapping(String indexName, String type, Object mappings) {
        return elasticsearchOperations.putMapping(indexName, type, mappings);
    }

    public boolean putProjectMapping(Class<ESCommonDomain> clazz) {
        return elasticsearchOperations.putMapping(clazz);
    }

    public Object getMapping(String indexName, String type) {
        return elasticsearchOperations.getMapping(indexName, type);
    }

    @Override
    public boolean createIndex(String indexName) {
        return elasticsearchOperations.createIndex(indexName);
    }

    @Override
    public boolean deleteIndex(String indexName) {
        return elasticsearchOperations.deleteIndex(indexName);
    }
}
