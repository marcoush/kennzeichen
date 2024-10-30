package com.example.kennzeichen;

import android.util.Log;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.*;

public class ExcelDataReader {
    private static String TAG = "ExcelDataReader";

    public Map<String, List<String[]>> readExcelFile(InputStream inputStream) {
        Map<String, List<String[]>> dataMap = new HashMap<>();

        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            for (Row currentRow : sheet) {
                String numberPlate, town, bundesland;
                if (currentRow.getCell(0).getCellType() == CellType.NUMERIC) numberPlate = String.valueOf(currentRow.getCell(0).getNumericCellValue());
                else numberPlate = currentRow.getCell(0).getStringCellValue();
                //Log.d(TAG,"numberplate:"+numberPlate); //deubg
                if (currentRow.getCell(1).getCellType() == CellType.NUMERIC) town = String.valueOf(currentRow.getCell(0).getNumericCellValue());
                else town = currentRow.getCell(1).getStringCellValue();
                if (currentRow.getCell(2).getCellType() == CellType.NUMERIC) bundesland = String.valueOf(currentRow.getCell(0).getNumericCellValue());
                else bundesland = currentRow.getCell(2).getStringCellValue();

                List<String[]> townBundeslandList = dataMap.getOrDefault(numberPlate, new ArrayList<>());
                townBundeslandList.add(new String[]{town, bundesland});
                dataMap.put(numberPlate, townBundeslandList);
            }

            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataMap;
    }
}
