package com.javatao.jkami.support;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.javatao.jkami.JkException;
import com.javatao.jkami.Page;
import com.javatao.jkami.RunConfing;
import com.javatao.jkami.jdbc.BeanListHandle;
import com.javatao.jkami.jdbc.JdbcTypesUtils;
import com.javatao.jkami.jdbc.MapListHandle;
import com.javatao.jkami.jdbc.NumberHandle;
import com.javatao.jkami.jdbc.ResultHandle;
import com.javatao.jkami.utils.FKParse;
import com.javatao.jkami.utils.JkBeanUtils;
import com.javatao.jkami.utils.SqlUtils;

/**
 * jdbc包装数据
 * 
 * @author tao
 */
public class DataMapper {
    private static DataMapper mapper;
    private static final Log logger = LogFactory.getLog(DataMapper.class);
    private static final String DEFAULT_BEAN = "defaultBean";
    private static final String EMPTY = "";
    private static final String SEMICOLON = ";";
    /**
     * 数据库类型
     */
    public static final String DATABSE_TYPE_MYSQL = "mysql";
    public static final String DATABSE_TYPE_POSTGRE = "postgresql";
    public static final String DATABSE_TYPE_ORACLE = "oracle";
    public static final String DATABSE_TYPE_SQLSERVER = "sqlserver";
    /**
     * 分页SQL
     */
    // mysql select * from ( {0} ) page_tab limit {1},{2}
    private static final String MYSQL_SQL = "select * from ( {0} ) page_kami_tab limit ?,?";
    // postgresql {2} offset {1}
    private static final String POSTGRE_SQL = "select * from ( {0} ) page_kami_tab limit ? offset ?";
    // oracle where rownum <= {1}) where rownum_>{2}
    private static final String ORACLE_SQL = "select * from (select row_.*,rownum rownum_ from ({0}) row_ where rownum <= ?) where rownum_> ? ";
    private static final String SQLSERVER_SQL = "select * from ( select row_number() over(order by tempColumn) tempRowNumber, * from (select top ? tempColumn = 0, {0} ) t ) tt where tempRowNumber > ? "; // sqlserver
    private static final String COUNT_SQL = "select count(1) from ( {0} ) conut_kami_tab"; // count_sql
    // oracle 序列查询
    private static final String SEQUENCE_SQL = "select {0}.nextval from dual"; // sequence_sql
    private static final Pattern pat = Pattern.compile(":[ tnx0Bfr]*[a-z.A-Z]+");
    private static final String numberRegex = "^:\\d+$";

    /**
     * 获取 Connection
     * 
     * @return Connection
     */
    private Connection getCon() {
        return RunConfing.getConfig().getConnection();
    }

    /**
     * 释放连接
     * 
     * @param con
     *            Connection
     */
    private void doReleaseConnection(Connection con) {
        RunConfing.getConfig().doReleaseConnection(con);
    }

    /**
     * 获取 DataMapper 单例
     * 
     * @return DataMapper
     */
    public static DataMapper getMapper() {
        if (mapper == null) {
            mapper = new DataMapper();
        }
        return mapper;
    }

    /**
     * 设置预编译值
     * 
     * @param ps
     *            PreparedStatement
     * @param args
     *            参数
     * @throws SQLException
     *             SQLException
     */
    private void setPSValue(PreparedStatement ps, Object... args) throws SQLException {
        if (args != null && args.length > 0) {
            if (args[0] instanceof Collection) {
                args = ((Collection<?>) args[0]).toArray();
            }
            for (int i = 0; i < args.length; i++) {
                Object v = args[i];
                int index = i + 1;
                if (v instanceof Date) {
                    ps.setTimestamp(index, new java.sql.Timestamp(((Date) v).getTime()));
                } else {
                    ps.setObject(index, v);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("params[" + i + ":" + v + "]");
                }
            }
        }
    }

    /**
     * 设置预编译值
     * 
     * @param ps
     *            PreparedStatement
     * @param entity
     *            实体
     * @return 字段数量
     */
    private int setPSEntityValue(PreparedStatement ps, Object entity) {
        try {
            Class<?> classType = entity.getClass();
            List<String> fileds = SqlUtils.getEntityAttrMp(classType);
            for (int i = 0; i < fileds.size(); i++) {
                String fd = fileds.get(i);
                Object value = JkBeanUtils.getPropertyValue(entity, fd);
                int index = i + 1;
                if (value == null) {
                    ps.setNull(index, JdbcTypesUtils.getJdbcType(value));
                } else {
                    if (value instanceof Date) {
                        ps.setTimestamp(index, new java.sql.Timestamp(((Date) value).getTime()));
                    } else {
                        ps.setObject(index, value);
                    }
                }
            }
            return fileds.size();
        } catch (Exception e) {
            throw new JkException(e);
        }
    }

