package don.api.office;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

// POI 4.0+      JDK 1.8 or later.
// POI 3.11+     JDK 1.6 or later.
// POI 3.5~10    JDK 1.5 or later. 
// https://www.cnblogs.com/biehongli/p/7495053.html
public class XlsWriter {

    // 標題名稱
    private static String[] columns = { "Name", "Email", "Salary", "Birthday"};
    // 資料列
    private static List<HashMap<String,String>> rows = new ArrayList<>();

    // 設置要寫入excel的資料
    static {
        // TODO map 改用 LinkedHashMap 就可以不用另外設置標題列 columns[]
        HashMap<String,String> row1 = new HashMap<String,String>();
        row1.put("Name", "Rajeev");
        row1.put("Email", "rajeev@example.com");
        row1.put("Salary", "35000.0");
        row1.put("Birthday", "1988/05/15");
        rows.add(row1);
        
        HashMap<String,String> row2 = new HashMap<String,String>();
        row2.put("Name", "Thomas");
        row2.put("Email", "thomas@example.com");
        row2.put("Salary", "43500.0");
        row2.put("Birthday", "1975/02/07");
        rows.add(row2);
    }

    public static void main(String[] args) {
        String xlsPath = "poi-generated-file.xlsx";
        String tabName = "Sheet1";
        XlsWriter xlswriter = new XlsWriter();
        
        // 測試寫入資料
        xlswriter.writeToTab(xlsPath, tabName, rows);

        // 測試寫入已存在的tab, x指定列(改為自動算列數)疊加資料
        xlswriter.writeToTab(xlsPath, tabName, rows);
        
        // 測試寫入已存在的xls, 新增tab
        tabName = "Sheet2";
        xlswriter.writeToTab(xlsPath, tabName, rows);
    }
          
    // TODO 指定起始欄(cell)   
    public void writeToTab(String xlsPath, String tabName, List<HashMap<String,String>> rows) {
        try {
            // 讀取xls檔，若不存在則新建
            Workbook workbook;
            if(new File(xlsPath).exists()) {
                FileInputStream fileIn = new FileInputStream(xlsPath);
                workbook = WorkbookFactory.create(fileIn);   
                fileIn.close();
            } else {
                workbook = new XSSFWorkbook();                
            }
            
            // 讀取工作表(頁籤)，若不存在則新建
            Sheet sheet = workbook.getSheet(tabName);
            if(sheet == null) {
                sheet = workbook.createSheet(tabName);
            } 
            
            // 算起始列，若已有資料自動累加到最後一列+2 (插入一個空白列)
            int startRow = sheet.getLastRowNum();
            if(startRow != 0) startRow += 2; 

            // 使用 CreationHelper 建立日期格式、超連結、RichTextString...etc
            CreationHelper createHelper = workbook.getCreationHelper();

            // 字型樣式(for標題列)
            Font headerFont = workbook.createFont();
            headerFont.setBoldweight((short) 3); // 字粗
            headerFont.setFontHeightInPoints((short) 14); // 字高
            //headerFont.setColor(IndexedColors.RED.getIndex()); // 顏色
            
            // 儲存格樣式(for標題列)
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont); // 字型           
            
            // 背景色-色碼表 https://blog.csdn.net/DrifterJ/article/details/46662277
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); // 背景色
            headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND); // 背景填滿
            
            // 儲存格樣式(for日期格式)
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

            // TODO 待測樣式
            // https://kknews.cc/zh-tw/career/q5z9ejr.html
            
            // 建立第一列
            Row headerRow = sheet.createRow(startRow);

            // 建立第一列的儲存格
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // 建立其他列和儲存格 & 塞資料
            int rowNum = startRow + 1;
            for (HashMap<String,String> datarow : rows) {
                Row row = sheet.createRow(rowNum++);

                for (int i = 0; i < columns.length; i++) {
                    row.createCell(i).setCellValue(datarow.get(columns[i]));
                }
            }

            // 重設儲存格大小，以符合內容寬度
            // for (int i = 0; i < columns.length; i++) {
            //     sheet.autoSizeColumn(i);
            // }
            
            // 設定欄寬
            sheet.setColumnWidth(0, 10*256);
            sheet.setColumnWidth(1, 20*256);
            sheet.setColumnWidth(2, 10*256);
            sheet.setColumnWidth(3, 15*256);

            // 凍結窗格
            //sheet.createFreezePane(3, 1, 5, 2); // 在研究
            sheet.createFreezePane(0, 1); // 凍結首列
            
            // 輸出到檔案中，未指定路徑則在專案根目錄中
            FileOutputStream fileOut = new FileOutputStream(xlsPath);
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            //workbook.close(); 
            System.out.println("XlsWriter.writeToTab ["+ tabName +"] finish");
        } catch (Exception e) {
            System.out.println("XlsWriter.writeToTab ["+ tabName +"] error");
            e.printStackTrace();
        }
    }
}