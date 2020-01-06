package don.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class TestPath {
    
    public static void main(String[] args) throws IOException {
        String os = System.getProperty("os.name");
        String root = System.getProperty("user.dir");
        String sp = File.separator;
        System.out.println("os = "+ os);
        System.out.println("root = "+ root);
        System.out.println("sp = "+ sp);
        System.out.println("==================================");
        
        System.out.println("--> 測試 class.getResourceAsStream");
        InputStream is;
        // 限制: 只能取src底下的資源
        // path   以'/'開頭時​​，從src往下獲取；
        // path 不以'/'開頭時​​，從類所在的packege往下取資源；        
        is = TestPath.class.getResourceAsStream("/prop/db2.properties");                // 取/don_Lib/src/prop/db2.properties           
        showPropties(is);
        is = TestPath.class.getResourceAsStream("prop/db2.properties");                 // 取/don_Lib/src/don/test/prop/db2.properties         
        showPropties(is);
        
        System.out.println("--> 測試 ClassLoader.getResourceAsStream");
        // 限制: path 不能以'/'開頭
        // path 有含packege時，從src往下取資源；
        // path 不含packege時，從project root往下取資源；
        is = TestPath.class.getClassLoader().getResourceAsStream("prop/db2.properties");// 取/don_Lib/src/prop/db2.properties
        showPropties(is);
        is = TestPath.class.getClassLoader().getResourceAsStream("db2.properties");     // 取/don_Lib/prop/db2.properties
        showPropties(is);
        
        System.out.println("--> 測試 File.separator");
        // 限制: windows路徑是雙反斜線、linux路徑是單斜線
        // 使用 File.separator 來組成路徑，則可解決此問題
        is = new FileInputStream("prop"+ sp +"db2.properties");                         // 取/don_Lib/prop/db2.properties
        showPropties(is);
        
        System.out.println("--> 測試 ClassLoader.getResource");
        System.out.println("path = "+TestPath.class.getClassLoader().getResource("db2.properties"));
        System.out.println("path = "+TestPath.class.getClassLoader().getResource("db2.properties").toString());
        System.out.println("path = "+TestPath.class.getClassLoader().getResource("db2.properties").getPath());
    }
    
    public static void showPropties(InputStream is) throws IOException {
        Properties prop = new Properties();
        prop.load(is);
        String driver = prop.getProperty("jdbc.driver");
        System.out.println("driver = "+ driver);        
    }
}
