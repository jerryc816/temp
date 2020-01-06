package don.api.office;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

import org.dom4j.*;
import org.dom4j.io.*;

// XMLDemoDOM4J 
public class XML_DOM4J {

    public static void main(String[] args) {
        try {
            Document doc;
            doc = readXML("C:\\test\\myfile.xml");
            //doc = createXML(); //自組xml
            printDocument(doc);
            String xmlStr = documentToXmlStr(doc);
            xmlStr2Document(xmlStr);
            showAllNodes(doc);
            showNodeByPath(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // main

    // 從檔案讀取XML
    // 輸入檔名，返回XML文件
    public static Document readXML(String fileName) throws MalformedURLException, DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(fileName)); // 可用 InputStream, File, Url 等不同的引數來讀取
        return document;
    }
    
    public static Document createXML() throws Exception {
        System.out.println("====== createXML ======");
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("root");

        Element car1 = root.addElement("car");
        car1.addAttribute("name", "Ferrai");
        car1.addElement("type").addText("sport");
        car1.addElement("cost").addText("3,000,000");

        Element car2 = root.addElement("car");
        car2.addAttribute("name", "Bmw");
        car2.addElement("type").addText("sedan");
        car2.addElement("cost").addText("1,850,000");

        System.out.println("");
        return doc;
    }

    public static void printDocument(Document doc) throws Exception {
        System.out.println("====== printDocument ======");
        // 印出xml(格式化)
        OutputFormat format = OutputFormat.createPrettyPrint();
        // 解決多一個空白行
        format.setEncoding("UTF-8");
        format.setNewLineAfterDeclaration(false);
        // 輸出到console
        XMLWriter writer;
        writer = new XMLWriter(System.out, format);
        writer.write(doc);
        System.out.println("");
    }

    public static String documentToXmlStr(Document doc) throws Exception {
        System.out.println("====== documentToXmlStr ======");
        // 轉字串後印出
        String xmlStr = doc.asXML();
        System.out.println(xmlStr);
        System.out.println("");
        return xmlStr;
    }

    public static void xmlStr2Document(String xmlStr) throws Exception {
        System.out.println("====== xmlStr2Document ======");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(new StringReader(xmlStr));
        documentToXmlStr(doc);
        System.out.println("");
    }

    public static void showAllNodes(Document doc) throws Exception {
        System.out.println("====== showAllNodes ======");
        // 取根節點
        Element root = doc.getRootElement();
        // 遍歷根節點底下的所有car節點
        for (Iterator i = root.elementIterator("car"); i.hasNext();) {
            Element e = (Element) i.next();
            // 取car的name屬性
            System.out.println(e.attributeValue("name"));
            // 取car底下的元素值
            System.out.println("  type: " + e.elementText("type"));
            System.out.println("  cost: " + e.elementText("cost"));
        }
        System.out.println("");
    }


    // 路徑設錯時(ex.少了/root) 會拋例外 ClassCastException cannot be cast to org.dom4j.Element
    @SuppressWarnings("unchecked")
    public static void showNodeByPath(Document doc) throws Exception {
        System.out.println("====== showNodeByPath ======");

        // 依path取節點(方式1) 取element
        List<Element> list = (List<Element>) doc.selectObject("/root/car"); // 當多節點時,返回的會是List
        System.out.println("list: " + list.get(0).getStringValue());
        Element elment = (Element) doc.selectObject("/root/bicycle/brand"); // 單節點時,可直接轉element
        System.out.println("brand: " + elment.getStringValue());
        elment = (Element) doc.selectObject("/root/bicycle/cost"); 
        System.out.println("cost: " + elment.getStringValue());
        System.out.println("--------------------");
        
        // 依path取節點(方式2) 取nodes
        String path = "/root/car[@name = 'Bmw']";
        System.out.println("path: "+ path);        
        List<Node> nodes = doc.selectNodes(path); 
        for (Node node : nodes) {
            // 設定值
            node.selectSingleNode("cost").setText("567");
            // 讀取值
            System.out.println("element is: " + node.getName());
            System.out.println("name: " + node.valueOf("@name"));
            System.out.println("type: " + node.selectSingleNode("type").getText());
            System.out.println("cost: " + node.selectSingleNode("cost").getText());
        }        
        System.out.println("");
    }
}