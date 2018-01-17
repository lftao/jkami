package com.javatao.jkami.proxy;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.javatao.jkami.JkException;
import com.javatao.jkami.LazyBeanHolder;
import com.javatao.jkami.Page;
import com.javatao.jkami.RunConfing;
import com.javatao.jkami.annotations.ExecuteUpdate;
import com.javatao.jkami.annotations.PageQuery;
import com.javatao.jkami.annotations.Param;
import com.javatao.jkami.annotations.ResultType;
import com.javatao.jkami.annotations.Sql;
import com.javatao.jkami.jdbc.BeanListHandle;
import com.javatao.jkami.support.DaoInterceptor;
import com.javatao.jkami.support.DataMapper;
import com.javatao.jkami.support.KaMiDaoImpl;
import com.javatao.jkami.support.KaMiDaoInterface;
import com.javatao.jkami.utils.FKParse;
import com.javatao.jkami.utils.JkBeanUtils;
import com.javatao.jkami.utils.SqlUtils;

/**
 * 代理处理类
 * 
 * @author tao
 */
public class MapperProxy<T> extends KaMiDaoImpl<T> implements InvocationHandler, Serializable, InitializingBean {
    private static final Log logger = LogFactory.getLog(MapperProxy.class);
    private static final long serialVersionUID = -3149859725082518128L;

    private DaoInterceptor daoInterceptor;
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RunConfing.bindConfing(this.getConfing());
        Object resutl = null;
        try {
            //  前置拦截
            if(daoInterceptor!=null){
                daoInterceptor.beforeInvoke(proxy, method, args);
            }
            if (method.getDeclaringClass().isAssignableFrom(KaMiDaoInterface.class)) {
                resutl = method.invoke(this, args);
            } else {
                Sql annotation = method.getAnnotation(Sql.class);
                if (annotation != null) {
                    resutl = runQuery(method, getParamMap(method, args), annotation.value());
                } else {
                    resutl = runTemplate(method, getParamMap(method, args));
                }
            }
            //后置拦截
            if(daoInterceptor!=null){
                daoInterceptor.afterInvoke(proxy, method, args,resutl);
            }
            return resutl;
        } catch (Throwable t) {
            throw new JkException(t);
        }finally{
            RunConfing.clear();
            LazyBeanHolder.clear();
        }
    }

    /**
     * 获取注解参数
     * 
     * @param method
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getParamMap(Method method, Object[] args) {
        Map<String, Object> paramMap = new HashMap<>();
        if (args == null) {
            return paramMap;
        }
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if ((parameterAnnotations == null || parameterAnnotations.length < args.length) && args.length > 0) {
            throw new JkException(this.getMapperInterface().getName() + "@Param is missing");
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Annotation[] as = parameterAnnotations[i];
            if (as.length == 0) {
                throw new JkException(method.toGenericString() + " @Param not find ");
            }
            if (arg instanceof Map) {
                Map<String, Object> mmp = (Map<String, Object>) arg;
                for (Entry<String, Object> entry : mmp.entrySet()) {
                    Object val = entry.getValue();
                    if (val instanceof String) {
                        // 转移sql防注入
                        entry.setValue(SqlUtils.escapeSql((String) val));
                    }
                }
                paramMap.putAll(mmp);
            }
            // 转移sql防注入
            if (arg instanceof String) {
                arg = SqlUtils.escapeSql((String) arg);
            }
            Param param = (Param) as[0];
            paramMap.put(param.value(), arg);
        }
        return paramMap;
    }

    /**
     * 执行sql模板
     * 
     * @param method
     * @param args
     * @return
     */
    private Object runTemplate(Method method, Map<String, Object> paramMap) {
        Class<?> _interface = this.getMapperInterface();
        String packageNme = _interface.getPackage().getName();
        String simpleName = _interface.getSimpleName();
        String methodName = method.getName();
        String sqlpath = this.getConfing().getSqlPath();
        if (sqlpath != null) {
            packageNme = sqlpath;
        } else {
            packageNme = packageNme.replace(".", "/").concat("/sql/");
        }
        String sqlTempletPath = packageNme.concat(simpleName).concat("/" + methodName + ".sql");
        logger.debug("SQL-Path:" + sqlTempletPath);
        if (FKParse.isExistTemplate(sqlTempletPath)) {
            String sql = FKParse.parseTemplate(sqlTempletPath, paramMap);
            return runQuery(method, paramMap, sql);
        } else {
            throw new JkException(sqlTempletPath + " not find ");
        }
    }

    /**
     * 执行
     * 
     * @param method
     * @param paramMap
     * @param sql
     * @return
     */
    @SuppressWarnings("unchecked")
    private Object runQuery(Method method, Map<String, Object> paramMap, String sql) {
        // 返回结果泛型
        Class<?> returnMethodType = method.getReturnType();
        Class<?> returnType = method.getReturnType();
        if ("void".equalsIgnoreCase(returnType.getName())) {
            returnType = null;
        } else if (returnType.isAssignableFrom(List.class) || returnType.isAssignableFrom(Page.class)) {
            ResultType resultType = method.getAnnotation(ResultType.class);
            if (resultType == null) {
                resultType = method.getDeclaringClass().getAnnotation(ResultType.class);
            }
            if (resultType != null) {
                returnType = resultType.value();
            } else {
                returnType = super.getClassType();
            }
        }
        List<Object> params = new ArrayList<>();
        sql = DataMapper.getMapper().placeholderSqlParam(sql, paramMap, params);
        // 是否又分页注解
        PageQuery pageQuery = method.getAnnotation(PageQuery.class);
        if (pageQuery != null || returnMethodType.isAssignableFrom(Page.class)) {
            Page page = new Page<>();
            page.setPage((Integer) paramMap.get("page"));
            page.setSize((Integer) paramMap.get("size"));
            return DataMapper.getMapper().findPage(sql, returnType, page);
        }
        // 更新插入操作
        ExecuteUpdate isUpdate = method.getAnnotation(ExecuteUpdate.class);
        if (returnType == null || isUpdate != null) {
            return DataMapper.getMapper().executeBatchUpdate(sql, params);
        }
        int maxDepth = JkBeanUtils.getMaxDepth(returnType);
        List<?> resultQuery = DataMapper.getMapper().query(sql, new BeanListHandle<>(returnType, 1, maxDepth), params);
        if (resultQuery == null) {
            return null;
        }
        if (!returnMethodType.isAssignableFrom(List.class)) {
            if (resultQuery.size() > 1) {
                throw new JkException("resultQuery size = " + resultQuery.size() + " but  method one");
            }
            if (resultQuery.size() == 1) {
                return resultQuery.get(0);
            }
        }
        if (returnMethodType.isAssignableFrom(List.class)) {
            return resultQuery;
        }
        return null;
    }

    public boolean isNotEmpty(String s) {
        if (s == null || "".equals(s)) {
            return false;
        }
        return true;
    }

    public MapperProxy() {
        super();
    }

    public void setResultType(Class<?> resultType) {
        if (resultType != null) {
            this.setClassType(resultType);
        }
    }

    public void setDaoInterceptor(DaoInterceptor daoInterceptor) {
        this.daoInterceptor = daoInterceptor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {}
}
