

# LDAP Authentication in Java #

Usually, LDAP authentication is required for enterprise applications. Of course, you can use some LDAP Java library to achieve this. But it is very easy to implement in simple Java code. Here, I like to illustrate how to implement this using Java and Spring with Ldaps support.

# Authentication Mechanism #

Ldap authentication usually first uses a manager account to bind to the Ldap server and this manager account should have the privilege to search users in the Ldap server. Once the user is found, use the provided password to bind to that user. The the bind is successful, the user is authenticated, otherwise, the authentication is failed.

## Context Source ##

First, we need a ContextSource class to hold all the following information,

```
public class ContextSource {

	private String contextFactory;
	
	private String providerUrl;
	
	private String authenticationType;
	
	private String managerDN;
	
	private String managerPassword;
	
	private String base;

        private String securityProtocol;
}
```

Here the setters and getters are skipped here.

## Ldap Search ##

To search Users from the Ldap server, we need a set of interfaces and classes.

### Ldap Result Set ###

```
public class LdapResultSet {
	Map<String, Object> attributeMap;
	
	public final Map<String, Object> getAttributeMap() {
		return attributeMap;
	}

	public final void setAttributeMap(Map<String, Object> attributeMap) {
		this.attributeMap = attributeMap;
	}

	public Object getAttribute(String attribute){
		
		if(attributeMap == null){
			
			return null;
		}else{
			
			return attributeMap.get(attribute);
		}
	}
}
```

### Ldap Result Set Builder ###

```
public interface LdapResultSetBuilder {
	
  LdapResultSet buildLdapResultSet(SearchResult result, String[] attributesToReturn) throws NamingException;
}

```

### Ldap Result Set Builder Implementation ###

```
public class LdapResultSetBuilderImpl extends I18NSupportImpl implements LdapResultSetBuilder 
{
	public LdapResultSet buildLdapResultSet(SearchResult result, String[] attributesToReturn) throws NamingException 
	{
		
		if(result == null)
			return null;
		
		LdapResultSet resultSet = new LdapResultSet();
		Attributes attributes = result.getAttributes();
		Map<String, Object> attributesMap = new HashMap<String, Object>();
		
		if(attributesToReturn == null){
			
			resultSet.setAttributeMap(null);
		}else{
			
			for(String attribute : attributesToReturn){
				Attribute attr = attributes.get(attribute);
				if(attr != null)
					attributesMap.put(attribute,  attr.get());
				else
					attributesMap.put(attribute,  null);
			}
			
			resultSet.setAttributeMap(attributesMap);
		}
		
		return resultSet;
	}

}

```

## Ldap Resource ##

The interface is as follows,

```
public interface LdapResource {
	public DirContext bind(Hashtable env) throws NamingException;
	
	public DirContext bind(String userDN, String userPassword) throws NamingException;
	
	public List<LdapResultSet> search(String filter, String[] attributesToReturn) throws NamingException;
	
	public List<LdapResultSet> search(String base, String filter, String[] attributesToReturn) throws NamingException;
	
	public void unbind(DirContext ctx);
}
```

and the implementation is as follows,

