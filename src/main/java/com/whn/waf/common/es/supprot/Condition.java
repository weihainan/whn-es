package com.whn.waf.common.es.supprot;

import com.whn.waf.common.base.constant.ErrorCode;
import com.whn.waf.common.exception.WafBizException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */
public class Condition {

    private String field;

    private Operator operator;

    private Object value;

    private Class valueType;

    private Condition() {
    }

    public static Condition of(String field, Operator operator, Object value, Class valueType) {
        if (StringUtils.isBlank(field) || operator == null) {
            throw WafBizException.of(ErrorCode.INVALID_QUERY);
        }
        Condition condition = new Condition();
        condition.field = field;
        condition.operator = operator;
        condition.value = value;
        condition.valueType = valueType;
        return condition;
    }

    /**
     * 创建一个等于操作的条件
     *
     * @param field     字段名称，与实体字段名一致
     * @param value     字段值
     * @param valueType 字段类型
     * @return
     */
    public static Condition eq(String field, Object value, Class valueType) {
        return Condition.of(field, Operator.EQ, value, valueType);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Class getValueType() {
        return valueType;
    }

    public void setValueType(Class valueType) {
        this.valueType = valueType;
    }

    @Override
    public String toString() {
        return field + " " + operator + " " + value;
    }

}
