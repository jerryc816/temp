/**
 *
 * @author ardon
 */
package don.api.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


import don.api.Cmn;

public class Database {
    private static Logger logger = Logger.getLogger(Database.class);
    
    //private static final Database instance = new Database(); // 一般單例模式
    private static ConcurrentHashMap<String,Database> instanceMap = new ConcurrentHashMap<>(); // 傳參數的單例模式
    
    public Connection con = null;
    public Statement stmt = null;
    public String dbType = "";

    // ===== 實例化 =============================================================================
    public static Database getInstance() {
        return getInstance("prop.test");
    }

    public static Database getInstance(String profile) {
        if(instanceMap.get(profile) == null) {
            instanceMap.put(profile, new Database(profile));
        }
        return instanceMap.get(profile);
    }
    
    // ===== 建立連結 ============================================================================
    public Connection getConn() {
        if (this.con == null) {
            return getConn("prop.test");
        }
        return this.con;
    }

    public Connection getConn(String profile) {
        if (this.con == null) {
            try {
                ResourceBundle rb = ResourceBundle.getBundle(profile);
                String url = rb.getString("jdbc.url");
                String driver = rb.getString("jdbc.driver");
                String username = rb.getString("jdbc.username");
                String password = rb.getString("jdbc.password");
                this.dbType = driver;

                Class.forName(driver).newInstance();
                this.con = java.sql.DriverManager.getConnection(url, username, password);
            } catch (Exception e) {
                logger.error(e);
                logger.error(profile);
            }
        }
        return this.con;
    }

    //===== 建構子 ==============================================================================
    private Database() {
        this.con = getConn();
    }

    private Database(String profile) {
        this.con = getConn(profile);
    }

    /*private Database(String url, String id, String pw) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            this.con = java.sql.DriverManager.getConnection(url, id, pw);
        } catch (Exception e) {
            logger.error(e);
        }
    }*/  
    
    //===================================================================================
    public java.sql.ResultSet getQuery(String sql) {
        java.sql.ResultSet rs = null;
        try {
            this.stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (Exception e) {
            logger.error(e);
            logger.error(sql);
        }
        return rs;
    }

    /**
     * @param sql query syntax
     * @param TYPE ResultSet.TYPE_FORWARD_ONLY(default)、ResultSet.TYPE_SCROLL_INSENSITIVE、ResultSet.TYPE_SCROLL_SENSITIVE
     * @param CONCUR ResultSet.CONCUR_READ_ONLY(default)、ResultSet.CONCUR_UPDATABLE
     * @return java.sql.ResultSet
     */    
    public java.sql.ResultSet getQuery(String sql, int TYPE, int CONCUR) {
        java.sql.ResultSet rs = null;
        try {
            this.stmt = con.createStatement(TYPE, CONCUR);
            rs = stmt.executeQuery(sql);
        } catch (Exception e) {
            logger.error(e);
            logger.error(sql);
        }
        return rs;
    }

    public ArrayList<HashMap<String, String>> getQueryList(String sql) {
        return getQueryList(sql, null);
    }

    public ArrayList<HashMap<String, String>> getQueryList(String sql, HashMap<String, String> columnMap) {
        ArrayList<HashMap<String, String>> table = new ArrayList<>();

        try {
            this.stmt = con.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sql);
            String[] columnArray = getColumnNames(rs);
            // logger.info("columnArray="+columnArray.toString());

            while (rs.next()) {
                HashMap<String, String> row = new HashMap<String, String>();
                for (String column : columnArray) {
                    if (columnMap == null) {
                        row.put(column, rs.getString(column));
                    } else {
                        String column_new = (String) columnMap.get(column);
                        if (column_new == null)
                            continue;
                        row.put(column_new, rs.getString(column));
                        // logger.info(column+" → "+ column_new);
                    }
                }
                table.add(row);
            }
        } catch (Exception e) {
            logger.error(e);
            logger.error(sql);
        }
        return table;
    }

