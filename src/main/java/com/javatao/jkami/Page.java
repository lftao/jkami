package com.javatao.jkami;

import java.util.ArrayList;
import java.util.List;

import com.javatao.jkami.annotations.Key;

/**
 * 分页实体
 * 
 * @author tao
 */
public class Page<T> implements java.io.Serializable {
    private static final long serialVersionUID = -3011180843826014135L;
    @Key
    private Integer page = 1;// 当前页
    private Long total = 0l;// 总数
    private Integer size = 10;// 每页数量
    private List<T> rows = new ArrayList<>();
    private String order;// 排序
    private List<SearchFilter> searchFilter = new ArrayList<>();// 检索条件

    /**
     * 总数
     * 
     * @return 总数
     */
    public Long getTotal() {
        return total;
    }

    /**
     * 总数
     * 
     * @param total
     *            总数
     */
    public void setTotal(Long total) {
        this.total = total;
    }

    /**
     * 当前页
     * 
     * @return 当前页
     */
    public Integer getPage() {
        return page;
    }

    /**
     * 当前页
     * 
     * @param page
     *            页数
     */
    public void setPage(Integer page) {
        if (page == null || page <= 0) {
            page = 1;
        }
        this.page = page;
    }

    /**
     * 每页数量
     * 
     * @return 每页数量
     */
    public Integer getSize() {
        if (size == null) {
            size = 10;
        }
        return size;
    }

    /**
     * 每页数量
     * 
     * @param size
     *            每页数量
     */
    public void setSize(Integer size) {
        if (size != null) {
            this.size = size;
        }
    }

    /**
     * 结果集
     * 
     * @return 结果集合
     */
    public List<T> getRows() {
        if (rows == null) {
            setRows(new ArrayList<T>());
        }
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    /**
     * 排序
     * 
     * @return 排序
     */
    public String getOrder() {
        return order;
    }

    /**
     * 排序
     * 
     * @param order
     *            排序
     */
    public void setOrder(String order) {
        this.order = order;
    }

    /**
     * 检索条件
     * 
     * @return 获取检索条件
     */
    public List<SearchFilter> getSearchFilter() {
        return searchFilter;
    }

    /**
     * 检索条件
     * 
     * @param searchFilter
     *            设置检索条件
     */
    public void setSearchFilter(List<SearchFilter> searchFilter) {
        this.searchFilter = searchFilter;
    }

    /**
     * 添加检索条件
     * 
     * @param searchFilter
     *            条件
     */
    public void addFilter(SearchFilter searchFilter) {
        getSearchFilter().add(searchFilter);
    }
}
