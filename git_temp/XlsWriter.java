package don.api.office;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// POI 4.0+      JDK 1.8 or later.
// POI 3.11+     JDK 1.6 or later.
// POI 3.5~10    JDK 1.5 or later. 
// https://www.cnblogs.com/biehongli/p/7495053.html
public class XlsWriter {

    // 標題名稱
    private static String[] columns = { "Name", "Email", "Date Of Birth", "Salary" };
    // 資料列
    private static List<Datarow> rows = new ArrayList<>();

    // 設置要寫入excel的資料
    static {
        Calendar date = Calendar.getInstance();
        date.set(1992, 7, 21);
        rows.add(new Datarow("Rajeev", "rajeev@example.com", date.getTime(), 1200000.0));

        date.set(1965, 10, 15);
        rows.add(new Datarow("Thomas", "thomas@example.com", date.getTime(), 1500000.0));

        date.set(1987, 4, 18);
        rows.add(new Datarow("Steve", "steve@example.com", date.getTime(), 1800000.0));
    }

    public static void main(String[] args) throws IOException, InvalidFormatException {
        // 建立活頁簿 (.xls檔)
        Workbook workbook = new XSSFWorkbook();

        // 使用 CreationHelper 建立日期格式、超連結、RichTextString...etc
        CreationHelper createHelper = workbook.getCreationHelper();

        // 建立工作表 (一個tab)
        Sheet sheet = workbook.createSheet("Sheet1");

        // 建立字型樣式 for 標題列
        Font headerFont = workbook.createFont();
        headerFont.setBoldweight((short) 3); // 字粗
        headerFont.setFontHeightInPoints((short) 14); // 字高
        headerFont.setColor(IndexedColors.RED.getIndex()); // 顏色

        // 將字型樣式，設入儲存格
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // 建立第一列
        Row headerRow = sheet.createRow(0);

        // 建立第一列的儲存格
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // 建立日期格式的儲存格
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

        // 建立其他列和儲存格 & 塞資料
        int rowNum = 1;
        for (Datarow datarow : rows) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(datarow.getName());
            row.createCell(1).setCellValue(datarow.getEmail());

            Cell dateCell = row.createCell(2);
            dateCell.setCellValue(datarow.getDate());
            dateCell.setCellStyle(dateCellStyle);

            row.createCell(3).setCellValue(datarow.getSalary());
        }

        // 重設儲存格大小，以符合內容寬度
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 輸出到檔案中，未指定路徑則在專案根目錄中
        FileOutputStream fileOut = new FileOutputStream("poi-generated-file.xlsx");
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        //workbook.close();
    }
}