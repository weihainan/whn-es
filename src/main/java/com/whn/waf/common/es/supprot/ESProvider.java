package com.whn.waf.common.es.supprot;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;

/**
 * 一种约定：在需要操作es前，需要显示调用set方法把index和type设置进来
 * 只能满足一个请求处理过程中只会操作一个es的type
 * 问题：如果在一个请求中需要操作两种type这种方式就满足不了
 */
public class ESProvider {

    private static final Logger logger = LoggerFactory.getLogger(ESProvider.class);
    private static final ThreadLocal<String> INDEX = new NamedThreadLocal<>("index");
    private static final ThreadLocal<String> TYPE = new NamedThreadLocal<>("type");

    public static String getIndex() {
        return StringUtils.defaultString(INDEX.get(), "default_index");
    }

    public static String getType() {
        return StringUtils.defaultString(TYPE.get(), "default_type");
    }

    public static void remove() {
        INDEX.remove();
        TYPE.remove();
    }

    /**
     * 设置index和type，index会自动加前缀
     *
     * @param index es index
     * @param type  es type
     */
    public static void set(String index, String type) {
        INDEX.set(index);
        TYPE.set(type);
        logger.info("es index={}, type={}", getIndex(), getType());
    }
}
