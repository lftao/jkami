package com.javatao.jkami.jdbc;

import java.sql.ResultSet;

public interface ResultHandle<T> {
    T handle(ResultSet rs);
}
