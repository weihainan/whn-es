package com.whn.waf.common.es.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Map;

/**
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */

@Document(indexName = "#{T(com.whn.waf.common.es.supprot.ESProvider).getIndex()}",createIndex=false,
        type = "#{T(com.whn.waf.common.es.supprot.ESProvider).getType()}")
public class ESCommonDomain {

    @Id
    private String id;
    private Object data;
    private Map<String, Object> indexes;

    @JsonIgnore
    private String type;

    public ESCommonDomain() {
        // empty
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, Object> getIndexes() {
        return indexes;
    }

    public void setIndexes(Map<String, Object> indexes) {
        this.indexes = indexes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
