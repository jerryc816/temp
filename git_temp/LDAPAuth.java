// TODO 待測,驗證Ldap使用者
public class LDAPAuth {    
    private final String URL = "ldap://192.168.1.205:389/";
    private final String BASEDN = "cn=demo1,dc=sys,dc=com";  // 根據情況進行修改
    private final String FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private LdapContext ctx = null;
    private final Control[] connCtls = null;
  
    private void LDAP_connect() {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, FACTORY);
        env.put(Context.PROVIDER_URL, URL + BASEDN);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        String root = "cn=demo1,dc=sys,dc=com";  // 根根據情況修改
        env.put(Context.SECURITY_PRINCIPAL, root);  // 管理員
        env.put(Context.SECURITY_CREDENTIALS, "123456");  // 管理員密碼
         
        try {
            ctx = new InitialLdapContext(env, connCtls);
            System.out.println( "認證成功" ); 
            System.out.println(ctx);
             
        } catch (javax.naming.AuthenticationException e) {
            System.out.println("認證失敗：");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("認證出錯：");
            e.printStackTrace();
        }
         
        if (ctx != null) {
            try {
                ctx.close();
            }
            catch (NamingException e) {
                e.printStackTrace();
            }
 
        }
    }
}