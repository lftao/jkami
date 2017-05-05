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
     * @return
     */
    public Long getTotal() {
        return total;
    }

    /**
     * 总数
     * 
     * @return
     */
    public void setTotal(Long total) {
        this.total = total;
    }

    /**
     * 当前页
     * 
     * @return
     */
    public Integer getPage() {
        return page;
    }

    /**
     * 当前页
     * 
     * @param size
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
     * @return
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
     */
    public void setSize(Integer size) {
        if (size != null) {
            this.size = size;
        }
    }

    /**
     * 结果集
     * 
     * @return
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
     * @return
     */
    public String getOrder() {
        return order;
    }

    /**
     * 排序
     * 
     * @param order
     */
    public void setOrder(String order) {
        this.order = order;
    }

    /**
     * 检索条件
     * 
     * @return
     */
    public List<SearchFilter> getSearchFilter() {
        return searchFilter;
    }

    /**
     * 检索条件
     * 
     * @param searchFilter
     */
    public void setSearchFilter(List<SearchFilter> searchFilter) {
        this.searchFilter = searchFilter;
    }

    public void addFilter(SearchFilter searchFilter) {
        getSearchFilter().add(searchFilter);
    }
}
