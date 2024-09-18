package org.kxl.home.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExcelUtil {

    public static List<List<Object>> read(File excelFile, Integer whichSheet) {
        String traceId = "";
        String site = "";
        String guid = "";
        String method = "read excel";

        List<List<Object>> list = null;
        FileInputStream file = null;
        Workbook workbook = null;
        try {
            String fileName = excelFile.getName();
            file = new FileInputStream(excelFile);
            if(fileName.endsWith("xls")) {
                workbook = new HSSFWorkbook(file);
            } else if(fileName.endsWith("xlsx")) {
                workbook = new XSSFWorkbook(file);
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
            return list;
        } catch(IOException e) {
            e.printStackTrace();
            return list;
        }
        Sheet sheet = workbook.getSheetAt(whichSheet);
        list = new ArrayList<List<Object>>();
        for(Row row : sheet) {
            List<Object> rowList = new ArrayList<>();
            for(Cell cell : row) {
                switch(cell.getCellType()) {
                    case STRING:
                        rowList.add(cell.getStringCellValue());
                        break;
                    case NUMERIC:
                        rowList.add(cell.getNumericCellValue());
                        break;
                    case BOOLEAN:
                        rowList.add(cell.getBooleanCellValue());
                        break;
                    case FORMULA:
                        rowList.add(cell.getCellFormula());
                        break;
                    default:
                        rowList.add("");
                        break;
                }
            }
            list.add(rowList);
        }
        try {
            workbook.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void write(File excelFile, Integer whichSheet, List<Map<Integer, Object>> contents) {
        String traceId = "";
        String site = "";
        String guid = "";
        String method = "write excel";

        List<List<Object>> list = null;
        FileInputStream file = null;
        Workbook workbook = null;
        try {
            String fileName = excelFile.getName();
            file = new FileInputStream(excelFile);
            if(fileName.endsWith("xls")) {
                workbook = new HSSFWorkbook(file);
            } else if(fileName.endsWith("xlsx")) {
                workbook = new XSSFWorkbook(file);
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
        Sheet sheet = workbook.getSheetAt(whichSheet);
        int rowNum = 0;
        for(Map<Integer, Object> content : contents) {
            Row row = sheet.getRow(rowNum);
            if(row == null) {
                row = sheet.createRow(rowNum);
            }
            ++rowNum;
            if(content.isEmpty()) continue;
            for(Map.Entry<Integer, Object> entry : content.entrySet()) {
                int columnNum = entry.getKey();
                Object value = entry.getValue();
                Cell cell = null;
                try {
                    cell = row.getCell(columnNum);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                if(cell == null) {
                    cell = row.createCell(columnNum);
                }
                if(value != null) {
                    cell.setCellValue(value.toString());
                }
            }
        }

        try {
            FileOutputStream out = new FileOutputStream(excelFile);
            workbook.write(out);
            out.close();
            // 关闭工作簿
            workbook.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        File file = new File("C:/Users/Administrator/Downloads/USPS新API包裹追踪-结果.xlsx");
        List<List<Object>> contents = read(file, 0);

        File resultFile = new File("C:/Users/Administrator/Downloads/USPS新API包裹追踪-结果 - 副本.xlsx");
        Workbook workbook = new XSSFWorkbook(new FileInputStream(resultFile));


        XSSFColor blue = new XSSFColor(new java.awt.Color(0, 255, 196), new DefaultIndexedColorMap());
        Font font = workbook.createFont();
        font.setBold(true);
        font.setItalic(true);

        CellStyle blueStyle = workbook.createCellStyle();
        blueStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        blueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle orangeStyle = workbook.createCellStyle();
        orangeStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        orangeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle currentStyle = null;

        int s = 0;
        Sheet sheet = workbook.getSheetAt(0);
        String id = "";
        int rowNum = 0;
        for(List<Object> content : contents) {
            if(!Objects.equals(content.get(0).toString(), id)) {
                id = content.get(0).toString();
                s = ++s % 2;
            }
            currentStyle = s == 0 ? blueStyle : orangeStyle;
            Row row = sheet.createRow(rowNum);
            int cellNum = 0;
            for(Object rowTxt : content) {
                Cell cell = row.createCell(cellNum);
                cell.setCellValue(rowTxt.toString());
                cell.setCellStyle(currentStyle);
                ++cellNum;
            }
            ++rowNum;
        }

        FileOutputStream out = new FileOutputStream(resultFile);
        workbook.write(out);
        // 关闭工作簿
        workbook.close();
        out.close();


    }
}
