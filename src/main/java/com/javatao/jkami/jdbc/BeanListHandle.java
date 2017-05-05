package com.javatao.jkami.jdbc;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.javatao.jkami.RunConfing;
import com.javatao.jkami.annotations.ResultType;
import com.javatao.jkami.proxy.LazyBeanProxy;
import com.javatao.jkami.support.DataMapper;
import com.javatao.jkami.utils.JkBeanUtils;
import com.javatao.jkami.utils.SqlUtils;
import com.javatao.jkami.utils.JkBeanUtils.PsKey;

/**
 * list 结果集
 * 
 * @author tao
 * @param <T>
 */
public class BeanListHandle<T> implements ResultHandle<List<T>> {
    private static final Log logger = LogFactory.getLog(BeanListHandle.class);
    private final String SPLIT = "_";
    private Class<T> clazz;
    private int _depth;
    private int _maxDepth;

    public BeanListHandle(Class<T> clazz, int depth, int maxDepth) {
        this.clazz = clazz;
        this._depth = depth;
        this._maxDepth = maxDepth;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> handle(ResultSet rs) {
        if (_depth > _maxDepth) {
            logger.debug("Bean load is max Depth ");
            return null;
        }
        RunConfing config = RunConfing.getConfig();
        // 返回map对象
        ResultHandle<List<Map<String, Object>>> handle = new MapListHandle();
        List<Map<String, Object>> outMap = handle.handle(rs);
        if (clazz.isAssignableFrom(Map.class)) {
            return (List<T>) outMap;
        }
        List<T> resultlist = new ArrayList<T>();
        final Map<String, String> filedMap = SqlUtils.getEntityColumnFiledMap(clazz);
        // 处理map 包装实体
        for (Map<String, Object> map : outMap) {
            try {
                T o = null;
                if (!JkBeanUtils.isBeanClass(clazz)) {
                    Object val = map.values().iterator().next();
                    o = clazz.getConstructor(String.class).newInstance(String.valueOf(val));
                    resultlist.add(o);
                    continue;
                }
                // 懒加载开启
                if (config.isLazybean()) {
                    org.springframework.cglib.proxy.Enhancer enhancer = new org.springframework.cglib.proxy.Enhancer();
                    enhancer.setSuperclass(clazz);
                    enhancer.setCallback(new LazyBeanProxy(config, _depth, _maxDepth));
                    o = (T) enhancer.create();
                } else {
                    o = clazz.newInstance();
                }
                o = (T) JkBeanUtils.mapToObject(map, o, new PsKey() {
                    public String before(String key) {
                        String pname = filedMap.get(key);
                        if (pname != null) {
                            key = pname;
                        }
                        if (key.indexOf(SPLIT) > -1) {
                            // 转换对象属性名字
                            key = JkBeanUtils.columnToHump(key);
                        }
                        return key;
                    }
                });
                resultlist.add(o);
                // 非懒加载
                if (!config.isLazybean()) {
                    // 对象包含sql
                    Map<String, String> columnSqlMp = SqlUtils.getColumnSqlMp(clazz);
                    if (!columnSqlMp.isEmpty()) {
                        int now_depth = new Integer(_depth) + 1;
                        DataMapper mapper = DataMapper.getMapper();
                        for (Entry<String, String> entry : columnSqlMp.entrySet()) {
                            String name = entry.getKey();
                            String sql = entry.getValue();
                            // JkBeanUtils.columnToHump(k)
                            List<Object> params = new ArrayList<>();
                            // 组装sql+参数
                            sql = mapper.placeholderSqlParam(sql, params, o);
                            Field field = JkBeanUtils.getObjField(clazz, name);
                            Class<?> type = field.getType();
                            ResultType result = field.getAnnotation(ResultType.class);
                            if (result != null) {
                                type = result.value();
                            }
                            List<?> oval = mapper.query(sql, new BeanListHandle<>(type, now_depth, _maxDepth), params);
                            JkBeanUtils.setProperty(o, name, oval);
                        }
                    }
                    
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return resultlist;
    }
}
