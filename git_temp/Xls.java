package don.api.office;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class Xls {
    public static int count = 1;

    // 2003,2007 通用
    public static ArrayList<?> load(String xlsPath) throws Exception {
        return load(xlsPath, 0);
    }
    public static ArrayList<LinkedHashMap<?, String>> load(String xlsPath, int sheetIndex) throws Exception {
        File myFile = new File(xlsPath);
        FileInputStream fis = new FileInputStream(myFile);
        
        Workbook myWorkBook = WorkbookFactory.create(fis); // 通用類
        Sheet mySheet = myWorkBook.getSheetAt(sheetIndex); // 通用類
        ArrayList<LinkedHashMap<?, String>> rows = new ArrayList<>(); // for儲存

        myWorkBook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK); // 取空白格
        DataFormatter fmt = new DataFormatter();
        for (int rn = mySheet.getFirstRowNum(); rn <= mySheet.getLastRowNum(); rn++) {
            Row row = mySheet.getRow(rn); // 通用類
            if (row == null) {
                // 空白列
            } else {
                LinkedHashMap<Integer, String> cells = new LinkedHashMap<>(); // for儲存
                for (int cell_idx = 0; cell_idx < row.getLastCellNum(); cell_idx++) {
                    Cell cell = row.getCell(cell_idx);
                    if (cell == null) {
                        cells.put(cell_idx + 1, "");
                    } else {
                        cells.put(cell_idx + 1, fmt.formatCellValue(cell));
                    }
                }
                rows.add(cells);
            }
        }
        fis.close();
        return rows;
    }
    
    public static void main(String[] args) throws Exception {
        ArrayList<?> sheets = load("C:\\test\\試算表.xls");
        System.out.println(sheets);
        sheets = load("C:\\test\\試算表.xlsx");
        System.out.println(sheets);
    }
}
