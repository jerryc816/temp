package don.api.net;

import java.io.*;
import java.net.*;
import java.util.Vector;

public class TelnetClient extends Thread {

    Socket socket;
    BufferedInputStream in;
    PrintWriter out;

    public Vector barcode_pair = new Vector(); // 0=color,1=material
    public boolean read_flag = false;

    public static void _p(String s) {
        System.out.println(s);
    }

    public boolean connect(String host) {
        return connect(host, 23);
    }

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            socket.setKeepAlive(true);
            in = new BufferedInputStream(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println(host + "：" + port + " telnet connect ok !");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(host + "：" + port + " telnet connect fail !");
            return false;
        }
    }

    // after connect to telnet, login
    public boolean login(String usr, String pwd) {
        try {
            String res = readUntil("Enter User Name");
            if (res != null) {
                res = readUntil(">");
                if (res != null) {
                    send(usr);
                    Thread.sleep(500);
                    res = readUntil("Enter Password");
                    if (res != null) {
                        send(pwd, false);
                        Thread.sleep(500);
                        res = readUntil(">");
                        if (res != null) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // send command to telnet server
    public void send(String command) {
        send(command, true);
    }

    public void send(String command, boolean echo) {
        try {
            out.println(command + "\n");
            out.flush();
            if (echo) {
                // System.out.println(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        read_flag = true;
        while (read_flag) {
            // barcode_pair.add(readUntil("\n"));
            barcode_pair.add(0, readUntil("\n"));
        }
    }

    // read data from telnet server
    public String readUntil(String pattern) {
        // _p("wait to scan...");
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuffer sb = new StringBuffer();

            if (in == null || socket == null || socket.isClosed())
                return null;

            char ch = (char) in.read(); // <=java.net.SocketException: Socket closed
            while (true) {
                sb.append(ch);
                if (ch == lastChar && sb.toString().endsWith(pattern)) {
                    return sb.toString().replace("\n", "").trim();
                }
                ch = (char) in.read();
            }
        } catch (java.net.SocketException e) {
            read_flag = false;
        } catch (Exception e) {
            read_flag = false;
            e.printStackTrace();
        }
        return null;
    }

    public void disconnect() {
        try {
            //read_flag = false;
            //System.out.println("read thread destroy");
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
            System.out.println("disconnect telnet");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TelnetClient tClient = new TelnetClient();
        tClient.connect("192.168.1.2");
        //System.out.println(tClient.readUntil("\n") + " bc1");
        //System.out.println(tClient.readUntil("\n") + " bc2");
        //tClient.disconnect();
    }
}
