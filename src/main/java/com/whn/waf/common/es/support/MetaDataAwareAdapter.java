package com.whn.waf.common.es.support;

import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.highlight.HighlightField;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */
public class MetaDataAwareAdapter implements MetaDataAware {
    @Id
    private String id;
    private Object data;
    @Transient
    private Map<String, String> highlightFields;
    @Transient
    private String index;
    @Transient
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public void setScore(Float score) {
        // this.score=score;
    }

    public Map<String, String> getHighlightFields() {
        return highlightFields;
    }

    public void setHighlight(Map<String, HighlightField> highlightFields) {
        if (null != highlightFields) {
            if (this.highlightFields == null) {
                this.highlightFields = new HashMap<String, String>();
            }
            Set<Map.Entry<String, HighlightField>> entrySet = highlightFields.entrySet();
            for (Map.Entry<String, HighlightField> e : entrySet) {
                StringBuilder sb = new StringBuilder();
                for (Text text : e.getValue().fragments()) {
                    sb.append(text);
                }
                this.highlightFields.put(e.getKey(), sb.toString());
            }
        }
    }

}
