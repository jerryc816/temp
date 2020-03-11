package don.api.db;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

// SQLite 沒有使用者登入機制，連線只需告知要存取的db檔位置
// 下面的 sample.db 就是db檔案名稱，若無此檔案則會自動建立
// db檔路徑: 預設是在 C:\Windows\System32\ 底下
// db檔路徑: 程式加上 user.dir 是在專案根目錄底下
// 也可指定絕對路徑，如: c:\sample.db
// 程式操作上，只有取得連線的語法和其他資料庫不同
public class SQLiteUtil {
    Connection con = null;

    public SQLiteUtil() throws SQLException {
        this.con = getConnection(null);
    }

    public SQLiteUtil(String dbName) throws SQLException {
        this.con = getConnection(dbName);
    }

    // 建立連線
    public Connection getConnection(String dbName) throws SQLException {
        System.out.println("System.getProperty(\"user.dir\") = "+ System.getProperty("user.dir"));
        SQLiteConfig config = new SQLiteConfig();
        // config.setReadOnly(true);
        config.setSharedCache(true);
        config.enableRecursiveTriggers(true);

        if (dbName == null)
            dbName = "sample.db";
        SQLiteDataSource ds = new SQLiteDataSource(config);
        ds.setUrl("jdbc:sqlite:" + System.getProperty("user.dir") + File.separator + dbName); // .db 在專案目錄下
        return ds.getConnection();
        // ds.setServerName("sample.db");
    }

    // 增、刪、修
    public void exeSQL(String sql) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate(sql);
    }

    // 查詢
    public ResultSet query(String sql) throws SQLException {
        Statement stat = con.createStatement();
        ResultSet rs = stat.executeQuery(sql);
        return rs;
    }

    public String[][] queryArray(String sql) throws SQLException {
        ResultSet rs = query(sql);
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCnt = rsmd.getColumnCount();

        ArrayList<String[]> dataList = new ArrayList<>();
        while (rs.next()) {
            String row[] = new String[colCnt];
            for (int i = 1; i <= colCnt; i++) {
                row[i - 1] = rs.getString(i);
            }
            dataList.add(row);
        }

        String array[][] = new String[dataList.size()][colCnt];
        for (int i = 0; i < array.length; i++) {
            array[i] = dataList.get(i);
        }

        return array;
    }

    public void printQuery(String sql) throws SQLException {
        ResultSet rs = query(sql);
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCnt = rsmd.getColumnCount();
        while (rs.next()) {
            for (int i = 1; i <= colCnt; i++) {
                System.out.print(rs.getString(i) + "，");
            }
            System.out.println("");
        }
    }

    public static String sqliteEscape(String keyWord) {
        return keyWord
                .replace("/", "//")
                .replace("'", "''")
                .replace("[", "/[")
                .replace("]", "/]")
                .replace("%", "/%")
                .replace("&", "/&")
                .replace("_", "/_")
                .replace("(", "/(")
                .replace(")", "/)");
    }

    public String getValue(String sql) throws SQLException {
        String rtnValue = "";
        ResultSet rs = query(sql);
        if (rs.next())
            rtnValue = rs.getString(1);
        return rtnValue;
    }
    
    public void close() {
        if(con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String args[]) {
        try {
            SQLiteUtil db = new SQLiteUtil();
            
            // 建立table
            db.exeSQL("CREATE TABLE IF NOT EXISTS test(id integer, name string, date integer);");
            // 建立索引 (增or修必須有索引)
            db.exeSQL("CREATE UNIQUE INDEX IF NOT EXISTS data_idx ON test(id); ");
            
            // 新增資料
            db.exeSQL("insert into test (id,name,date) values(1,'第一筆','2018-11-30 12:30:00.00')");
            db.exeSQL("insert into test (id,name,date) values(2,'第二筆','2018-12-03 10:44:50.23')");
            db.exeSQL("insert into test (id,name,date) values(3,'第三筆',1543679999999)"); // 2018-11-31 23:59:59.999 注意!!因11月只有30天，這邊給31號，當milliseconds轉回date時，會多一天變成12/01
            db.exeSQL("insert into test (id,name) values(4,'第四筆')");
            System.out.println("新增4筆資料後:");
            db.printQuery("select * from test");

            // String[][] dataList = db.queryArray("select * from test");
            // for(String[] row : dataList) {
            // System.out.println("row = "+ row[0] +"，"+ row[1]);
            // }
            
            // 依時間查詢
            System.out.println("\n查詢日期大於1130的紀錄:");
            //db.printQuery("select * from test where date > '2018-11-30 12:30:00.00'");
            db.printQuery("select datetime(date/1000,'unixepoch','localtime') from test"); 

            // 修改資料
            db.exeSQL("update test set name = '值改變' where id = 1");
            System.out.println("\n修改第1筆資料後:");
            db.printQuery("select * from test");

            // 新增OR修改資料
            db.exeSQL("INSERT OR REPLACE INTO test VALUES (5, '第五筆', '日期時間')");
            System.out.println("\n新增OR修改第5筆(增):");
            db.printQuery("select * from test");

            db.exeSQL("INSERT OR REPLACE INTO test VALUES (5, '第五筆', 'YMD HMS')");
            System.out.println("\n新增OR修改第5筆(修):");
            db.printQuery("select * from test");
            
            // 刪除資料
            db.exeSQL("delete from test where id = 2");
            System.out.println("\n刪除第2筆資料後:");
            db.printQuery("select * from test");

            // 刪除table
            db.exeSQL("drop table test");

            db.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}