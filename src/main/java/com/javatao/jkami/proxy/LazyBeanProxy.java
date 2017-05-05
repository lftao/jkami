package com.javatao.jkami.proxy;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import com.javatao.jkami.LazyBeanHolder;
import com.javatao.jkami.RunConfing;
import com.javatao.jkami.annotations.ResultType;
import com.javatao.jkami.jdbc.BeanListHandle;
import com.javatao.jkami.support.DataMapper;
import com.javatao.jkami.utils.JkBeanUtils;
import com.javatao.jkami.utils.SqlUtils;

/**
 * 懒加载bean
 * 2015.11.19
 * 
 * @author TLF
 */
public class LazyBeanProxy implements MethodInterceptor, Serializable {
    private static final long serialVersionUID = 8838716051758899600L;
    private static final Log logger = LogFactory.getLog(LazyBeanProxy.class);
    private static final String SET = "set";
    private static final String GET = "get";
    private transient int _curentdepth = 1;
    private transient int _maxDepth = 2;
    private transient RunConfing config;
    private transient Set<String> isLoad = new HashSet<>();
    private transient Set<String> notLazy = new HashSet<>();

    public LazyBeanProxy(RunConfing config, int _curentdepth, int maxDepth) {
        super();
        this.config = config;
        this._curentdepth = _curentdepth;
        this._maxDepth = maxDepth;
        notLazy = LazyBeanHolder.get();
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Class<?> classType = obj.getClass().getSuperclass();
        String methodName = method.getName();
        if (methodName.indexOf(GET) > -1) {
            // 字段名
            String name = methodName.substring(3);
            String prop = JkBeanUtils.firstToMix(name);
            // 对象包含sql
            Map<String, String> columnSqlMp = SqlUtils.getColumnSqlMp(classType);
            if (!columnSqlMp.isEmpty()) {
                if (_curentdepth > _maxDepth) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("LazyBeanProxy skip is maxDepth " + _maxDepth);
                    }
                    return null;
                }
                notLazy.addAll(LazyBeanHolder.get());
                if (columnSqlMp.containsKey(prop) && !isLoad.contains(prop) && !notLazy.contains(prop)) {
                    int now_depth = new Integer(_curentdepth) + 1;
                    DataMapper mapper = DataMapper.getMapper();
                    RunConfing.bindConfing(config);
                    if (logger.isDebugEnabled()) {
                        logger.debug("LazyBean load " + classType.getName() + ">" + prop);
                    }
                    String sql = columnSqlMp.get(prop);
                    List<Object> params = new ArrayList<>();
                    // 组装sql+参数
                    sql = mapper.placeholderSqlParam(sql, params, obj);
                    Field field = JkBeanUtils.getObjField(obj.getClass(), prop);
                    Class<?> type = field.getType();
                    ResultType result = field.getAnnotation(ResultType.class);
                    if (result != null) {
                        type = result.value();
                    }
                    List<?> oval = mapper.query(sql, new BeanListHandle<>(type, now_depth, _maxDepth), params);
                    JkBeanUtils.setProperty(obj, prop, oval);
                    Boolean force = SqlUtils.getSqlForce(classType.toString() + prop);
                    if (!force) {
                        isLoad.add(prop);
                    }
                    RunConfing.clear();
                }
            }
        } else if (methodName.indexOf(SET) > -1) {
            String name = methodName.substring(3);
            String prop = JkBeanUtils.firstToMix(name);
            Boolean force = SqlUtils.getSqlForce(classType.getName() + prop);
            if (!force) {
                isLoad.add(prop);
            }
        }
        return proxy.invokeSuper(obj, args);
    }
}
