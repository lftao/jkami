package com.javatao.jkami.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.support.JdbcUtils;

/**
 * MapListHandle
 * 
 * @author tao
 * @param <T>
 */
public class MapListHandle implements ResultHandle<List<Map<String, Object>>> {
    private final static String RWN = "rownum_";

    @Override
    public List<Map<String, Object>> handle(ResultSet rs) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            ResultSetMetaData rsm = rs.getMetaData();
            int n = rsm.getColumnCount();
            List<String> names = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                // sql 查询的字段
                String name = rsm.getColumnLabel(i);
                if (RWN.equalsIgnoreCase(name)) {
                    continue;
                }
                names.add(name);
            }
            while (rs.next()) {
                Map<String, Object> mp = new HashMap<>();
                for (int index = 1; index <= names.size(); index++) {
                    String name = names.get(index - 1);
                    Object value = JdbcUtils.getResultSetValue(rs, index);
                    mp.put(name, value);
                }
                result.add(mp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
