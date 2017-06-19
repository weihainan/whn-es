package com.whn.waf.common.es.supprot;

import com.google.common.collect.Lists;
import com.whn.waf.common.base.constant.ErrorCode;
import com.whn.waf.common.exception.WafBizException;
import com.whn.waf.common.support.WafJsonMapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */
public class EsQueryUtils {

    public static void compositeListParamQuery(BoolQueryBuilder queryBuilder, CustomListParam pageable) {
        for (Condition condition : pageable.getConditions()) {
            Operator operator = condition.getOperator();
            String field = condition.getField();
            Object value = condition.getValue();
            switch (operator) {
                case EQ:
                    if (value.equals("null")) {
                        queryBuilder.must(boolQuery().mustNot(existsQuery(field)));
                        break;
                    }
                    queryBuilder.must(boolQuery().should(termQuery(field, value))
                            .should(termQuery(String.format("%s.raw", field), value)));
                    break;
                case NE:
                    if (value.equals("null")) {
                        queryBuilder.must(existsQuery(field));
                        break;
                    }
                    queryBuilder.mustNot(boolQuery().should(termQuery(field, value))
                            .should(termQuery(String.format("%s.raw", field), value)));
                    break;
                case GE:
                    queryBuilder.must(rangeQuery(field).gte(value));
                    break;
                case GT:
                    queryBuilder.must(rangeQuery(field).gt(value));
                    break;
                case LE:
                    queryBuilder.must(rangeQuery(field).lte(value));
                    break;
                case LT:
                    queryBuilder.must(rangeQuery(field).lt(value));
                    break;
                case LIKE:
                    queryBuilder.must(boolQuery().should(matchPhraseQuery(field, value))
                            .should(wildcardQuery(String.format("%s.raw", field), String.format("*%s*", value))));
                    break;
                case MATCH:
                    queryBuilder.must(boolQuery().should(matchQuery(field, value))
                            .should(wildcardQuery(String.format("%s.raw", field), String.format("*%s*", value))));
                    break;
                case PHRASE_PREFIX:
                    queryBuilder.must(matchPhrasePrefixQuery(field, value));
                    break;
                case IN:
                    BoolQueryBuilder inBoolQuery = boolQuery();
                    List<String> values = (ArrayList<String>) value;
                    for (String v : values) {
                        inBoolQuery.should(termQuery(field, v)).should(termQuery(String.format("%s.raw", field), v));
                    }
                    queryBuilder.must(inBoolQuery);
                    break;
                case BETWEEN:
                    List<String> _values = (ArrayList<String>) value;
                    queryBuilder.must(rangeQuery(field).gte(Double.parseDouble(_values.get(0)))
                            .lte(Double.parseDouble(_values.get(1))));
                    break;
                default:
                    throw WafBizException.of(ErrorCode.INVALID_QUERY);
            }
        }
    }

    public static HashMap<String, Object> compositeListParamQueryMap(BoolQueryBuilder queryBuilder,
                                                                     CustomListParam pageable) {
        compositeListParamQuery(queryBuilder, pageable);
        HashMap<String, Object> map = new HashMap<String, Object>();
        String query = queryBuilder.toString();
        try {
            map = WafJsonMapper.parse(query, HashMap.class);
        } catch (IOException e) {
            throw WafBizException.of("compositeListParamQueryMap", ErrorCode.INVALID_QUERY, e);
        }
        List<Object> orders = Lists.newArrayList();
        if (pageable.getSort() != null) {
            Iterator<Sort.Order> iterator = pageable.getSort().iterator();
            while (iterator.hasNext()) {
                Sort.Order order = iterator.next();
                HashMap<String, Object> fields = new HashMap<>();
                HashMap<String, Object> fieldSort = new HashMap<>();
                fieldSort.put("order",
                        order.getDirection().name().equalsIgnoreCase("ASC") ? SortOrder.ASC : SortOrder.DESC);
                fields.put(order.getProperty(), fieldSort);
                orders.add(fields);
            }
        }
        HashMap<String, Object> queryMap = new HashMap<>();
        queryMap.put("query", map);
        if (!orders.isEmpty()) {
            queryMap.put("sort", orders);
        }
        queryMap.put("from", pageable.getOffset() * pageable.getLimit());
        queryMap.put("size", pageable.getLimit());
        return queryMap;
    }


    public EsQueryUtils() {
        // empty
    }
}
