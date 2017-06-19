package com.whn.waf.common.es.repository;


import com.whn.waf.common.es.domain.ESCommonDomain;
import com.whn.waf.common.es.supprot.CustomListParam;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

public interface ESCommonRepositoryEnhance {
    Object queryForRest(String[] indexes, String[] types, HashMap<String, Object> query);

    Object aliases(Object actions);

    Object getAliases(String alias);

    Map getMappingByAlias(String alias, String type);

    void deleteByQuery(String index, String type, CustomListParam listParam);

    void deleteByRawQuery(String index, String type, HashMap<String, Object> query);

    Map getMappingByAlias(String alias);

    boolean addAlias(String index, String aliasName);

    boolean removeAlias(String index, String aliasName);

    Object reIndex(String source, String dest);

    void clearIndex(String index, String type);

    Page<ESCommonDomain> queryAll(BoolQueryBuilder queryBuilder, String index, String type, CustomListParam listParam);

    void deleteByQuery(String index, String type, BoolQueryBuilder queryBuilder);

    void refresh(String index);
}
