package com.concurrency.fileprocessing.processor;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class XLSXProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(XLSXProcessorService.class);

    public Map<String, Map<String, Object>> process(File file) {

        logger.info("XLSX file processing started: {}", file.getName());

        Map<String, Map<String, Object>> result = new HashMap<>();
        Map<String, String[]> metadataMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet attributeSheet = workbook.getSheetAt(0);
            Sheet metadataSheet = workbook.getSheetAt(1);

            for(Row row: metadataSheet) {
                if(row.getRowNum() == 0) continue;
                String code = getCellValue(row.getCell(0));
                String name = getCellValue(row.getCell(1));
                String description = getCellValue(row.getCell(2));

                if(!code.isEmpty()) {
                    metadataMap.put(code, new String[]{name, description});
                }
            }

            List<String> headers = new ArrayList<>();

            for(Row row: attributeSheet) {
                if(row.getRowNum() == 0) {
                    for (int i = 1; i < row.getLastCellNum(); i++) {
                        String header = getCellValue(row.getCell(i)).trim();
                        headers.add(header);
                        result.put(header, new HashMap<>());
                    }
                }else {
                    String rowHeader = getCellValue(row.getCell(0)).trim();
                    for (int i = 1; i < row.getLastCellNum(); i++) {
                        String value = getCellValue(row.getCell(i)).trim();
                        if ("1".equals(value)) {
                            Map<String, Object> attribute = new HashMap<>();
                            String[] meta = metadataMap.getOrDefault(rowHeader,
                                    new String[]{"Unknown", "No description available"});

                            attribute.put("mandatory", true);
                            attribute.put("name", meta[0]);
                            attribute.put("hint", meta[1]);
                            attribute.put("type", "string");
                            attribute.put("options", "");
                            attribute.put("width", 150);
                            attribute.put("isNewColumn", true);
                            attribute.put("wordCount", 0);

                            result.get(headers.get(i - 1)).put(rowHeader, attribute);
                        }
                    }
                }
            }
            

        }catch(Exception e) {
            logger.error("Error processing XLSX file: {}", e.getMessage());
        }

        logger.info("File processing completed: {} | result columns: {}", file.getName(), result.size());
        return result;
    }

        private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue()); // assuming integers
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

}
