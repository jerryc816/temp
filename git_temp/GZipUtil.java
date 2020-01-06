package don.test.compress;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream; // 解壓用
import java.util.zip.GZIPOutputStream; // 壓縮用

// TODO: compress directory
// 1 http://www.java2s.com/Code/Java/File-Input-Output/gzippingfilesandzippingdirectories.htm
// 2 https://knpcode.com/java-programs/gzip-multiple-files-java-tar-archive/
public class GZipUtil {

    public static void main(String[] args) {
        String input_file = "C:/test/gzipInput.txt";
        String gzip_file = "C:/test/gzipFile.gz";
        String output_file = "C:/test/gzipOutput.txt";

        GZipUtil.gzipFile(input_file, gzip_file);
        GZipUtil.gunzipFile(gzip_file, output_file);
    }

    public static void gzipFile(String input_file, String output_gzip_file) {
        try {
            GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(output_gzip_file));
            FileInputStream fis = new FileInputStream(input_file);

            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }

            fis.close();
            gzos.finish();
            gzos.close();
            System.out.println("gzip done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gunzipFile(String input_gzip_file, String output_file) {
        try {
            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(input_gzip_file));
            FileOutputStream fos = new FileOutputStream(output_file);

            int len;
            byte[] buffer = new byte[1024];
            while ((len = gzis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            gzis.close();
            fos.close();
            System.out.println("gunzip done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
