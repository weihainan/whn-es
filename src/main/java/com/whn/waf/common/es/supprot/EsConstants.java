package com.whn.waf.common.es.supprot;

import java.util.HashSet;
import java.util.Set;

/**
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */
public class EsConstants {

    public static final String CUSTOM ="custom";

    //存放es的index_type
    private static final Set<String> INDEXES = new HashSet<>();
    private static final Set<String> INDEX_TYPES = new HashSet<>();

    public static boolean containsKeys(String index, String index_type) {
        return INDEXES.contains(index) && INDEX_TYPES.contains(index_type);
    }

    public static void addKey(String index, String index_type) {
        INDEXES.add(index);
        INDEX_TYPES.add(index_type);
    }

    public static void clearIndexs() {
        INDEXES.clear();
        INDEX_TYPES.clear();
    }

    public EsConstants() {
        // empty
    }
}
