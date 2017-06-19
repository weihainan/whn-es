package com.whn.waf.common.es.supprot;

import com.whn.waf.common.base.constant.ErrorCode;
import com.whn.waf.common.exception.WafBizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */
public class CustomListParam extends SlicePage {
    /**
     * 过滤条件
     */
    private List<Condition> conditions = Collections.emptyList();

    public CustomListParam addCondition(Condition condition) {
        if (condition == null) {
            return this;
        }
        if (this.conditions.isEmpty()) {
            this.conditions = new LinkedList<>();
        }
        this.conditions.add(condition);
        return this;
    }

    /**
     * 替换掉相同Field及Operator的条件，如果原不存在，不抛错
     */
    public CustomListParam replaceCondition(Condition condition) {
        if (condition == null) {
            return this;
        }
        if (this.conditions.isEmpty()) {
            this.conditions = new LinkedList<>();
            this.conditions.add(condition);
        } else {
            removeCondition(condition.getField(), condition.getOperator());
            this.conditions.add(condition);
        }
        return this;
    }

    public CustomListParam removeCondition(Condition condition) {
        if (!conditions.isEmpty()) {
            conditions.remove(condition);
        }
        return this;
    }

    public CustomListParam clearConditions() {
        conditions.clear();
        return this;
    }

    /**
     * 删除指定Field及Operator的条件
     */
    public CustomListParam removeCondition(String field, Operator operator) {
        if (conditions.isEmpty()) {
            return this;
        }
        Iterator<Condition> iterator = conditions.iterator();
        while (iterator.hasNext()) {
            Condition condition = iterator.next();
            if (condition.getField().equals(field) && condition.getOperator().equals(operator)) {
                iterator.remove();
            }
        }
        return this;
    }

    /**
     * 删除指定Field的条件
     */
    public CustomListParam removeCondition(String field) {
        if (conditions.isEmpty()) {
            return this;
        }
        Iterator<Condition> iterator = conditions.iterator();
        while (iterator.hasNext()) {
            Condition condition = iterator.next();
            if (condition.getField().equals(field)) {
                iterator.remove();
            }
        }
        return this;
    }

    public boolean containCondition(Condition condition) {
        return conditions.contains(condition);
    }

    public int conditionSize() {
        return conditions.size();
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public CustomListParam parseConditions(String filter) {
        if (!StringUtils.isBlank(filter)) {
            String[] conditionArray = filter.split("\\s+and\\s+");
            conditions = new LinkedList<>();
            for (String conditionStr : conditionArray) {
                conditions.add(parseCondition(conditionStr));
            }
        }
        return this;
    }

    private Condition parseCondition(String conditionStr) {
        String[] filterValues = conditionStr.trim().split("\\s+");
        if (filterValues.length < 3) {
            throw WafBizException.of(ErrorCode.INVALID_QUERY);
        }
        String fieldName = filterValues[0];
        if (!Operator.isValid(filterValues[1].toUpperCase())) {
            throw WafBizException.of(ErrorCode.INVALID_QUERY);
        }
        Operator operator = Operator.valueOf(filterValues[1].toUpperCase());
        String rawValue = conditionStr.substring(conditionStr.indexOf(" " + filterValues[1] + " ") + filterValues[1].length() + 2).trim();
        try {
            if (operator == operator.IN || operator == operator.BETWEEN) {
                List<String> values = new ArrayList<>();
                if (rawValue.startsWith("[")) {
                    rawValue = rawValue.trim().substring(1, rawValue.length() - 1);
                    for (String v : rawValue.split(",")) {
                        values.add(new String(Base64Utils.decodeFromUrlSafeString(v.replaceAll(" ", "+")), "utf8"));
                    }
                } else {//兼容旧的写法
                    //获取值串并且补全url传参数+变为空格的问题
                    rawValue = new String(Base64Utils.decodeFromUrlSafeString(rawValue.replaceAll(" ", "+")), "utf8");
                    rawValue = rawValue.trim().substring(1, rawValue.length() - 1);
                    for (String v : rawValue.split(",")) {
                        values.add(v);
                    }
                }
                return Condition.of(fieldMapping(fieldName), operator, values, List.class);
            } else {
                //获取值串并且补全url传参数+变为空格的问题
                return Condition.of(fieldMapping(fieldName), operator, new String(Base64Utils.decodeFromUrlSafeString(rawValue.replaceAll(" ", "+")), "utf8"), String.class);
            }
        } catch (UnsupportedEncodingException e) {
            throw WafBizException.of(ErrorCode.INVALID_QUERY, e);
        }
    }

    /**
     * 配置字段映射情况
     *
     * @return
     */
    public String fieldMapping(String fieldName) {
        return fieldName;
    }

}

