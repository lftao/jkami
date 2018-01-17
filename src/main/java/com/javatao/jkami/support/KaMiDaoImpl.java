package com.javatao.jkami.support;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.javatao.jkami.Page;
import com.javatao.jkami.RunConfing;
import com.javatao.jkami.SearchFilter;
import com.javatao.jkami.jdbc.BeanListHandle;
import com.javatao.jkami.utils.JkBeanUtils;
import com.javatao.jkami.utils.SqlUtils;

/**
 * 默认接口实现类
 * 
 * @author tao
 * @param <T>
 *            类型
 */
public class KaMiDaoImpl<T> implements KaMiDaoInterface<T>, ApplicationContextAware {
    private Class<T> classType;
    private Class<?> mapperInterface;
    /**
     * 运行时参数
     */
    private RunConfing confing;

    @SuppressWarnings("unchecked")
    public KaMiDaoImpl() {
        classType = (Class<T>) geType();
    }

    protected Class<?> geType() {
        int index = 0;
        Type genType = getClass().getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            throw new RuntimeException("Index outof bounds");
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class<?>) params[index];
    }

    public RunConfing getConfing() {
        return this.confing;
    }

    public void setConfing(RunConfing confing) {
        this.confing = confing;
    }

    @Override
    public int save(T o) {
        return DataMapper.getMapper().save(o);
    }

    @Override
    public T findById(Serializable id) {
        return DataMapper.getMapper().findById(id, classType);
    }

    @Override
    public int updateById(T o) {
        return DataMapper.getMapper().updateById(o);
    }

    @Override
    public int deleteById(Serializable id) {
        return DataMapper.getMapper().deleteById(id, classType);
    }

    @Override
    public int executeUpdate(String sql, Object... parameter) {
        return DataMapper.getMapper().executeUpdate(sql, parameter);
    }

    @Override
    public List<T> findList(String whereSql) {
        String sql = SqlUtils.getSqls(classType, SqlUtils.TYPE.SELECT);
        if (whereSql != null) {
            sql = sql + " " + whereSql;
        }
        int maxDepth = JkBeanUtils.getMaxDepth(classType);
        List<T> list = DataMapper.getMapper().query(sql, new BeanListHandle<>(classType, 1, maxDepth));
        return list;
    }

    @Override
    public List<T> findList(List<SearchFilter> searchFilter) {
        String sql = SqlUtils.getSqls(classType, SqlUtils.TYPE.SELECT);
        int maxDepth = JkBeanUtils.getMaxDepth(classType);
        List<Object> params = new ArrayList<>();
        String sqlParam = SqlUtils.getSearchParames(classType, searchFilter, params);
        sql += sqlParam;
        List<T> list = DataMapper.getMapper().query(sql, new BeanListHandle<>(classType, 1, maxDepth), params);
        return list;
    }

    @Override
    public <K> List<K> findList(String sql, Class<K> result, Object... parameter) {
        int maxDepth = JkBeanUtils.getMaxDepth(result);
        List<K> list = DataMapper.getMapper().query(sql, new BeanListHandle<>(result, 1, maxDepth));
        return list;
    }

    @Override
    public int updateNotNullById(T o) {
        return DataMapper.getMapper().updateNotNullById(o);
    }

    @SuppressWarnings("unchecked")
    public void setClassType(Class<?> resultType) {
        this.classType = (Class<T>) resultType;
    }

    public Class<?> getMapperInterface() {
        return mapperInterface;
    }

    public void setMapperInterface(Class<?> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Page<T> findPage(Page<T> pagevo) {
        return DataMapper.getMapper().findPage(null, classType, pagevo);
    }

    @Override
    public List<Map<String, Object>> querySql(String sql, Object... parameter) {
        return DataMapper.getMapper().queryForMap(sql, parameter);
    }

    @Override
    public <K> K queryForObject(String sql, Class<K> result, Object... parameter) {
        int maxDepth = JkBeanUtils.getMaxDepth(result);
        return DataMapper.getMapper().queryForObject(sql, result, 1, maxDepth, parameter);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (confing != null) {
            DataSource dataSource = confing.getDataSource();
            if (dataSource == null) {
                String dataSourceId = confing.getDataSourceId();
                if (dataSourceId != null) {
                    DataSource source = applicationContext.getBean(dataSourceId, DataSource.class);
                    confing.setDataSource(source);
                }
            }
        }
    }
    
    public Class<?> getClassType() {
        return classType;
    }
}