```
public class LdapResourceImpl extends BaseResourceImpl implements LdapResource {

    private static final String CONNECTION_POOL_KEY = "com.sun.jndi.ldap.connect.pool";

    private static final String ERROR_INVALID_USERNAME_PASSWORD = "error.invalid.username.password";

    private static Log log = LogFactory.getLog(LdapResourceImpl.class);

    private boolean useConnectionPool;

    private ContextSource contextSource;

    private LdapResultSetBuilder ldapResultSetBuilder;

    private String trustStore;

    private String trustStorePassword;

    private String trustStoreType;

    @Required
    public final void setLdapResultSetBuilder(
            LdapResultSetBuilder ldapResultSetBuilder) {
        this.ldapResultSetBuilder = ldapResultSetBuilder;
    }

    @Required
    public final void setContextSource(ContextSource contextSource) {
        this.contextSource = contextSource;
    }

    public DirContext bind(Hashtable env) throws NamingException {
        if (log.isDebugEnabled()) {

            log.debug("LdapResource bind Directory Context");
        }

        DirContext ctx = new InitialDirContext(env);

        return ctx;
    }

    public DirContext bind(String userDN, String userPassword) throws NamingException {
        if (userDN == null || userPassword == null)
            throw new NamingException(getLocalMessage(ERROR_INVALID_USERNAME_PASSWORD));

        Hashtable env = this.getUserEnvironment(userDN, userPassword);

        return this.bind(env);
    }


    public List<LdapResultSet> search(String filter, String[] attributesToReturn) throws NamingException {

        return this.search(contextSource.getBase(), filter, attributesToReturn);
    }

    public List<LdapResultSet> search(String base, String filter, String[] attributesToReturn) throws NamingException {
        if (log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            if (attributesToReturn != null) {
                for (String attr : attributesToReturn) {
                    sb.append(attr).append(" ");
                }
            }

            log.debug("LdapResource search(" + "base = " + base + ", filter = "
                    + filter + " attributesToReturn = " + sb.toString());
        }

        Hashtable env = this.getManagerEnvironment();
        DirContext ctx = this.bind(env);

        List<LdapResultSet> list = new ArrayList<LdapResultSet>();
        NamingEnumeration results = null;

        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(attributesToReturn);
            results = ctx.search(base, filter, controls);

            while (results.hasMore()) {
                SearchResult result = (SearchResult) results.next();
                LdapResultSet resultSet = ldapResultSetBuilder.buildLdapResultSet(result, attributesToReturn);
                list.add(resultSet);
            }
        } catch (NameNotFoundException e) {
            //base context not found
            log.error("Base Context " + base + " not found with Exception " + e.getLocalizedMessage());
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    //Do nothing here
                    log.warn("Exception during closing result sets " + e.getLocalizedMessage());
                }
            }

            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    //Do nothing here
                    log.warn("Exception during closing Directory Context " + e.getLocalizedMessage());
                }
            }
        }

        return list;
    }


    public void unbind(DirContext ctx) {

        if (ctx != null) {
            try {
                ctx.close();
            } catch (Exception e) {
                //Do nothing here
                log.warn("Exception during closing Directory Context " + e.getLocalizedMessage());
            }
        }
    }

    protected Hashtable getManagerEnvironment() {
        Hashtable<String, String> env = new Hashtable<String, String>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, contextSource.getContextFactory());
        env.put(Context.PROVIDER_URL, contextSource.getProviderUrl());
        env.put(Context.SECURITY_AUTHENTICATION, contextSource.getAuthenticationType());
        env.put(Context.SECURITY_PRINCIPAL, contextSource.getManagerDN());
        env.put(Context.SECURITY_CREDENTIALS, contextSource.getManagerPassword());

        String protocol = contextSource.getSecurityProtocol();
        //use white list, ignore all others that are not "ssl"
        if (protocol != null && protocol.equalsIgnoreCase("ssl")) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            if (this.trustStore != null)
                System.setProperty("javax.net.ssl.trustStore", this.trustStore);
            if (this.trustStoreType != null)
                System.setProperty("javax.net.ssl.trustStoreType", this.trustStoreType);
            if (this.trustStorePassword != null)
                System.setProperty("javax.net.ssl.trustStorePassword", this.trustStorePassword);
        }

        if (useConnectionPool) {
            env.put(CONNECTION_POOL_KEY, "true");
        }

        return env;
    }

    protected Hashtable getUserEnvironment(String userDN, String userPassword) {
        Hashtable<String, String> env = new Hashtable<String, String>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, contextSource.getContextFactory());
        env.put(Context.PROVIDER_URL, contextSource.getProviderUrl());
        env.put(Context.SECURITY_AUTHENTICATION, contextSource.getAuthenticationType());
        env.put(Context.SECURITY_PRINCIPAL, userDN);
        env.put(Context.SECURITY_CREDENTIALS, userPassword);

        String protocol = contextSource.getSecurityProtocol();
        if (protocol != null && protocol.equalsIgnoreCase("ssl")) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            if (this.trustStore != null)
                System.setProperty("javax.net.ssl.trustStore", this.trustStore);
            if (this.trustStoreType != null)
                System.setProperty("javax.net.ssl.trustStoreType", this.trustStoreType);
            if (this.trustStorePassword != null)
                System.setProperty("javax.net.ssl.trustStorePassword", this.trustStorePassword);
        }

        return env;
    }
}
```

Again, most setters and getters are skipped to make the code more clear. The @Required annotation is used for Spring wiring and the system variables such as "javax.net.ssl.trustStore", "javax.net.ssl.trustStoreType", and "javax.net.ssl.trustStorePassword" are used for Ldaps support. Another way to support the trust store is to add trusted Ldap server CA cert into JDK/jre/lib/security/cacerts, but which may not be preferable. Be aware that you should put the LDAP server CA cert into the trust store instead of individual node cert in a production cluster environment.

