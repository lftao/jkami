package com.javatao.jkami.jdbc;

import java.sql.ResultSet;

public interface ResultHandle<T> {
    /**
     * 处理结果集
     * 
     * @param rs
     *            ResultSet
     * @return 结果
     */
    T handle(ResultSet rs);
}
