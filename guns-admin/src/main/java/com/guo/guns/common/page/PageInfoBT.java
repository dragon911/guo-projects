package com.guo.guns.common.page;

import com.github.pagehelper.Page;

import java.util.List;

/**
 * 分页结果的封装(for Bootstrap Table)
 * 
 * @Author guo
 * @Date 2018-03-04 13:47
 */
public class PageInfoBT<T> {

    /**
     *  结果集
     */
    private List<T> rows;

    /**
     * 总数
     */
    private long total;

    public PageInfoBT(List<T> page) {
        this.rows = page;
        if (page instanceof Page) {
            this.total = ((Page) page).getTotal();
        } else {
            this.total = page.size();
        }
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
