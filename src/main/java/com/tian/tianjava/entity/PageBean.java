package com.tian.tianjava.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

//分页查询结果封装类
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer total; //总记录数
    private List<Schema> rows;  //数据列表

    public List<Schema> getRows() {
        return rows;
    }

    public void setRows(List<Schema> rows) {
        this.rows = rows;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
