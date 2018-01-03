package com.javatao.jkami.utils;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.util.LinkedCaseInsensitiveMap;

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
    // config mapping
    private static Properties mappingEntity = new Properties();

    public enum TYPE {
        INSERT, SELECT, UPDATE
    };

    /**
     * 获取sql
     * 
     * @param clazz
     *            class
     * @param type
     *            枚举TYPE
     * @return sql
     */
    public static String getSqls(Class<?> clazz, TYPE type) {
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
     *            class
     * @return sql
     */
    public static String getSelectSqls(Class<?> clazz) {
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
     *            class
     * @return sql
     */
    public static String getInsertSqls(Class<?> clazz) {
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
     *            class
     * @return sql
     */
    public static String getUpdateSqls(Class<?> clazz) {
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

    /**
     * 初始化
     * 
     * @param clazz
     *            class
     */
    private static void initData(Class<?> clazz) {
        String k = clazz.getName();
        List<String> attrs = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        Map<String, String> cmsql = new LinkedCaseInsensitiveMap<>();
        List<Field> listField = JkBeanUtils.getAllFields(clazz);
        for (Field field : listField) {
            // 排除静态
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String fieldName = field.getName();
            if (fieldName.contains("CGLIB")) {
                continue;
            }
            Transient tr = field.getAnnotation(Transient.class);
            if (tr != null) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            String columnString = JkBeanUtils.columnToHumpReversal(fieldName);
            // 注解字段
            if (column != null) {
                columnString = column.value();
            }
            String KeyMappingColumn = k + "." + fieldName;
            // 映射字段
            String mappingColumn = mappingEntity.getProperty(KeyMappingColumn);
            if (mappingColumn != null) {
                columnString = mappingColumn;
            }
            // 注解sql
            Sql Sql = field.getAnnotation(Sql.class);
            if (Sql != null) {
                cmsql.put(fieldName, Sql.value());
                sqlforce.put(KeyMappingColumn, Sql.force());
            } else {
                // 映射字段
                String mappingColumnSql = mappingEntity.getProperty(KeyMappingColumn + "[sql]");
                if (mappingColumnSql != null) {
                    cmsql.put(fieldName, mappingColumnSql);
                    String mappingColumnSqlForce = mappingEntity.getProperty(KeyMappingColumn + "[sql][force]");
                    if (mappingColumnSqlForce == null) {
                        sqlforce.put(KeyMappingColumn, false);
                    } else {
                        sqlforce.put(KeyMappingColumn, Boolean.valueOf(mappingColumnSqlForce));
                    }
                } else {
                    attrs.add(fieldName);
                    columns.add(columnString);
                }
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
     *            class
     * @return 表名
     */
    public static String getTableName(Class<?> clazz) {
        String k = clazz.getName() + "[tableName]";
        if (mappingEntity.containsKey(k)) {
            return mappingEntity.getProperty(k);
        }
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
     *            class
     * @return 序列名字
     */
    public static String getSequenceGeneratorVal(Class<?> clazz) {
        String k = clazz.getName() + ".sequenceGenerator";
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
     * 获得主键字段<br>
     * [0] name<br>
     * [1] cloumn<br>
     * [2] type<br>
     * 
     * @param clazz
     *            class
     * @return 数组
     */
    public static Object[] getTableKey(Class<?> clazz) {
        String k = clazz.getName() + "[key]";
        if (mapCache.containsKey(k)) {
            return (Object[]) mapCache.get(k);
        }
        String KeyMapping = mappingEntity.getProperty(k);
        Object[] obj = new Object[3];
        List<Field> lsField = JkBeanUtils.getAllFields(clazz);
        for (Field field : lsField) {
            Key Key = field.getAnnotation(Key.class);
            Column column = field.getAnnotation(Column.class);
            String fieldName = field.getName();
            obj[0] = fieldName;
            obj[1] = fieldName;
            obj[2] = field.getType();
            if (Key != null || fieldName.equalsIgnoreCase(KeyMapping)) {
                if (column != null) {
                    obj[1] = column.value();
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
     *            class
     * @return 属性集合
     */
    public static List<String> getEntityAttrMp(Class<?> clazz) {
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
     *            class
     * @return db字段集合
     */
    public static List<String> getEntityColumnMp(Class<?> clazz) {
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
     * @param clazz
     *            class
     * @return sql
     */
    public static Map<String, String> getColumnSqlMp(Class<?> clazz) {
        String key = clazz.getName();
        if (!columnSqlMp.containsKey(key)) {
            initData(clazz);
        }
        return columnSqlMp.get(key);
    }

    /**
     * sql强制查询
     * 
     * @param key
     *            key
     * @return ture/false
     */
    public static Boolean getSqlForce(String key) {
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
     *            class
     * @return key属性 value注解
     */
    public static Map<String, String> getEntityFiledColumnMap(Class<?> clazz) {
        String kname = clazz.getName();
        String k = kname + "#filedColMap#";
        if (mapCache.containsKey(k)) {
            return (Map<String, String>) mapCache.get(k);
        }
        Map<String, String> mp = new LinkedCaseInsensitiveMap<>();
        List<Field> lsField = JkBeanUtils.getAllFields(clazz);
        for (Field field : lsField) {
            // 排除静态
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            String fieldName = field.getName();
            if (fieldName.indexOf("CGLIB") > -1) {
                continue;
            }
            Sql Sql = field.getAnnotation(Sql.class);
            if (Sql != null) {
                continue;
            }
            String columnString;
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                columnString = column.value();
            } else {
                // 映射字段
                String mappingColumn = mappingEntity.getProperty(kname + "." + fieldName);
                if (mappingColumn != null) {
                    columnString = mappingColumn;
                } else {
                    columnString = JkBeanUtils.columnToHumpReversal(field.getName());
                }
            }
            mp.put(field.getName(), columnString);
        }
        mapCache.put(k, mp);
        return mp;
    }

    /**
     * 获得属性和注解map
     * 
     * @param clazz
     *            class
     * @return key属性 value注解
     */
    public static Map<String, String> getEntityColumnFiledMap(Class<?> clazz) {
        String k = clazz.getName() + "#colFiledMap#";
        if (mapCache.containsKey(k)) {
            return (Map<String, String>) mapCache.get(k);
        }
        Map<String, String> mp = new LinkedCaseInsensitiveMap<>();
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
     * @param classType
     *            calss
     * @param page
     *            Page
     * @param params
     *            结果参数
     * @return sql
     */
    public static String getSearchParames(Class<?> classType, Page<?> page, List<Object> params) {
        List<SearchFilter> filters = page.getSearchFilter();
        String query = getSearchParames(classType, filters, params);
        if (isNotBlank(page.getOrder())) {
            query = query.concat("  order by " + page.getOrder());
        }
        return query;
    }

    /**
     * 获取搜索参数
     * 
     * @param classType
     *            calss
     * @param filters
     *            参数
     * @param params
     *            结果参数
     * @return sql
     */
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
                    } else if (operator.equals(Operator.concat)) {
                        sb.append(" " + value);
                    } else {
                        sb.append(operator.getOp());
                        params.add(value);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * 判断是否为空
     * 
     * @param s
     *            字符串
     * @return 结果
     */
    private static boolean isNotBlank(String s) {
        if (s == null || "".equals(s.trim())) {
            return false;
        }
        return true;
    }

    /**
     * 获取col
     * 
     * @param col
     *            字符
     * @return col
     */
    private static String getCol(String col) {
        if (DataMapper.DATABSE_TYPE_ORACLE.equalsIgnoreCase(RunConfing.getConfig().getDbType())) {
            return "\"" + col.toUpperCase() + "\"";
        } else {
            return col;
        }
    }

    /**
     * 拼接字符串
     * 
     * @param cols
     *            参数值
     * @param params
     *            结果值
     * @return 字符串
     */
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
     * @param str
     *            入参
     * @return 转以后
     */
    public static String escapeSql(String str) {
       /** if (str == null) {
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
        return sb.toString();*/
        return str;
    }

    /**
     * 加载实体映射
     * 
     * @param path
     *            路径
     */
    public static void loadConfigMapping(String path) {
        if (path == null) {
            return;
        }
        URL resource = SqlUtils.class.getResource("/");
        File mappingDir = new File(resource.getPath() + path);
        if (mappingDir.exists()) {
            loadFileMapping(mappingDir);
        }
    }

    /**
     * 加载实体映射
     * 
     * @param file
     *            文件
     */
    private static void loadFileMapping(File file) {
        try {
            if (file.isFile()) {
                String fileName = file.getName();
                if (fileName.length() > 11) {
                    FileInputStream in = new FileInputStream(file);
                    String name = fileName.substring(0, fileName.length() - 11);
                    if (name.contains(".")) {
                        Properties mapping = new Properties();
                        mapping.load(in);
                        for (Object key : mapping.keySet()) {
                            String s = key.toString();
                            if (s.startsWith("[")) {
                                s = name + key;
                            } else {
                                s = name + "." + key;
                            }
                            mappingEntity.setProperty(s, mapping.getProperty(key.toString()));
                        }
                    } else {
                        mappingEntity.load(in);
                    }
                    in.close();
                }
            } else if (file.isDirectory()) {
                File[] listFiles = file.listFiles();
                for (File fe : listFiles) {
                    loadFileMapping(fe);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取配置映射参数
     * 
     * @return
     */
    public static Properties getConfigMapping() {
        return mappingEntity;
    }
}
