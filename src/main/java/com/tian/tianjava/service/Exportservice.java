package com.tian.tianjava.service;

import com.tian.tianjava.entity.Exportdate;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service

public class Exportservice {

    public void exportToExcel(HttpServletResponse response) throws IOException{
        String fileName = "export.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        List<Exportdate> Exportdates = getExportdate(); // 获取数据

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Data");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Day");
            headerRow.createCell(2).setCellValue("Time");

            // 填充数据行
            int rowNum = 1;
            for (Exportdate exportdate : Exportdates) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(exportdate.getId());
                LocalDate day = exportdate.getDay();
                DateTimeFormatter formatterday = DateTimeFormatter.ofPattern("yy:MM:dd");
                String formattedDate = day.format(formatterday);
                row.createCell(1).setCellValue(formattedDate);
                //化为字符串
                LocalTime time = exportdate.getTime();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String formattedTime = time.format(formatter);
                row.createCell(2).setCellValue(formattedTime);
            }

            workbook.write(response.getOutputStream());
        }
    }

    public List<Exportdate> getExportdate(){
        // 模拟从数据库或其他数据源获取数据
        List<Exportdate> Exportdates = new ArrayList<>();
        Exportdates.add(new Exportdate(1, LocalDate.of(2024, 5,26), LocalTime.of(0,0,0)));
        Exportdates.add(new Exportdate(2, LocalDate.of(2024, 5,26), LocalTime.of(0,0,10)));
        Exportdates.add(new Exportdate(3, LocalDate.of(2024, 5,26), LocalTime.of(0,0,20)));
        return Exportdates;
    }

}
