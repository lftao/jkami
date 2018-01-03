package com.javatao.jkami;

import java.io.Serializable;

/**
 * 搜索 条件
 * 
 * @author tao
 */
public class SearchFilter implements Serializable {
    /**
     * 搜索条件
     * 
     * @author tao
     */
    public enum Operator {
        /**
         * 小于
         */
        lt(" < ? "),
        /**
         * 小于等于
         */
        let(" <= ? "),
        /**
         * 大于
         */
        gt(" > ? "),
        /**
         * 大于等于
         */
        get(" >= ? "),
        /**
         * 等于
         */
        eq(" = ? "),
        /**
         * like ?
         */
        lk(" like ? "),
        /**
         * property in (?)
         */
        in(" in(?) "),
        /**
         * property not in(？)
         */
        not(" not in(?) "),
        /**
         * property in (sql)
         */
        inSql(""),
        /**
         * property not in (sql)
         */
        notInSql(""),
        /**
         * property concat
         */
        concat("");
        
        String op;
        String sql;

        Operator(String op) {
            this.op = op;
        }

        public String getOp() {
            return op;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }
    }

    private static final long serialVersionUID = -4717572686702173329L;

    public SearchFilter() {}

    public SearchFilter(Operator operator, String property, Object value) {
        super();
        this.operator = operator;
        this.property = property;
        this.value = value;
    }

    private Operator operator;
    private String property;
    private Object value;

    public Operator getOperator() {
        return operator;
    }

    public SearchFilter setOperator(Operator operator) {
        this.operator = operator;
        return this;
    }

    public String getProperty() {
        return property;
    }

    public SearchFilter setProperty(String property) {
        this.property = property;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public SearchFilter setValue(Object value) {
        this.value = value;
        return this;
    }

    public static SearchFilter newInstance(Operator operator, String property, Object value) {
        return new SearchFilter(operator, property, value);
    }

    public static SearchFilter newInstance() {
        return new SearchFilter();
    }
}
