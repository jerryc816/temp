package don.api.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import don.api.Cmn;
import don.api.util.MapUtil;


public class FileUtil {

    public static void main(String[] args) {
        //moveTo("E:/dirA/test1.txt", "E:/dirB/");
        //ArrayList<String> fileList = getFileList("C:\\eclipse_work\\don_Lib");
        //for(String file : fileList) System.out.println(file);
        //replaceFileName("C:\\test\\batch\\", "old", "new");
        
        //Map<String, Integer> map = countWord("C:\\eclipse_work\\don_Lib\\src\\don\\api\\ChartDir.java");
        //countWordOfProject("C:\\eclipse_work\\don_Lib");
        //countWordOfProject("C:\\eclipse_work\\TestSpringMvc(Bootstrap)");
        //appendStrToFile("C:\\test\\testAppend.txt", "666\r\n測試");
    }

    public static void appendStrToFile(String fileName, String appendStr) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true)); 
            out.write(appendStr); 
            out.close();  
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // TODO 少數檔案轉檔後變亂碼(13/540)，待修正
    // 將資料夾中的txt檔案轉存為UTF-8格式
    public static void saveFileToUTF8Batch(String dir) {
        ArrayList<String> list = getFileList(dir);
        for(String fileName : list) {
            if(fileName.endsWith(".txt")) {
                System.out.println(fileName);
                saveFileToUTF8(fileName);
            }
        }
    }
    
    // 將單一檔案轉存為UTF-8格式
    public static void saveFileToUTF8(String fileName) {
        try {
            String sourceCharset = getCharset(fileName);
            String targetCharset = "UTF-8";
            
            Path path = Paths.get(fileName);
            ByteBuffer byteBuffer = ByteBuffer.wrap(Files.readAllBytes(path));
            CharBuffer charBuffer = Charset.forName(sourceCharset).decode(byteBuffer);
            byteBuffer = Charset.forName(targetCharset).encode(charBuffer);
            Files.write(path, byteBuffer.array());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 查出檔案的編碼，ANSI:Big5，UTF-16 LE:UTF-16
    public static String getCharset(String fileName) {
        String charSetArray[] = {"Big5", "UTF-16", "UTF-8"};
        String currentCharSet = null;
        for (String charSet : charSetArray) {
            if(isCharset(fileName, charSet)) {
                currentCharSet = charSet;
                break;
            }
        }
        return currentCharSet;
    }
    // 查出檔案是否為傳入的編碼
    public static boolean isCharset(String fileName, String charSet) {
        boolean isCharset = true;
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(Files.readAllBytes(Paths.get(fileName)));
            Charset.availableCharsets().get(charSet).newDecoder().decode(ByteBuffer.wrap(byteBuffer.array()));
        } catch (CharacterCodingException e) {
            isCharset = false;
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return isCharset;    
    }
    

    // 找出專案內，所有java檔的英文單字出現次數
    public static void countWordOfProject(String projectPath) {
        ArrayList<String> fileList = getFileList(projectPath);
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (String file : fileList) {
            if(!file.endsWith(".java")) continue;
            map = countWord(file, map);
        }
        map = MapUtil.sortByValue(map, "desc");
        System.out.println(map);
        //for (Object key : map.keySet()) {
        //    System.out.println(key +": "+ map.get(key));
        //}        
    }
    
    // 找出檔案內，所有英文單字出現次數
    public static Map<String, Integer> countWord(String path) {
        return countWord(path, new HashMap<String, Integer>());
    }
    public static Map<String, Integer> countWord(String path, Map<String, Integer> map) {
        try {
            FileReader fr = new FileReader(path);
            BufferedReader bf = new BufferedReader(fr);
            String line = null;
            while((line = bf.readLine()) != null) {
                String regex = "[^a-zA-Z]+";
                String words[] = line.split(regex);
                for (String word : words) {
                    if(word.equals("")) continue;
                    Integer cnt = map.get(word);
                    if(cnt == null) {
                        map.put(word, 1);
                    } else {
                        map.put(word, ++cnt);
                    }
                }
            } 
            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    
    // 取指定目錄下的所有檔案和目錄
    public static ArrayList<String> getFileList(String directory) {
        ArrayList<String> fileList = new ArrayList<String>();
        File f = new File(directory);
        File[] files = f.listFiles();
        for (File file : files) {
            fileList.add(file.getAbsolutePath());
            if (file.isFile()) {
                //System.out.println("檔案：" + file);
            } else {
                //System.out.println("目錄：" + file);
                ArrayList<String> subList = getFileList(file.getAbsolutePath());
                fileList.addAll(subList);
            }
        }
        return fileList;
    }
    
    // 傳入檔案路徑，生成路徑中不存在的資料夾
    public static void genDir(String filePath) {
        String dirLayer[] = filePath.split("/");
        StringBuilder dirPath = new StringBuilder();
        for (String dirName : dirLayer) {
            dirPath.append(dirName).append("/");

            File dir = new File(dirPath.toString());
            if (!dir.exists()) {
                dir.mkdir();
            }
        }
    }

    // 取副檔名
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int lastPoint = fileName.lastIndexOf(".");
        return (lastPoint == -1) ? "" : fileName.substring(lastPoint);
    }

    // 取路徑(不含檔名)
    public static String getFilePath(File file) {
        String absolutePath = file.getAbsolutePath();
        return absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
    }
    
    // 批次更改檔名 (不指定新字串,用空字串取代)
    public static void replaceFileName(String dirPath, String fileSubstrOld) {
        replaceFileName(dirPath, fileSubstrOld, "");
    }

    // 批次更改檔名 (指定新字串,取代)
    public static void replaceFileName(String dirPath, String fileSubstrOld, String fileSubstrNew) {        
        ArrayList<String> fileList = getFileList(dirPath);
        for (String fileName : fileList) {
            File oldFile = new File(fileName);
            if(oldFile.isDirectory()) continue; // 資料夾不更名
                
            String oldName = oldFile.getName();
            String newName = oldName.replaceAll(fileSubstrOld, fileSubstrNew);
            Cmn._p("rename: " + oldName + " -> " + newName);

            File newFile = new File(getFilePath(oldFile) + File.separator + newName);
            if (oldFile.renameTo(newFile)) {
                Cmn._p("ok");
            } else {
                Cmn._p("error");
            }
        } // for end
        Cmn._p("replaceFileName end~");
    }

    // 將source目錄下的文件(含子目錄)，搬移到target目錄下
    public static ArrayList<String> moveFilesTo(String sourceDirPath, String targetDirPath) {
        ArrayList<String> targetPathList = new ArrayList<>();
        try {
            File dir = new File(sourceDirPath);
            File files[] = dir.listFiles();
            for (File file : files) {
                String targetPath = moveTo(file.getAbsolutePath(), targetDirPath);
                targetPathList.add(targetPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetPathList;
    }

    // 將source文件(單一檔)，搬移到target目錄下
    public static String moveTo(String sourceFilePath, String targetDirPath) {
        Path targetPath = null;
        try {
            Path sourcePath = Paths.get(sourceFilePath);
            targetPath = Paths.get(targetDirPath).resolve(sourcePath.getFileName());
            Files.move(sourcePath, targetPath);
            Cmn._p("File moved! srFile: " + sourcePath + " -> dtFile: " + targetPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (targetPath == null) ? "" : targetPath.toString();
    }

    // 將source文件(單一檔)，複製到target目錄下
    public static void copyTo(String sourceFilePath, String targetFilePath) {
        try {
            File f1 = new File(sourceFilePath);
            File f2 = new File(targetFilePath);
            InputStream in = new FileInputStream(f1);

            OutputStream out = new FileOutputStream(f2); // For Overwrite the file.
            // OutputStream out = new FileOutputStream(f2,true); // For Append the file.

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            Cmn._p("File copied! srFile: " + sourceFilePath + " -> dtFile: " + targetFilePath);
        } catch (FileNotFoundException ex) {
            Cmn._p(ex.getMessage() + " in the specified directory.");
            System.exit(0);
        } catch (IOException e) {
            Cmn._p(e.getMessage());
        }
    }

}
