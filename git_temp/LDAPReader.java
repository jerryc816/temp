// TODO 待測,讀取Ldap使用者資訊
public class LDAPReader {

    public static void main(String[] args) {
        readLdap();
    }
    
    public static void readLdap() {
        Hashtable<String, String> env = new Hashtable<String, String>();
        DirContext ctx = null;
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://" + "192.168.1.205" + ":" + "389");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "CN=" + "admin" + "," + "dc=sys,dc=com");
        env.put(Context.SECURITY_CREDENTIALS, "123456");
        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException e) {
            e.printStackTrace();
            return;
        }
        
        Map<String,String> map = new HashMap<String, String>();
        try {
            if(ctx != null){
                NamingEnumeration<NameClassPair> list = ctx.list("dc=sys,dc=com");
                while(list.hasMore()){
                    NameClassPair ncp = list.next();
                    String cn = ncp.getName();
                    if(cn.indexOf("=") != -1){
                        int index = cn.indexOf("=");
                        cn = cn.substring(index + 1,cn.length());
                        map.put(cn, ncp.getNameInNamespace());
                    }
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
            return;
        }
        
        try {
            if(ctx != null)
                ctx.close();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        
        Iterator<Entry<String,String>> it = map.entrySet().iterator();
        while(it.hasNext()){
            Entry<String,String> entry = it.next();
            System.out.println("Key:"+entry.getKey());
            System.out.println("Value:"+entry.getValue());
        }
    }
}