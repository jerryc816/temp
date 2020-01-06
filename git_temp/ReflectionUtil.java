package don.api.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import don.api.Cmn;
import don.test.interfacee.GooglePay;
import don.test.interfacee.IPay;

public class ReflectionUtil {
    public String userName = "jerry";
    private int userAge = 23;
    public String[] strArray = { "Hello", "World", "jerry" };
    public double money = 580.5;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserAge() {
        return userAge;
    }

    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    // 測試
    public static void main(String[] args) {
        try {
            // Class c = Class.forName("don.test.TestReflection");
            // TestReflection myObj = (TestReflection) c.newInstance();
            ReflectionUtil myObj = new ReflectionUtil();

            // check all field (name and value) of some Object
            System.out.println(">>checkValues()");
            checkValues(myObj);

            System.out.println("\n>>getValueMap()");
            HashMap<String, Object> rtnMap = getValueMap(myObj);
            Cmn.showMap(rtnMap);

            // call method by fieldName (int)
            String fieldName = "userAge";
            Method method = myObj.getClass().getMethod("set" + Cmn.toUpperFirst(fieldName), Integer.TYPE);
            method.invoke(myObj, new Integer(38));
            System.out.println("\nafter setter age = " + myObj.getUserAge());

            // call method by fieldName (String)
            fieldName = "userName";
            Class[] cArg = { String.class };
            method = myObj.getClass().getMethod("set" + Cmn.toUpperFirst(fieldName), cArg);
            method.invoke(myObj, "test");
            System.out.println("after setter name = " + myObj.getUserName());

            // test stackTrace
            System.out.println("\n>>test stackTrace");
            myMethodA();

            // test inject
            System.out.println("\n>>test inject");
            HashMap<String, String> dataMap = new HashMap<String, String>() {
                {
                    put("userName", "abc");
                    put("userAge", "123");
                    put("money", "99.6");
                }
            };
            inject(myObj, dataMap);
            
            System.out.println("\n>>checkValues()");
            checkValues(myObj);

            System.out.println("\n>>testClassForName()");
            testClassForName();

        } catch (Exception e) {
            e.printStackTrace();
        }
    } // main

    public static void myMethodA() {
        myMethodB();
    }

    public static void myMethodB() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            System.out.println(element.toString());
        }
    }

    // 動態檢查{實體物件}的所有變數{名稱、值}
    public static void checkValues(Object obj) {
        String className = obj.getClass().getName();
        System.out.println("******↓↓ " + className + "：checkValues ↓↓******");
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                String type = f.getGenericType().toString();
                boolean isArray = type.contains("class [");
                if (isArray && f.get(obj) != null) {
                    String[] arr = (String[]) f.get(obj);
                    System.out.println(f.getName() + "：" + Arrays.toString(arr));
                } else {
                    System.out.println(f.getName() + "：" + f.get(obj));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("******↑↑ " + className + "：checkValues ↑↑******");
    }

    // 取 obj(pojo) 的值,放到 HashMap 中
    public static HashMap<String, Object> getValueMap(Object obj) {
        HashMap<String, Object> map = new HashMap<>();
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                String type = f.getGenericType().toString();
                boolean isArray = type.contains("class [");
                if (isArray && f.get(obj) != null) {
                    String[] arr = (String[]) f.get(obj);
                    map.put(f.getName(), arr);
                    //map.put(f.getName(), Arrays.toString(arr));
                } else {
                    map.put(f.getName(), String.valueOf(f.get(obj)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    // 將 dataMap 注入到 obj(pojo) 中
    public static void inject(Object obj, HashMap dataMap) {
        String className = obj.getClass().getName();
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                String fieldName = f.getName();
                Object fieldValue = dataMap.get(fieldName);
                if (fieldValue == null) {
                    fieldValue = dataMap.get(fieldName.toUpperCase());
                }
                if (fieldValue == null) {
                    fieldValue = dataMap.get(fieldName.toLowerCase());
                }
                if (fieldValue == null) {
                    // map 中找不到此 member 的 k/v，跳下一個
                    //System.out.println(fieldName + " skip");
                    continue;
                }

                Method method = null;
                String typeStr = f.getGenericType().toString();
                System.out.println("inj# " + fieldName + " " + fieldValue + " ("+ typeStr +")");
                switch (typeStr) {
                    case "int":
                    case "class java.lang.Integer":
                        method = obj.getClass().getMethod("set" + Cmn.toUpperFirst(fieldName), Integer.TYPE);
                        method.invoke(obj, new Integer((String)fieldValue));
                        break;
                    case "double":
                    case "class java.lang.Double":
                        method = obj.getClass().getMethod("set" + Cmn.toUpperFirst(fieldName), Double.TYPE);
                        method.invoke(obj, new Double((String)fieldValue));
                        break;
                    case "class java.lang.String":
                        Class[] cArg = { String.class };
                        method = obj.getClass().getMethod("set" + Cmn.toUpperFirst(fieldName), cArg);
                        method.invoke(obj, fieldValue);
                        break;
                    // 其他型態、array...
                    default:
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 用Class.forName取class -> 在取實體 -> 動態呼叫他的方法
    public static void testClassForName() {
        try {
            Class<?> c = Class.forName("don.test.interfacee.GooglePay");
            Constructor<?> constructor = c.getConstructor(double.class);
            
            GooglePay gpay = (GooglePay) constructor.newInstance(0.5);
            System.out.println("反射呼叫1: " + gpay.pay());

            IPay payment = (IPay) constructor.newInstance(0.5);
            System.out.println("反射呼叫2: " + payment.pay());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 待測
    // apache commons-lang3:
    // FieldUtils.readField(object, fieldName, true);
}
