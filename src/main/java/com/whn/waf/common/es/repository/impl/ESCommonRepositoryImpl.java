package com.whn.waf.common.es.repository.impl;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import com.google.common.collect.UnmodifiableIterator;
import com.whn.waf.common.client.http.WafHttpClient;
import com.whn.waf.common.es.domain.ESCommonDomain;
import com.whn.waf.common.es.repository.ESCommonRepositoryEnhance;
import com.whn.waf.common.es.supprot.CustomListParam;
import com.whn.waf.common.es.supprot.EsQueryUtils;
import com.whn.waf.common.utils.CommonUtil;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.highlight.HighlightBuilder.Field;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.elasticsearch.ElasticsearchException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.AliasQuery;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.sort.SortBuilders.fieldSort;

public class ESCommonRepositoryImpl implements ESCommonRepositoryEnhance {

    private static final Logger logger = LoggerFactory.getLogger(ESCommonRepositoryImpl.class);

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Value("${es.protocol}")
    private String protocol;
    @Value("${es.http.port}")
    private int port;
    @Value("${es.host}")
    private String hostname;

    @Override
    public Page<ESCommonDomain> queryAll(BoolQueryBuilder queryBuilder, String index, String type,
                                         CustomListParam listParam) {
        return query(queryBuilder, new String[]{index}, new String[]{type}, null, null, false, null, listParam);
    }

    @Override
    public Object queryForRest(String[] indexes, String[] types, HashMap<String, Object> query) {
        String index = StringUtils.join(indexes, ",");
        if (StringUtils.isEmpty(index)) {
            index = "_all";
        }
        String type = StringUtils.join(types, ",");
        if (StringUtils.isEmpty(type)) {
            type = "";
        } else {
            type = type + "/";
        }
        return buildClient().postForObject(String.format("%s%s:%s/%s/%s_search", protocol, hostname, port, index, type), query, Object.class);
    }

    private Page<ESCommonDomain> query(BoolQueryBuilder queryBuilder, String[] indexes, String[] types, String searchField, List<String> keywords,
                                       boolean highlight, List<String> highlightFields, CustomListParam listParam) {
        if (StringUtils.isNotBlank(searchField) && keywords != null && keywords.size() != 0) {
            queryBuilder.must(buildKeywordQuery(searchField, keywords));
        }
        Pageable pageable = new PageRequest(listParam.getOffset(), listParam.getPageSize());
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder().withQuery(queryBuilder)
                .withIndices(indexes).withTypes(types).withPageable(pageable);
        if (listParam.getSort() != null) {
            Iterator<Order> iterator = listParam.getSort().iterator();
            while (iterator.hasNext()) {
                Order order = iterator.next();
                searchQueryBuilder.withSort(fieldSort(order.getProperty())
                        .order(order.getDirection().name().equalsIgnoreCase("ASC") ? SortOrder.ASC : SortOrder.DESC));
            }
        }
        if (highlight && highlightFields != null) {
            ArrayList<Field> fields = new ArrayList<Field>();
            for (String h : highlightFields) {
                fields.add(new Field(h).fragmentSize(2000));
            }
            searchQueryBuilder.withHighlightFields(fields.toArray(new Field[0]));
        }
        SearchQuery searchQuery = searchQueryBuilder.build();
        return elasticsearchOperations.queryForPage(searchQuery, ESCommonDomain.class);
    }

    @Override
    public void deleteByQuery(String index, String type, CustomListParam listParam) {
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setIndex(index);
        deleteQuery.setType(type);
        BoolQueryBuilder queryBuilder = boolQuery();
        EsQueryUtils.compositeListParamQuery(queryBuilder, listParam);
        deleteQuery.setQuery(queryBuilder);
        elasticsearchOperations.delete(deleteQuery, ESCommonDomain.class);
    }

    @Override
    public void refresh(String index) {
        elasticsearchOperations.refresh(index);
    }

    @Override
    public void deleteByQuery(String index, String type, BoolQueryBuilder queryBuilder) {
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setIndex(index);
        deleteQuery.setType(type);
        deleteQuery.setQuery(queryBuilder);
        elasticsearchOperations.delete(deleteQuery, ESCommonDomain.class);
        this.refresh(index);
    }

    @Override
    public void deleteByRawQuery(String index, String type, HashMap<String, Object> query) {
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setIndex(index);
        deleteQuery.setType(type);
        deleteQuery.setQuery(QueryBuilders.wrapperQuery(CommonUtil.toJson(query)));
        elasticsearchOperations.delete(deleteQuery, ESCommonDomain.class);
        this.refresh(index);
    }

