package com.javatao.jkami.support;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import com.javatao.jkami.Page;
import com.javatao.jkami.RunConfing;
import com.javatao.jkami.SearchFilter;
import com.javatao.jkami.jdbc.BeanListHandle;
import com.javatao.jkami.utils.JkBeanUtils;
import com.javatao.jkami.utils.SqlUtils;

public class KaMiDaoImpl<T extends Serializable> implements KaMiDaoInterface<T> {
    private DataSource dataSource;
    private Class<T> classType;
    private Class<?> mapperInterface;
    /**
     * 数据库类型
     */
    /*
     * private String dbType;
     * private boolean lazybean = true;
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
        if (confing != null) {
            confing.setDataSource(dataSource);
        }
        return this.confing;
    }

    public void setConfing(RunConfing confing) {
        this.confing = confing;
    }

    @Resource
    public final void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
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
    public List<T> findList(String sql, Class<T> result, Object... parameter) {
        int maxDepth = JkBeanUtils.getMaxDepth(result);
        List<T> list = DataMapper.getMapper().query(sql, new BeanListHandle<>(classType, 1, maxDepth));
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

    public DataSource getDataSource() {
        return dataSource;
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
    public T queryForObject(String sql, Class<T> result, Object... parameter) {
        int maxDepth = JkBeanUtils.getMaxDepth(classType);
        return DataMapper.getMapper().queryForObject(sql, result, 1, maxDepth, parameter);
    }
}
