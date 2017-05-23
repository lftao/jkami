package com.javatao.jkami;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * 运行时参数
 * 
 * @author tao
 */
public class RunConfing {
    private static ThreadLocal<RunConfing> config = new ThreadLocal<RunConfing>();
    /** 数据库类型 */
    private transient String dbType;
    /** 数据源 */
    private transient DataSource dataSource;
    /** 懒加载 */
    private transient Boolean lazybean;
    /** sql 模板路径 */
    private transient String sqlpath;
    private transient String dataSourceId;

    public String getDbType() {
        return dbType;
    }

    public RunConfing setDbType(String dbType) {
        this.dbType = dbType;
        return this;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public RunConfing setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public Boolean isLazybean() {
        return lazybean;
    }

    public RunConfing setLazybean(Boolean lazybean) {
        this.lazybean = lazybean;
        return this;
    }

    public String getSqlpath() {
        return this.sqlpath;
    }

    public RunConfing setSqlpath(String sqlpath) {
        this.sqlpath = sqlpath;
        return this;
    }

    public RunConfing setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
        return this;
    }
    public String getDataSourceId() {
        return this.dataSourceId;
    }

    public void bind() {
        bindConfing(this);
    }

    public static void clear() {
        config.remove();
    }

    public static RunConfing getConfig() {
        RunConfing runConfing = config.get();
        if (runConfing == null) {
            bindConfing(new RunConfing());
        }
        return config.get();
    }

    public static void bindConfing(RunConfing config) {
        RunConfing.config.set(config);
    }

    public Connection getConnection() {
        if (dataSource == null) {
            throw new RuntimeException("dataSource is null  no bean dataSource please set spring xml ");
        }
        try {
            return DataSourceUtils.getConnection(dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e1) {
                throw new JkException(e);
            }
        }
    }

    public void doReleaseConnection(Connection con) {
        try {
            if (dataSource != null) {
                DataSourceUtils.doReleaseConnection(con, dataSource);
            } else {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
