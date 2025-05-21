package com.tian.tianjava.controller;

import com.tian.tianjava.entity.Exportdate;
import com.tian.tianjava.service.Exportservice;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController()

@Validated

public class Exportcontroller{

    @Autowired
    public Exportservice exportservice;

    @GetMapping("/download")
    public void exportToExcel(HttpServletResponse response) throws IOException{
        exportservice.exportToExcel(response);
    }

    @GetMapping("/test")
    public List<Exportdate>test(){
        return exportservice.getExportdate();
    }

}