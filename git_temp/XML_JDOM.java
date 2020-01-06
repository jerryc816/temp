package don.api.office;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import don.api.CustomException;

public class XML_JDOM {


    // 解析XML，返回第一級元素鍵值對。如果第一級元素有子節點，則此節點的值是子節點的XML數據。
    public static LinkedHashMap<String, String> doXMLParse(String strxml) throws JDOMException, IOException {
        strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");
        if (null == strxml || "".equals(strxml)) {
            return null;
        }

        LinkedHashMap<String, String> m = new LinkedHashMap<>();

        InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(in);
        Element root = doc.getRootElement();
        List list = root.getChildren();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Element e = (Element) it.next();
            String k = e.getName();
            String v = "";
            List children = e.getChildren();
            if (children.isEmpty()) {
                v = e.getTextNormalize();
            } else {
                v = XML_JDOM.getChildrenText(children);
            }
            m.put(k, v);
        }
        in.close();
        return m;
    }

    // 取子節點的 xml string
    public static String getChildrenText(List children) {
        StringBuffer sb = new StringBuffer();
        if (!children.isEmpty()) {
            Iterator it = children.iterator();
            while (it.hasNext()) {
                Element e = (Element) it.next();
                String name = e.getName();
                String value = e.getTextNormalize();
                List list = e.getChildren();
                sb.append("<" + name + ">");
                if (!list.isEmpty()) {
                    sb.append(XML_JDOM.getChildrenText(list));
                }
                sb.append(value);
                sb.append("</" + name + ">");
            }
        }

        return sb.toString();
    }

    // 將請求參數Map轉換為xml格式的string
    public static String toXmlStr(SortedMap<Object, Object> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k) || "sign".equalsIgnoreCase(k)) {
                sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
            } else {
                sb.append("<" + k + ">" + v + "</" + k + ">");
            }
        }
        sb.append("</xml>");
        return sb.toString();
    }

    // 讀取 NodeList 所有節點，轉換為 HashMap<節點名稱,節點值>
    public static HashMap<String, String> getNodesMap(NodeList nodes) {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            String nodeValue = (node.hasChildNodes()) ? node.getFirstChild().getNodeValue() : "";
            map.put(nodeName, nodeValue);
        }
        return map;
    }

    // 讀取包含soap header的xmlStr()，取其soap body，在由tag名稱取底下的節點群
    public static NodeList getNodesByTagName(String xmlStr, String tagName) throws IOException, SOAPException {
        InputStream is = new ByteArrayInputStream(xmlStr.getBytes());
        SOAPMessage smessage = MessageFactory.newInstance().createMessage(null, is);
        SOAPBody soapBody = smessage.getSOAPBody();
        return soapBody.getElementsByTagName(tagName).item(0).getChildNodes();        
    }

}