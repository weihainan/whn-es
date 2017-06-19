package com.whn.waf.common.es.supprot;

import java.util.HashSet;
import java.util.Set;

/**
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */
public enum Operator {

    EQ("="),
    NE("!="),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    IN("IN"),
    BETWEEN("BETWEEN"),
    LIKE("LIKE"),
    MATCH("MATCH"),
    PHRASE_PREFIX("PHRASE_PREFIX");

    private static final Set<String> SET = new HashSet<>();

    private String value;

    Operator(String value) {
        this.value = value;
    }

    static {
        for (Operator operator : Operator.values()) {
            SET.add(operator.toString());
        }
    }

    public static boolean isValid(String str) {
        return SET.contains(str);
    }

    public String getValue() {
        return this.value;
    }
}
