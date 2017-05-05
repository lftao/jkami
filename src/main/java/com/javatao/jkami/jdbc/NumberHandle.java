package com.javatao.jkami.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.support.JdbcUtils;

/**
 * 数字 结果
 * 
 * @author tao
 * @param <T>
 */
public class NumberHandle<T> implements ResultHandle<Long> {
    @Override
    public Long handle(ResultSet rs) {
        try {
            while (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(rs);
        }
        return 0L;
    }
}
