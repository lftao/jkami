package com.javatao.jkami;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.BeanUtils;
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
    private transient Boolean lazybean=false;
    /** sql 模板路径 */
    private transient String sqlPath;
    private transient String dataSourceId;
    private transient String mappingPath;
    private transient String batchSplit=";";

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

    public String getSqlPath() {
        return this.sqlPath;
    }

    public RunConfing setSqlPath(String sqlpath) {
        this.sqlPath = sqlpath;
        return this;
    }

    public RunConfing setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
        return this;
    }
    public String getDataSourceId() {
        return this.dataSourceId;
    }

    public String getMappingPath() {
        return mappingPath;
    }

    public String getBatchSplit() {
		return batchSplit;
	}

	public RunConfing setBatchSplit(String batchSplit) {
		this.batchSplit = batchSplit;
		return this;
	}

	public RunConfing setMappingPath(String mappingPath) {
        this.mappingPath = mappingPath;
        return this;
    }

    public void bind() {
    	config.set(this);
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
    	RunConfing cg = RunConfing.config.get();
    	RunConfing cfg = new RunConfing();
    	BeanUtils.copyProperties(config, cfg);
    	if(cg!=null) {
    	   cfg.setBatchSplit(cg.getBatchSplit());
    	}
        RunConfing.config.set(cfg);
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
