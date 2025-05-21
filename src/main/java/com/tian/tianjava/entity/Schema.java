package com.tian.tianjava.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

@Setter
@Getter
public class Schema implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Date date;
    private Time time;
    private Double column1;
    private Double column2;
    private Double column3;
    private Double column4;

}
