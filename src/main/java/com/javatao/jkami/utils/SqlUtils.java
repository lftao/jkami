package com.javatao.jkami.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.javatao.jkami.CacheMap;
import com.javatao.jkami.Page;
import com.javatao.jkami.RunConfing;
import com.javatao.jkami.SearchFilter;
import com.javatao.jkami.SearchFilter.Operator;
import com.javatao.jkami.annotations.Column;
import com.javatao.jkami.annotations.Key;
import com.javatao.jkami.annotations.SequenceGenerator;
import com.javatao.jkami.annotations.Sql;
import com.javatao.jkami.annotations.Table;
import com.javatao.jkami.annotations.Transient;
import com.javatao.jkami.support.DataMapper;

/**
 * sql工具类
 * 
 * @author TLF
 */
public class SqlUtils {
    private static Map<Object, Object> mapCache = new CacheMap<>();
    private static Map<String, List<String>> entityAttrMp = new CacheMap<>();
    private static Map<String, List<String>> entityColumnMp = new CacheMap<>();
    private static Map<String, Map<String, String>> columnSqlMp = new CacheMap<>();
    private static Map<String, Boolean> sqlforce = new CacheMap<>();

    public enum TYPE {
        INSERT, SELECT, UPDATE
    };

    public static <T> String getSqls(Class<T> clazz, TYPE type) {
        try {
            String sql = null;
            if (TYPE.SELECT.equals(type)) {
                sql = getSelectSqls(clazz);
            }
            if (TYPE.INSERT.equals(type)) {
                sql = getInsertSqls(clazz);
            }
            if (TYPE.UPDATE.equals(type)) {
                sql = getUpdateSqls(clazz);
            }
            return sql;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询sql
     * 
     * @param clazz
     * @return
     */
    public static <T> String getSelectSqls(Class<T> clazz) {
        String key = clazz.getName();
        String keyCache = key + ".select";
        if (mapCache.containsKey(keyCache)) {
            return (String) mapCache.get(keyCache);
        }
        String table = getTableName(clazz);
        if (!entityColumnMp.containsKey(key)) {
            initData(clazz);
        }
        StringBuilder sb = new StringBuilder("select ");
        List<String> atts = entityColumnMp.get(key);
        Map<String, String> filedMap = getEntityColumnFiledMap(clazz);
        for (int i = 0; i < atts.size(); i++) {
            String col = atts.get(i);
            String prop = filedMap.get(col);
            if (i > 0) {
                sb.append(",");
            }
            sb.append(getCol(col));
            if (!col.equals(prop)) {
                sb.append(" as " + prop);
            }
        }
        sb.append(" from " + table + " _tb");
        String out = sb.toString();
        mapCache.put(keyCache, out);
        return out;
    }

    /**
     * 插入sql
     * 
     * @param clazz
     * @return
     */
    public static <T> String getInsertSqls(Class<T> clazz) {
        String key = clazz.getName();
        String keyCache = key + ".insert";
        if (mapCache.containsKey(keyCache)) {
            return (String) mapCache.get(keyCache);
        }
        String table = getTableName(clazz);
        if (!entityColumnMp.containsKey(key)) {
            initData(clazz);
        }
        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(table);
        sb.append("(");
        List<String> clos = entityColumnMp.get(key);
        for (int i = 0; i < clos.size(); i++) {
            String col = clos.get(i);
            col = getCol(col);
            if (i == 0) {
                sb.append(col);
            } else {
                sb.append("," + col);
            }
        }
        sb.append(")values(");
        for (int i = 0; i < clos.size(); i++) {
            if (i == 0) {
                sb.append(" ? ");
            } else {
                sb.append(", ? ");
            }
        }
        sb.append(")");
        String out = sb.toString();
        mapCache.put(keyCache, out);
        return out;
    }

    /**
     * 更新sql
     * 
     * @param clazz
     * @return
     */
    public static <T> String getUpdateSqls(Class<T> clazz) {
        String key = clazz.getName();
        String keyCache = key + ".update";
        if (mapCache.containsKey(keyCache)) {
            return (String) mapCache.get(keyCache);
        }
        String table = getTableName(clazz);
        if (!entityColumnMp.containsKey(key)) {
            initData(clazz);
        }
        StringBuilder sb = new StringBuilder("update ");
        sb.append(table);
        sb.append(" set ");
        List<String> clos = entityColumnMp.get(key);
        for (int i = 0; i < clos.size(); i++) {
            String col = clos.get(i);
            col = getCol(col);
            if (i == 0) {
                sb.append(col + " = ? ");
            } else {
                sb.append("," + col + " =? ");
            }
        }
        String out = sb.toString();
        mapCache.put(keyCache, out);
        return out;
    }

    private static <T> void initData(Class<T> clazz) {
        String k = clazz.getName();
        List<String> attrs = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        Map<String, String> cmsql = new HashMap<>();
        List<Field> listField = JkBeanUtils.getAllFields(clazz);
        for (Field field : listField) {
            // 排除静态
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String fdName = field.getName();
            if (fdName.contains("CGLIB")) {
                continue;
            }
            Transient tr = field.getAnnotation(Transient.class);
            if (tr != null) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            Sql Sql = field.getAnnotation(Sql.class);
            String columnString = JkBeanUtils.columnToHumpReversal(fdName);
            if (column != null) {
                columnString = column.value();
            }
            if (Sql != null) {
                cmsql.put(fdName, Sql.value());
                sqlforce.put(k + fdName, Sql.force());
            } else {
                attrs.add(fdName);
                columns.add(columnString);
            }
        }
        entityAttrMp.put(k, attrs);
        entityColumnMp.put(k, columns);
        columnSqlMp.put(k, cmsql);
    }

    /**
     * 获取表名
     * 
     * @param clazz
     * @return
     */
    public static <T> String getTableName(Class<T> clazz) {
        String k = clazz.getName() + ".tableName";
        if (mapCache.containsKey(k)) {
            return (String) mapCache.get(k);
        }
        Table table = clazz.getAnnotation(Table.class);
        String result = clazz.getSimpleName();
        if (table != null) {
            result = table.value();
        }
        mapCache.put(k, result);
        return result;
    }

    /**
     * 获取序列名
     * 
     * @param clazz
     * @return
     */
    public static <T> String getSequenceGeneratorVal(Class<T> clazz) {
        String k = clazz.getName() + ".SequenceGenerator";
        if (mapCache.containsKey(k)) {
            return (String) mapCache.get(k);
        }
        SequenceGenerator sg = clazz.getAnnotation(SequenceGenerator.class);
        String result = null;
        if (sg != null) {
            result = sg.value();
        }
        mapCache.put(k, result);
        return result;
    }

    /**
     * 获得主键字段<br/>
     * [0] name<br/>
     * [1] cloumn<br/>
     * [2] type<br/>
     * 
     * @return
     */
    public static <T> Object[] getTableKey(Class<T> clazz) {
        String k = clazz.getName() + ".key";
        if (mapCache.containsKey(k)) {
            return (Object[]) mapCache.get(k);
        }
        Object[] obj = new Object[3];
        List<Field> lsField = JkBeanUtils.getAllFields(clazz);
        for (Field field : lsField) {
            Key Key = field.getAnnotation(Key.class);
            Column column = field.getAnnotation(Column.class);
            obj[2] = field.getType();
            if (Key != null) {
                obj[0] = field.getName();
                if (column != null) {
                    obj[1] = column.value();
                } else {
                    obj[1] = field.getName();
                }
                mapCache.put(k, obj);
                return obj;
            }
        }
        return null;
    }

    /**
     * 获得实体属性
     * 
     * @param clazz
     * @return
     */
    public static <T> List<String> getEntityAttrMp(Class<T> clazz) {
        String key = clazz.getName();
        List<String> list = entityAttrMp.get(key);
        if (list == null) {
            initData(clazz);
            list = entityAttrMp.get(key);
        }
        return list;
    }

    /**
     * 获得实体db字段
     * 
     * @param clazz
     * @return
     */
    public static <T> List<String> getEntityColumnMp(Class<T> clazz) {
        String key = clazz.getName();
        List<String> list = entityColumnMp.get(key);
        if (list == null) {
            initData(clazz);
            list = entityColumnMp.get(key);
        }
        return list;
    }

    /**
     * 获得字段注解sql
     * 
     * @param <T>
     * @return
     */
    public static <T> Map<String, String> getColumnSqlMp(Class<T> clazz) {
        String key = clazz.getName();
        if (!columnSqlMp.containsKey(key)) {
            initData(clazz);
        }
        return columnSqlMp.get(key);
    }

    /**
     * sql强制查询
     * 
     * @param <T>
     * @return
     */
    public static <T> Boolean getSqlForce(String key) {
        Boolean force = sqlforce.get(key);
        if (force == null) {
            force = false;
        }
        return force;
    }

    /**
     * 获得属性和注解map
     * 
     * @param clazz
     * @return key属性 value注解
     */
    public static <T> Map<String, String> getEntityFiledColumnMap(Class<T> clazz) {
        String k = clazz.getName() + ".filedColMp";
        if (mapCache.containsKey(k)) {
            return (Map<String, String>) mapCache.get(k);
        }
        Map<String, String> mp = new HashMap<String, String>();
        List<Field> lsField = JkBeanUtils.getAllFields(clazz);
        String tmp = "";
        for (Field field : lsField) {
            // 排除静态
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String fdName = field.getName();
            if (fdName.indexOf("CGLIB") > -1) {
                continue;
            }
            Sql Sql = field.getAnnotation(Sql.class);
            if (Sql != null) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                tmp = column.value();
            } else {
                tmp = JkBeanUtils.columnToHumpReversal(field.getName());
            }
            mp.put(field.getName(), tmp);
        }
        mapCache.put(k, mp);
        return mp;
    }

    /**
     * 获得属性和注解map
     * 
     * @param clazz
     * @return key属性 value注解
     */
    public static <T> Map<String, String> getEntityColumnFiledMap(Class<T> clazz) {
        String k = clazz.getName() + ".colFiledMp";
        if (mapCache.containsKey(k)) {
            return (Map<String, String>) mapCache.get(k);
        }
        Map<String, String> mp = new HashMap<String, String>();
        Map<String, String> filedCol = getEntityFiledColumnMap(clazz);
        for (String key : filedCol.keySet()) {
            mp.put(filedCol.get(key), key);
        }
        mapCache.put(k, mp);
        return mp;
    }

    /**
     * 获取搜索参数
     * 
     * @param pg
     * @return
     */
    public static String getSearchParames(Class<?> classType, Page<?> pg, List<Object> params) {
        List<SearchFilter> filters = pg.getSearchFilter();
        String query = getSearchParames(classType, filters, params);
        if (isNotBlank(pg.getOrder())) {
            query.concat("  order by " + pg.getOrder());
        }
        return query;
    }

    public static String getSearchParames(Class<?> classType, List<SearchFilter> filters, List<Object> params) {
        StringBuilder sb = new StringBuilder();
        if (filters != null) {
            Map<String, String> filedMap = getEntityFiledColumnMap(classType);
            for (SearchFilter filter : filters) {
                Operator operator = filter.getOperator();
                String property = filter.getProperty();
                if (isNotBlank(property)) {
                    String column = filedMap.get(property);
                    if (sb.length() > 0) {
                        sb.append(" and ");
                    } else {
                        sb.append(" where ");
                    }
                    if (isNotBlank(column)) {
                        sb.append(column);
                    } else {
                        sb.append(property);
                    }
                    Object value = filter.getValue();
                    if (value == null) {
                        sb.append(" is null ");
                        continue;
                    }
                    if (operator.equals(Operator.in)) {
                        /*
                         * String val = join(value);
                         * sb.append(" in(" + val + ") ");
                         */
                        sb.append(" in(" + joinCollect(value, params) + ") ");
                    } else if (operator.equals(Operator.inSql)) {
                        sb.append(" in(" + value + ") ");
                    } else if (operator.equals(Operator.notInSql)) {
                        sb.append(" not in(" + value + ") ");
                    } else {
                        sb.append(operator.getOp());
                        params.add(value);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static boolean isNotBlank(String s) {
        if (s == null || "".equals(s.trim())) {
            return false;
        }
        return true;
    }

    private static String getCol(String col) {
        if (DataMapper.DATABSE_TYPE_ORACLE.equalsIgnoreCase(RunConfing.getConfig().getDbType())) {
            return "\"" + col.toUpperCase() + "\"";
        } else {
            return col;
        }
    }

    // 数组
    private static String joinCollect(Object cols, List<Object> params) {
        StringBuilder sbf = new StringBuilder();
        if (cols instanceof Collection) {
            for (Object v : (Collection<?>) cols) {
                sbf.append(",?");
                params.add(v);
            }
        } else if (cols instanceof Object[]) {
            for (Object v : (Object[]) cols) {
                sbf.append(",?");
                params.add(v);
            }
        } else {
            return cols.toString();
        }
        if (sbf.length() > 0) {
            sbf.deleteCharAt(0);
        }
        return sbf.toString();
    }

    /**
     * sql参数转义
     * 
     * @return
     */
    public static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char src = str.charAt(i);
            switch (src) {
                case '\'':
                    sb.append("''");
                    break;
                case '\"':
                case '\\':
                    sb.append('\\');
                default:
                    sb.append(src);
                    break;
            }
        }
        return sb.toString();
    }
}