    /**
     * 和议题有关的查询条件
     *
     * @param queryBuilder
     * @param application
     * @param userId
     * @param listParam
     */
    private void projectSearchParams(BoolQueryBuilder queryBuilder, String application, String userId, CustomListParam listParam) {
        EsQueryUtils.compositeListParamQuery(queryBuilder, listParam);
    }

    private BoolQueryBuilder buildKeywordQuery(String searchField, List<String> keywords) {
        BoolQueryBuilder keywordQueryBuilder = boolQuery();
        for (String keyword : keywords) {
            keywordQueryBuilder = keywordQueryBuilder.should(matchPhraseQuery(searchField, keyword));
        }
        return keywordQueryBuilder;
    }

    //------------------------以下为管理接口--------------------


    @Override
    public Object aliases(Object actions) {
        return buildClient().postForObject(String.format("%s%s:%s/_aliases", protocol, hostname, port), actions, Object.class);
    }

    @Override
    public Object getAliases(String alias) {
        return buildClient().getForObject(String.format("%s%s:%s/%s/_aliases", protocol, hostname, port, alias), Object.class);
    }

    @Override
    public Object reIndex(String source, String dest) {
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> sourceMap = new HashMap<>();
        Map<String, Object> destMap = new HashMap<>();
        sourceMap.put("index", source);
        destMap.put("index", dest);
        param.put("source", sourceMap);
        param.put("dest", destMap);
        return buildClient().postForObject(String.format("%s%s:%s/_reindex", protocol, hostname, port), param, Object.class);
    }

    /**
     * 如果alias对应都个index并且不同index中都拥有这个type，那么就会产生误判，新增alias时候避免发生这个问题。
     */
    @Override
    public Map getMappingByAlias(String alias, String type) {
        Assert.notNull(alias, "No index defined for putMapping()");
        Assert.notNull(type, "No type defined for putMapping()");
        try {
            ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings =
                    elasticsearchOperations.getClient().admin().indices().getMappings(new GetMappingsRequest().indices(alias).types(type))
                            .actionGet().getMappings();
            UnmodifiableIterator<ImmutableOpenMap<String, MappingMetaData>> valuesIt = mappings.valuesIt();
            while (valuesIt.hasNext()) {
                ImmutableOpenMap<String, MappingMetaData> next = valuesIt.next();
                if (next.containsKey(type)) {
                    return next.get(type).getSourceAsMap();
                }
            }
            throw new RuntimeException("no such type in alias");
        } catch (Exception e) {
            throw new ElasticsearchException("Error while getting mapping for alias : " + alias + " type : " + type + " " + e.getMessage());
        }
    }

    @Override
    public Map getMappingByAlias(String alias) {
        Assert.notNull(alias, "No index defined for putMapping()");
        try {
            ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings =
                    elasticsearchOperations.getClient().admin().indices().getMappings(new GetMappingsRequest().indices(alias))
                            .actionGet().getMappings();
            UnmodifiableIterator<ImmutableOpenMap<String, MappingMetaData>> valuesIt = mappings.valuesIt();
            Map<String, Object> _mappings = new HashMap<>();
            while (valuesIt.hasNext()) {
                ImmutableOpenMap<String, MappingMetaData> next = valuesIt.next();
                Iterator<ObjectObjectCursor<String, MappingMetaData>> iterator = next.iterator();
                while (iterator.hasNext()) {
                    ObjectObjectCursor<String, MappingMetaData> next2 = iterator.next();
                    _mappings.put(next2.key, next2.value.getSourceAsMap());
                }
            }
            return _mappings;
        } catch (Exception e) {
            throw new ElasticsearchException("Error while getting mapping for alias : " + alias + e.getMessage());
        }
    }

    @Override
    public boolean addAlias(String index, String aliasName) {
        AliasQuery aliasQuery = new AliasQuery();
        aliasQuery.setAliasName(aliasName);
        aliasQuery.setIndexName(index);
        return elasticsearchOperations.addAlias(aliasQuery);
    }

    @Override
    public boolean removeAlias(String index, String aliasName) {
        AliasQuery aliasQuery = new AliasQuery();
        aliasQuery.setAliasName(aliasName);
        aliasQuery.setIndexName(index);
        return elasticsearchOperations.removeAlias(aliasQuery);
    }

    @Override
    public void clearIndex(String index, String type) {
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setIndex(index);
        deleteQuery.setType(type);
        deleteQuery.setQuery(matchAllQuery());
        elasticsearchOperations.delete(deleteQuery);
    }

    private WafHttpClient buildClient() {
        WafHttpClient httpClient = new WafHttpClient();
        return httpClient;
    }
}
