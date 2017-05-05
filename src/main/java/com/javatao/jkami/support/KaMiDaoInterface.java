package com.javatao.jkami.support;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.javatao.jkami.Page;
import com.javatao.jkami.SearchFilter;

/**
 * 常用基础方法接口
 * 
 * @author tao
 */
public interface KaMiDaoInterface<T> {
    /**
     * 持久化实体
     * 
     * @param o
     * @return
     */
    int save(T entity);

    /**
     * 根据id查找
     * 
     * @param id
     * @return
     */
    T findById(Serializable id);

    /**
     * 查找返回对象
     * 
     * @param sqlParameter
     * @return
     */
    T queryForObject(String sql, Class<T> result, Object... parameter);

    /**
     * 根据id删除
     * 
     * @param id
     * @return
     */
    int deleteById(Serializable id);

    /**
     * 执行sql
     * 
     * @param sql
     * @param parameter
     * @return
     */
    int executeUpdate(String sql, Object... parameter);

    /**
     * 执行sql语句
     * 
     * @param sql
     * @param parameter
     * @return
     */
    List<Map<String, Object>> querySql(String sql, Object... parameter);

    /**
     * 根据参数 查询 <br/>
     * select cloum from table ?
     * 
     * @param whereSql
     *            demo where id=1
     * @return
     */
    List<T> findList(String whereSql);

    /**
     * 根据参数查找
     * 
     * @param searchFilters
     *            条件
     * @return
     */
    List<T> findList(List<SearchFilter> searchFilters);

    /**
     * 执行sql返回list
     * 
     * @param result
     *            类型
     * @param parameter
     *            参数
     * @return
     */
    List<T> findList(String sql, Class<T> result, Object... parameter);

    /**
     * 根据id更新
     * 
     * @param o
     * @return
     */
    int updateById(T entity);

    /**
     * 更新非空字段
     * 
     * @param o
     * @return
     */
    int updateNotNullById(T entity);

    /**
     * 分页查询
     * 
     * @param page
     * @return
     */
    Page<T> findPage(Page<T> page);
}
