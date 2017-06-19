package com.whn.waf.common.es.repository;

import com.whn.waf.common.es.domain.ESCommonDomain;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author yanguanyu(290536)
 * @since 0.1 created on 2017/1/11.
 */
@NoRepositoryBean
public interface ESBaseRepository<T> extends ElasticsearchRepository<T, String> {

    boolean putMapping(String indexName, String type, Object mappings);

    boolean putProjectMapping(Class<ESCommonDomain> clazz);

    Object getMapping(String indexName, String type);

    boolean createIndex(String indexName);

    boolean deleteIndex(String indexName);
}