    /**
     * get data map of one row
     * @param sql query syntax
     * @return HashMap
     */
    public HashMap<String, String> getQueryMap(String sql) {
        HashMap<String, String> row = new HashMap<String, String>();

        try {
            this.stmt = con.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sql);
            String[] columnArray = getColumnNames(rs);
            logger.debug("columnArray=" + Arrays.toString(columnArray));

            if (rs.next()) {
                for (String column : columnArray) {
                    row.put(column, rs.getString(column));
                }
            }

        } catch (Exception e) {
            logger.error(e);
            logger.error(sql);
        }
        return row;
    }

    public String[] getColumnNames(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount(); // number of column
        String columnArray[] = new String[count];
        for (int i = 1; i <= count; i++) {
            columnArray[i - 1] = rsmd.getColumnLabel(i);
        }
        return columnArray;
    }

    /**
     * get single value
     * @param sql query syntax
     * @return String
     */
    public String getValue(String sql) {
        String rtnValue = "";
        try {
            java.sql.Statement stmt2 = con.createStatement();
            java.sql.ResultSet rs2 = stmt2.executeQuery(sql);
            if (rs2.next()) {
                rtnValue = rs2.getString(1);
            }
            stmt2.close();
        } catch (Exception e) {
            logger.error(e);
            logger.error(sql);
        }
        return rtnValue;
    }

    /**
     * for html obj(select.option): only value
     * @param sql query syntax
     * @return String "v1,v2,v3..."
     */
    public String getValueListStr(String sql) {
        StringBuilder rtnValue = new StringBuilder();
        try {
            java.sql.Statement stmt2 = con.createStatement();
            java.sql.ResultSet rs2 = stmt2.executeQuery(sql);
            while (rs2.next()) {
                rtnValue.append(rs2.getString(1) + ",");
            }
            stmt2.close();
        } catch (Exception e) {
            logger.error(e);
            logger.error(sql);
        }
        return rtnValue.toString().replaceAll(",$", "");
    }

    /**
     * for html obj(select.option): value and text
     * @param sql query syntax
     * @return String "v1:t1,v2:t2,v3:t3..."
     */
    public String getValueListPairStr(String sql) {
        StringBuilder rtnValue = new StringBuilder();
        try {
            java.sql.Statement stmt2 = con.createStatement();
            java.sql.ResultSet rs2 = stmt2.executeQuery(sql);
            while (rs2.next()) {
                rtnValue.append(rs2.getString(1) + ":" + rs2.getString(2) + ",");
            }
            stmt2.close();
        } catch (Exception e) {
            logger.error(e);
            logger.error(sql);
        }
        return rtnValue.toString().replaceAll(",$", "");
    }

    /**
     * for sql in(...) parameter
     * @param sql query syntax
     * @return String "'v1','v2','v3'..."
     */
    public String getValueListSqlStr(String sql) {
        StringBuilder rtnValue = new StringBuilder();
        try {
            java.sql.Statement stmt2 = con.createStatement();
            java.sql.ResultSet rs2 = stmt2.executeQuery(sql);
            while (rs2.next()) {
                rtnValue.append("'" + rs2.getString(1) + "',");
            }
            stmt2.close();
        } catch (Exception e) {
            logger.error(e);
            logger.error(sql);
        }
        return rtnValue.toString().replaceAll(",$", "");
    }

    public ArrayList<String> getValueList(String sql) {
        ArrayList<String> list = new ArrayList<>();
        java.sql.Statement stmt2 = null;
        java.sql.ResultSet rs2 = null;
        try {
            stmt2 = con.createStatement();
            rs2 = stmt2.executeQuery(sql);
            while (rs2.next()) {
                list.add(rs2.getString(1));
            }
            
            rs2.close();
            stmt2.close();
        } catch (Exception e) {
            logger.error(e);
            logger.error(sql);
        } 
        return list;
    }

    /**
     * for html obj(select.option): key value
     * @param sql query syntax
     * @return LinkedHashMap
     */
    public LinkedHashMap<String, String> getValueMap(String sql) {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        String key = "";
        String value = "";

        try {
            java.sql.Statement stmt2 = con.createStatement();
            java.sql.ResultSet rs2 = stmt2.executeQuery(sql);

            while (rs2.next()) {
                key = rs2.getString(1);
                value = rs2.getString(2);
                map.put(key, value);
            }
            stmt2.close();
        } catch (Exception e) {
            logger.error(e);
            logger.error(sql);
        }
        return map;
    }

    @SuppressWarnings("finally")
    public int exeUpdate(String sql) {
        int stat = 0;
        try {
            java.sql.Statement stmt2 = con.createStatement();
            stmt2.executeUpdate(sql);
            stmt2.close();
            stat = 1;
        } catch (Exception e) {
            logger.error(e.getMessage().replaceAll("\\r|\\n", ""));
            logger.error(sql);
        } finally {
            return stat;
        }
    }

    @SuppressWarnings("finally")
    public int exeSQL(String sql) {
        int stat = 0;
        try {
            java.sql.Statement stmt2 = con.createStatement();
            stmt2.execute(sql);
            stmt2.close();
            stat = 1;
        } catch (Exception e) {
            logger.error(e);
            logger.error(sql);
        } finally {
            return stat;
        }
    }

    public void exeSQL(ArrayList<String> sqls) throws SQLException {
        java.sql.Statement stmt2 = con.createStatement();
        for (String sql : sqls) {
            stmt2.addBatch(sql);
        }
        stmt2.executeBatch();
        stmt2.close();
    }

    public HashMap<String, String> getTypeMap(String tbName) {
        HashMap<String, String> map = new HashMap<>();
        try {
            String sql = "select * from " + tbName + " where 1<0";
            ResultSet rs = getQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCnt = rsmd.getColumnCount();
            for (int i = 1; i <= columnCnt; i++) {
                String name = rsmd.getColumnName(i);
                String type = rsmd.getColumnTypeName(i);
                // System.out.println("name: "+name +"，type: "+type);
                map.put(name, type);
            }
            rs.close();
            this.stmt.close();
        } catch (SQLException e) {
            logger.error(e);
        }
        return map;
    }

    public ArrayList<String> getPKList(String tbName) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            DatabaseMetaData dbmd = con.getMetaData();
            ResultSet rs = dbmd.getPrimaryKeys(null, null, tbName);
            while (rs.next()) {
                list.add(rs.getString("COLUMN_NAME"));
            }

        } catch (SQLException e) {
            logger.error(e);
        }
        return list;
    }

    public HashMap<String, Integer> getColumnLengthMap(String tbName) {
        HashMap<String, Integer> map = new HashMap<>();
        try {
            String sql = "select * from " + tbName + " where 1 < 0";
            ResultSet rs = getQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCnt = rsmd.getColumnCount();
            for (int i = 1; i <= columnCnt; i++) {
                String name = rsmd.getColumnName(i);
                int length = rsmd.getColumnDisplaySize(i);
                // System.out.println("name: "+name +"，type: "+type);
                map.put(name, length);
            }
            rs.close();
            this.stmt.close();
        } catch (SQLException e) {
            logger.error(e);
        }
        return map;
    }
    
    public int batchInsert(String tbName, ArrayList<HashMap<String, String>> tbData) {
        return batchInsert(tbName, tbData, null);
    }

    @SuppressWarnings("finally")
    public int batchInsert(String tbName, ArrayList<HashMap<String, String>> tbData, String keyColumn) {
        int stat = 0;
        try {
            if (tbData.isEmpty()) {
                logger.info("batchInsert [" + tbName + "] dataSrcCnt=0");
                return 0;
            }

            // generate insert sql
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            HashMap<String, String> row0 = tbData.get(0);
            for (Object key : row0.keySet()) {
                columns.append(key).append(",");
                values.append("?").append(",");
            }
            columns.deleteCharAt(columns.length() - 1);
            values.deleteCharAt(values.length() - 1);
            String insertSQL = "INSERT INTO " + tbName + "(" + columns + ") VALUES(" + values + ")";
            // logger.debug("insertSQL="+insertSQL);

            // 檢查鍵值是否存在，會影響效能，關掉
            ArrayList<String> keyList = null;
//            if (keyColumn != null) {
//                keyList = getValueList("select " + keyColumn + " from " + tbName);
//            }
            
            HashMap<String, String> typeMap = getTypeMap(tbName);

            con.setAutoCommit(false); // 批次處理，要關閉自動提交
            PreparedStatement pStmt = con.prepareStatement(insertSQL);
            int insertCnt = 0;
            
            // 全部紀錄 loop
            outter: for (HashMap<String, String> row : tbData) {
                // check record exist or not
                if (keyColumn != null) {
                    // String chkExistSql = "select count(1) from "+tbName+" where
                    // "+keyColumn+"='"+row.get(keyColumn)+"'";
                    // int cnt = Cmn.toInt(getValue(chkExistSql));
                    // if(cnt > 0) {
                    if (keyList.contains(row.get(keyColumn))) {
                        logger.info("skip insert ，record " + keyColumn + "=" + row.get(keyColumn) + " is exist.");
                        continue;
                    }
                }

                // 組合欄位
                int columnIdx = 1;
                for (Object key : row.keySet()) {
                    String type = (String) typeMap.get(key);
                    String value = row.get(key);
                    // logger.debug(columnIdx+"_type="+type+" , key="+key+" , value="+value);
                    if (value != null && value.contains("??")) {
                        // value = ""+new Date().getTime(); //
                        // logger.debug("time value="+value);
                        continue outter;
                    }

                    // date / int / str
                    // if(Cmn.isDate(value)) {
                    if ("timestamp".equals(type)) {
                        pStmt.setTimestamp(columnIdx, Timestamp.valueOf(value));
                    }
                    // else if(Cmn.isNumber(value)) {
                    else if (type != null && type.contains("int")) {
                        pStmt.setInt(columnIdx, Cmn.toInt(value));
                    } else {
                        pStmt.setString(columnIdx, value);
                    }
                    columnIdx++;
                }
                pStmt.addBatch();
                
                insertCnt++;
            } // 全部紀錄 loop
            
            stat = 1;
            logger.info("[" + tbName + "] insertCnt=" + insertCnt + "，stat=" + stat);

            pStmt.executeBatch(); // 批次更新
            con.commit(); // 提交
            pStmt.close();
            
        } catch (Exception e) {
            logger.error(e);
            logger.error("[" + tbName + "] fail tbData= " + tbData);

        } finally {
            return stat;
        }
    }

    public void closeStmt() throws Exception {
        if (stmt != null) {
            stmt.close();
        }
    }

    public void closeCon() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

}