## Ldap DAO ##

Based on the above classes, we can implement a Ldap dao just like a database DAO

```
public class LdapEmployeeProfileDAOImpl extends I18NSupportImpl
        implements LdapEmployeeCredentialDAO, LdapEmployeeAttributes, LdapEmployeeFilter {

    private static Log log = LogFactory.getLog(LdapEmployeeProfileDAOImpl.class);

    private LdapResource ldapResource;

    private LdapEmployeeProfileBuilder ldapEmployeeProfileBuilder;

    private String[] attributesToReturn;

    public final LdapEmployeeProfileBuilder getLdapEmployeeProfileBuilder() {
        return ldapEmployeeProfileBuilder;
    }

    @Required
    public final void setLdapEmployeeProfileBuilder(
            LdapEmployeeProfileBuilder ldapEmployeeProfileBuilder) {
        this.ldapEmployeeProfileBuilder = ldapEmployeeProfileBuilder;
    }

    @Required
    public final void setLdapResource(LdapResource ldapResource) {
        this.ldapResource = ldapResource;
    }

    public boolean bindUser(String userDN, String userPassword) {
        boolean isSuccessful = false;
        DirContext ctx = null;

        try {
            ctx = ldapResource.bind(userDN, userPassword);
            isSuccessful = true;
        } catch (NamingException e) {
            log.warn("Cannot bind user with userDN = " + userDN + " with exception " + e.getLocalizedMessage());
        } finally {

            //unbind and close connection
            if (ctx != null)
                ldapResource.unbind(ctx);
        }

        return isSuccessful;
    }

    public LdapEmployeeProfile findUser(String userId) {
        if (userId == null) {
            if (log.isDebugEnabled())
                log.debug("Cannot find user with Null userId = ");

            return null;
        }

        //TODO: add filter builders
        String filter = FILTER.replaceFirst(FILTER_TOKEN, userId);
        LdapEmployeeProfile profile = null;

        try {
            List<LdapResultSet> list = ldapResource.search(filter, attributesToReturn);
            if (list != null) {
                if (list.size() == 0) {
                    if (log.isDebugEnabled())
                        log.debug("Cannot find user with userId = " + userId);

                    return null;
                }

                if (list.size() > 1) {
                    log.warn("Duplicated records found for user with userId = " + userId);
                    //if duplicated, only return the first record
                } else {
                    if (log.isDebugEnabled())
                        log.debug("One record found for user with userId = " + userId);
                }

                LdapResultSet resultSet = list.get(0);

                profile = ldapEmployeeProfileBuilder.buildLdapEmployeeProfile(resultSet);
            }
        } catch (NamingException e) {
            log.warn("Cannot find user with userId = " + userId
                    + " with exception " + e.getLocalizedMessage());
        }

        if (log.isDebugEnabled()) {
            if (profile != null) {
                log.debug("Found user: " + profile.toString());
            }
        }

        return profile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
 
        this.attributesToReturn = new String[5];
        this.attributesToReturn[0] = FIRST_NAME;
        this.attributesToReturn[1] = LAST_NAME;
        this.attributesToReturn[2] = USER_ID;
        this.attributesToReturn[3] = DN;
        this.attributesToReturn[4] = ACCOUNT_EXPIRES;
    }
}

```

## Ldap Authentication ##

One example to authenticate the Ldap user is as follows,

```
	protected boolean authCredential(WorkflowContext context, Principal principal, Credential credential) {
		boolean isSuccessful = false;
		LdapEmployeeProfile profile = ldapEmployeeProfileDAO.findUser(principal.getName());
		if(profile != null){
			UsernamePasswordCredential userCredential = (UsernamePasswordCredential)credential;
			
			isSuccessful = ldapEmployeeProfileDAO.bindUser(profile.getDn(), userCredential.getValue());

            if(log.isDebugEnabled()){
                log.debug("Authenticate UsernamePasswordCredential for " + principal.getName()
                        + " against ldap is " + (isSuccessful ? " Successful" : " Failed"));
            }
		}else{
            if(log.isDebugEnabled()){
                log.debug("Cannot find the profile for " + principal.getName()
                        + " in ldap server");
            }
        }
		
		return isSuccessful;
	}

```