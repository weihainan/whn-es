package com.whn.waf.common.es.supprot;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.whn.waf.common.es.domain.ESCommonDomain;
import com.whn.waf.common.support.PageableItems;
import com.whn.waf.common.support.WafJsonMapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author weihainan.
 * @since 0.1 created on 2017/6/19.
 */
public class ESUtils {

    private static final Logger logger = LoggerFactory.getLogger(ESUtils.class);

    public static PageableItems<ESCommonDomain> getItems(Object object) {
        return PageableItems.of(getList(object), getTotal(object));
    }

    public static PageableItems<Object> getDataItems(Object object) {
        return PageableItems.of(getDataList(object), getTotal(object));
    }

    /**
     * 从ES的查询结果中获取总数
     *
     * @param object
     * @return
     */
    public static long getTotal(Object object) {
        return Long.valueOf(((Map) ((Map) object).get("hits")).get("total").toString());
    }

    /**
     * 从ES的查询结果中获取ESCommonDomain列表
     *
     * @param esQueryResult
     * @return
     */
    public static List<ESCommonDomain> getList(Object esQueryResult) {
        List<ESCommonDomain> domains = Lists.newArrayList();
        List list = (List) ((Map) ((Map) esQueryResult).get("hits")).get("hits");
        for (Object object : list) {
            domains.add(getDomain(object));
        }
        return domains;
    }

    /**
     * 解析可复用资源中竞品收集相关的聚合结果
     *
     * @param esQueryResult
     * @return
     */
    public static List<String> getAggs(Object esQueryResult) {
        List<String> result = new ArrayList<>();
        List<Map> list = (List) ((Map) ((Map) ((Map) esQueryResult).get("aggregations")).get("result")).get("buckets");
        for (Map map : list) {
            result.add((String) map.get("key"));
        }
        return result;
    }

    /**
     * 解析可复用资源中竞品收集相关的聚合结果
     *
     * @param esQueryResult
     * @return
     */
    public static List<Map> getAggsBuckets(Object esQueryResult) {
        return (List) ((Map) ((Map) ((Map) esQueryResult).get("aggregations")).get("result")).get("buckets");
    }


    public static Collection<Object> getDataList(Object object) {

        List<ESCommonDomain> esCommonDomains = getList(object);
        if (CollectionUtils.isEmpty(esCommonDomains)) {
            return Collections.emptyList();
        }

        return Collections2.transform(esCommonDomains, new Function<ESCommonDomain, Object>() {
            @Override
            public Object apply(ESCommonDomain input) {
                if (input == null) {
                    return null;
                }
                return input.getData();
            }
        });
    }


    private static ESCommonDomain getDomain(Object object) {
        try {
            ESCommonDomain esCommonDomain = WafJsonMapper.parse(WafJsonMapper.toJson(((Map) object).get("_source")), ESCommonDomain.class);
            esCommonDomain.setId(((Map) object).get("_id").toString());
            return esCommonDomain;
        } catch (IOException e) {
            logger.info("data deserialize error.", e);
            return null;
        }
    }

    /**
     * 获取ESCommonDomain的data数据并转为指定类型
     *
     * @param esCommonDomain
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T getData(ESCommonDomain esCommonDomain, Class<T> type) {
        if (esCommonDomain == null) {
            return null;
        }
        try {
            return WafJsonMapper.parse(WafJsonMapper.toJson(esCommonDomain.getData()), type);
        } catch (IOException e) {
            logger.info("data deserialize error.", e);
            return null;
        }
    }


    // 解决因为字段全是数字不分词导致查询不到结果的问题
    public static BoolQueryBuilder buildMatchQuery(String field, String value) {
        return QueryBuilders.boolQuery().should(QueryBuilders.matchQuery(field, value))
                .should(QueryBuilders.wildcardQuery(String.format("%s", field), String.format("*%s*", value)))
                .should(QueryBuilders.wildcardQuery(String.format("%s.raw", field), String.format("*%s*", value)));
    }

    private ESUtils() {
        // empty
    }
}