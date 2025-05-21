package com.tian.tianjava.entity;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import java.time.LocalTime;

@Setter
@Getter
public class Exportdate {
    private Integer id;
    private LocalDate day;
    private LocalTime time;

    public Exportdate(Integer id, LocalDate day, LocalTime time) {
        this.id = id;
        this.day = day;
        this.time = time;
    }

}
