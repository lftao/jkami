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
     * @param entity
     *            实体
     * @return 变更值
     */
    int save(T entity);

    /**
     * 根据id查找
     * 
     * @param id
     *            标识符
     * @return 实体
     */
    T findById(Serializable id);

    /**
     * 查找返回对象
     * 
     * @param sql
     *            sql
     * @param result
     *            结果类型
     * @param parameter
     *            参数
     * @return 实体
     */
    T queryForObject(String sql, Class<T> result, Object... parameter);

    /**
     * 根据id删除
     * 
     * @param id
     *            标识符
     * @return 变更值
     */
    int deleteById(Serializable id);

    /**
     * 执行sql
     * 
     * @param sql
     *            sql
     * @param parameter
     *            参数
     * @return 变更值
     */
    int executeUpdate(String sql, Object... parameter);

    /**
     * 执行sql语句
     * 
     * @param sql
     *            sql
     * @param parameter
     *            参数
     * @return 结果集合
     */
    List<Map<String, Object>> querySql(String sql, Object... parameter);

    /**
     * 根据参数 查询 <br>
     * select cloum from table ?
     * 
     * @param whereSql
     *            demo where id=1
     * @return 结果集合
     */
    List<T> findList(String whereSql);

    /**
     * 根据参数查找
     * 
     * @param searchFilters
     *            条件集合
     * @return 结果集合
     */
    List<T> findList(List<SearchFilter> searchFilters);

    /**
     * 执行sql返回list
     * 
     * @param sql
     *            执行sql
     * @param result
     *            类型
     * @param parameter
     *            参数
     * @return 结果集合
     */
    List<T> findList(String sql, Class<T> result, Object... parameter);

    /**
     * 根据id更新
     * 
     * @param entity
     *            实体
     * @return 变更值
     */
    int updateById(T entity);

    /**
     * 更新非空字段
     * 
     * @param entity
     *            实体
     * @return 变更值
     */
    int updateNotNullById(T entity);

    /**
     * 分页查询
     * 
     * @param page
     *            分页对象
     * @return 分页实例
     */
    Page<T> findPage(Page<T> page);
}