    /**
     * 保存对象
     * 
     * @param o
     *            对象
     * @return 变更值
     */
    public int save(Object o) {
        RunConfing config = RunConfing.getConfig();
        Connection con = config.getConnection();
        Class<?> classType = o.getClass();
        String sql = SqlUtils.getSqls(classType, SqlUtils.TYPE.INSERT);
        try {
            Object[] key = SqlUtils.getTableKey(classType);
            if (key == null) {
                throw new JkException(classType.getPackage() + classType.getName() + " no  @key annotation ");
            }
            Object idValue = JkBeanUtils.getPropertyValue(o, (String) key[0]);
            String dbType = config.getDbType();
            if (idValue == null) {
                if (dbType.contains(DATABSE_TYPE_ORACLE)) {
                    String s = SqlUtils.getSequenceGeneratorVal(classType);
                    if (s != null) {
                        // 查询序列
                        String sequenceSql = MessageFormat.format(SEQUENCE_SQL, s);
                        idValue = query(sequenceSql, new NumberHandle<Long>());
                        if (idValue != null) {
                            JkBeanUtils.setProperty(o, (String) key[1], idValue);
                        }
                    }
                }
            }
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setPSEntityValue(ps, o);
            int n = ps.executeUpdate();
            if (idValue == null) {
                ResultSet rs = ps.getGeneratedKeys();// 返回主键
                ResultHandle<Long> handle = new NumberHandle<Long>();
                idValue = handle.handle(rs);
                JkBeanUtils.setProperty(o, (String) key[1], idValue);
            }
            ps.close();
            return n;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            doReleaseConnection(con);
        }
    }

    /**
     * 查询结果
     * 
     * @param <T>
     *            实体类型
     * @param sql
     *            sql
     * @param result
     *            类型
     * @param currentDepth
     *            当前深度
     * @param maxDepth
     *            最大深度
     * @param params
     *            参数
     * @return 结果
     */
    public <T> T queryForObject(String sql, Class<T> result, int currentDepth, int maxDepth, Object... params) {
        List<T> list = query(sql, new BeanListHandle<>(result, currentDepth, maxDepth), params);
        if (list != null) {
            if (list.size() > 1) {
                throw new JkException("queryForObject size = " + list.size() + " not one");
            }
            if (list.size() == 1) {
                return list.get(0);
            }
        }
        return null;
    }

    /**
     * 查询结果
     * 
     * @param <E>
     *            返回类型
     * @param sql
     *            sql
     * @param handle
     *            处理器
     * @param params
     *            参数
     * @return 结果
     */
    public <E> E query(String sql, ResultHandle<E> handle, Object... params) {
        Connection con = getCon();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(sql);
            }
            PreparedStatement ps = con.prepareStatement(sql);
            setPSValue(ps, params);
            ResultSet rs = ps.executeQuery();
            E rows = handle.handle(rs);
            ps.close();
            return rows;
        } catch (Exception e) {
            throw new JkException(e);
        } finally {
            doReleaseConnection(con);
        }
    }

    /**
     * 查找单个
     * 
     * @param <T>
     *            实体类型
     * @param result
     *            结果类型
     * @param sqlParameter
     *            sql parameter
     * @return 结果
     */
    public <T> T findOne(Class<T> result, Map<String, Object> sqlParameter) {
        Connection con = getCon();
        StringBuilder sb = new StringBuilder(SqlUtils.getSqls(result, SqlUtils.TYPE.SELECT));
        try {
            sb.append(" where 1=1 ");
            Map<String, String> filedMap = SqlUtils.getEntityFiledColumnMap(result);
            List<Object> values = new ArrayList<>();
            for (String ky : sqlParameter.keySet()) {
                String col = filedMap.get(ky);
                if (col == null) {
                    col = ky;
                }
                sb.append(" and " + col + " = ? ");
                values.add(sqlParameter.get(ky));
            }
            String sql = sb.toString();
            int maxDepth = JkBeanUtils.getMaxDepth(result);
            return queryForObject(sql, result, 1, maxDepth, values);
        } catch (Exception e) {
            throw new JkException(e);
        } finally {
            doReleaseConnection(con);
        }
    }

    /**
     * 查询返回
     * 
     * @param sql
     *            sql
     * @param params
     *            参数
     * @return 结果集合
     */
    public List<Map<String, Object>> queryForMap(String sql, Object... params) {
        return query(sql, new MapListHandle(), params);
    }

    /**
     * 根据id查找 对象
     * 
     * @param <T>
     *            实体类型
     * @param id
     *            ID
     * @param classType
     *            参数
     * @return 结果
     */
    public <T> T findById(Serializable id, Class<T> classType) {
        Connection con = getCon();
        String sql = SqlUtils.getSqls(classType, SqlUtils.TYPE.SELECT);
        try {
            Object[] key = SqlUtils.getTableKey(classType);
            if (key == null) {
                throw new RuntimeException(classType.getPackage() + classType.getName() + "no  @key annotation ");
            }
            sql += " where " + key[1] + " = ? ";
            int maxDepth = JkBeanUtils.getMaxDepth(classType);
            return queryForObject(sql, classType, 1, maxDepth, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            doReleaseConnection(con);
        }
    }

    /**
     * 更新实体
     * 
     * @param <T>
     *            实体类型
     * @param o
     *            实体
     * @return 变更值
     */
    public <T> int updateById(T o) {
        Class<?> classType = o.getClass();
        String sql = SqlUtils.getSqls(classType, SqlUtils.TYPE.UPDATE);
        Object[] key = SqlUtils.getTableKey(classType);
        if (key == null) {
            throw new RuntimeException(classType.getPackage() + classType.getName() + "no  @key annotation ");
        }
        sql += " where " + key[1] + " = ? ";
        Object kv = JkBeanUtils.getPropertyValue(o, (String) key[0]);
        return executeUpdate(sql, kv);
    }

    /**
     * 执行 更新
     * 
     * @param sql
     *            sql
     * @param params
     *            参数
     * @return 变更值
     */
    public int executeUpdate(String sql, Object... params) {
        Connection con = getCon();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(sql);
            }
            PreparedStatement ps = con.prepareStatement(sql);
            ps.addBatch(sql);
            setPSValue(ps, params);
            int n = ps.executeUpdate();
            ps.close();
            return n;
        } catch (Exception e) {
            throw new JkException(e);
        } finally {
            doReleaseConnection(con);
        }
    }

    /**
     * 批量执行
     * 
     * @param sqls
     *            sql集合
     * @param params
     *            参数
     * @return 变更集合
     */
    public Object executeBatchUpdate(String sqls, Object... params) {
        Connection con = getCon();
        try {
            if (sqls.indexOf(SEMICOLON) == -1) {
                return executeUpdate(sqls, params);
            }
            String[] sqlss = sqls.replaceAll("\r|\n", EMPTY).split(SEMICOLON);
            int length = sqlss.length;
            Statement st = con.createStatement();
            for (int i = 0; i < length; i++) {
                String sql = sqlss[i];
                if (logger.isDebugEnabled()) {
                    logger.debug("addBatch: " + sql);
                }
                st.addBatch(sql);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("executeBatchUpdate size: " + length);
            }
            int[] batch = st.executeBatch();
            st.close();
            return batch;
        } catch (Exception e) {
            throw new JkException(e);
        } finally {
            doReleaseConnection(con);
        }
    }

    /**
     * 根据 id 删除 <br>
     * delete from tabel where id=?
     * 
     * @param <T>
     *            实体类型
     * @param id
     *            id
     * @param classType
     *            类型
     * @return 变更值
     */
    public <T> int deleteById(Serializable id, Class<T> classType) {
        Object[] key = SqlUtils.getTableKey(classType);
        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(SqlUtils.getTableName(classType));
        sb.append(" where " + key[1] + " = ? ");
        String sql = sb.toString();
        return executeUpdate(sql, id);
    }

    /**
     * 更新非空
     * 
     * @param <T>
     *            泛型
     * @param o
     *            对象
     * @return 变更值
     */
    public <T> int updateNotNullById(T o) {
        Class<?> classType = o.getClass();
        StringBuilder sb = new StringBuilder("update ");
        sb.append(SqlUtils.getTableName(classType));
        sb.append(" set ");
        List<String> attrs = SqlUtils.getEntityAttrMp(classType);
        Map<String, String> filedMap = SqlUtils.getEntityFiledColumnMap(classType);
        List<Object> values = new ArrayList<>();
        boolean isFirst = true;
        for (int i = 0; i < attrs.size(); i++) {
            String attr = attrs.get(i);
            Object value = JkBeanUtils.getPropertyValue(o, attr);
            if (value != null) {
                if (!isFirst) {
                    sb.append(",");
                }
                sb.append(filedMap.get(attr) + " = ? ");
                values.add(value);
                isFirst = false;
            }
        }
        String sql = sb.toString();
        return executeUpdate(sql, values);
    }

    /**
     * 分页查询
     * 
     * @param <T>
     *            泛型
     * @param sql
     *            sql
     * @param classType
     *            类型
     * @param page
     *            分页参数
     * @return 分页实例
     */
    public <T> Page<T> findPage(String sql, Class<T> classType, Page<T> page) {
        if (page == null) {
            page = new Page<>();
        }
        if (sql == null) {
            sql = SqlUtils.getSelectSqls(classType);
        }
        RunConfing config = RunConfing.getConfig();
        String dbType = config.getDbType();
        //
        ArrayList<Object> params = new ArrayList<>();
        String sqlParam = SqlUtils.getSearchParames(classType, page, params);
        sql += sqlParam;
        String pageSql = createPageSql(dbType, sql, page.getPage(), page.getSize(), params);
        String countSql = MessageFormat.format(COUNT_SQL, sql);
        //
        int maxDepth = JkBeanUtils.getMaxDepth(classType);
        List<T> result = query(pageSql, new BeanListHandle<>(classType, 1, maxDepth), params);
        params.remove(params.size() - 1);
        params.remove(params.size() - 1);
        //
        Long total = query(countSql, new NumberHandle<>(), params);
        page.setRows(result);
        page.setTotal(total);
        return page;
    }

    /**
     * 按照数据库类型，封装SQL
     * 
     * @param dbType
     *            数据库类型
     * @param sql
     *            sql
     * @param page
     *            页数
     * @param rows
     *            每页数
     * @param params
     *            参数
     * @return 分页sql
     */
    public static String createPageSql(String dbType, String sql, int page, int rows, ArrayList<Object> params) {
        int beginNum = (page - 1) * rows;
        if (JkBeanUtils.isBlank(dbType)) {
            throw new RuntimeException("(数据库类型:dbType) 没有设置,请检查配置文件");
        }
        if (dbType.indexOf(DATABSE_TYPE_MYSQL) > -1) {
            sql = MessageFormat.format(MYSQL_SQL, sql);
            params.add(beginNum);
            params.add(rows);
        } else if (dbType.indexOf(DATABSE_TYPE_POSTGRE) > -1) {
            sql = MessageFormat.format(POSTGRE_SQL, sql);
            params.add(rows);
            params.add(beginNum);
        } else {
            int endIndex = beginNum + rows;
            params.add(endIndex);
            params.add(beginNum);
            if (dbType.indexOf(DATABSE_TYPE_ORACLE) > -1) {
                sql = MessageFormat.format(ORACLE_SQL, sql);
            } else if (dbType.indexOf(DATABSE_TYPE_SQLSERVER) > -1) {
                sql = MessageFormat.format(SQLSERVER_SQL, sql);
            }
        }
        return sql;
    }

    /**
     * 组装占位符参数 - Map
     * 
     * @param executeSql
     *            执行的sql
     * @param sqlParamsMap
     *            参数
     * @param result
     *            结果参数
     * @param k
     *            拓展参数
     * @return 解析后sql
     */
    public String placeholderSqlParam(String executeSql, Map<String, Object> sqlParamsMap, List<Object> result, String... k) {
        String key = EMPTY;
        if (k != null && k.length > 0) {
            key = k[0];
        }
        //非 别名参数 sql 直接解析${...}
        if (sqlParamsMap != null && !sqlParamsMap.isEmpty()) {
            executeSql = FKParse.parseTemplateContent(executeSql, sqlParamsMap);
        }
        Matcher m = pat.matcher(executeSql);
        while (m.find()) {
            String match = m.group();
            if (match.matches(numberRegex)) {
                continue;
            }
            executeSql = executeSql.replace(match, "?");
            Object v = FKParse.parseTemplateContent("${" + match.replace(":", key).trim() + "}", sqlParamsMap);
            result.add(v);
            if (logger.isDebugEnabled()) {
                logger.debug(" Match [" + match + "] at positions " + m.start() + "-" + (m.end() - 1) + " value:" + v);
            }
        }
        return executeSql;
    }

    /**
     * 组装占位符参数 - Map
     * 
     * @param executeSql
     *            执行的sql
     * @param result
     *            结果参数
     * @param value
     *            值
     * @return 解析后sql
     */
    public String placeholderSqlParam(String executeSql, List<Object> result, Object value) {
        Map<String, Object> sqlParamsMap = new HashMap<>();
        sqlParamsMap.put(DEFAULT_BEAN, value);
        return placeholderSqlParam(executeSql, sqlParamsMap, result, DEFAULT_BEAN + ".");
    }
}
