

# Introduction #

I like to share my experience with Google Guice when I implemented the [Smart Account Management Framework](http://code.google.com/p/sacct/wiki/SAcctUserGuide_0_1_0) (SAcct) server. Honestly, I had never used Google Guice before, but liked to take this chance to experience its cool features. Another reason was that its light weight and perfectly fits my need for the SAcct server because the SAcct server should be a small, standalone, and light weight Java application. I have used the Spring framework for many years, but Spring seems an overkill here because I do not need an application container.

The first sight of Google Guice was a bit strange and I was not familiar with the "Guice" way. But [the Google Guice](http://code.google.com/p/google-guice/wiki/Motivation?tm=6) project site provides pretty good documents. As I read it through and was getting used to it. The cool part of the Guice is that you can use annotations to auto-wire for the most part.  Although Spring provides the @Autowired and other annotations a long time ago, but I have rarely used it.

Here, I list some of the Google Guice features I used for the SAcct server.

## `@Inject` ##

The @Inject annotation indicates the dependency injection point. You can put it on setters, for example, in our AccountServerServiceImpl class, we need to wire in the account manager and the session manager, we put the @Inject annotation on their setters.

```
public class AccountServerServiceImpl extends BaseServiceImpl implements GuiceAccountServerService {

    private AccountManager accountManager;

    private SessionManager sessionManager;

    public AccountManager getAccountManager() {
        return accountManager;
    }

    @Inject
    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    @Inject
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
   
    ......
}
```

You can also put @Inject on the constructor like what we did for the  IdleTimeoutTokenExpirationPolicy class

```
public final class IdleTimeoutTokenExpirationPolicy implements TokenExpirationPolicy {
    
    private final long idletimeInMilliSeconds;

    @Inject
    public IdleTimeoutTokenExpirationPolicy(ServerConfig config){
        this.idletimeInMilliSeconds = config.getIdletimeInMilliSeconds();
    }
    ......
}
```

## `@ImplementedBy` ##

Guice provides Just-In-Time bindings or implicit bindings. The @ImplementedBy is one of them. Usually, you should put this annotation on an interface so that Guice knows what its default implementation is. For instance, we used @ImplementedBy on the GuiceAccountServerService interface and Guice will automatically bind the implementation AccountServerServiceImpl.class to it.

```
@ImplementedBy(AccountServerServiceImpl.class)
public interface GuiceAccountServerService extends SessionService, AccountService{
    
}
```

## Provider ##

Sometimes, you need to generate a new object for each service request and you cannot simply use the @Inject for auto-wiring. The Guice Provider is here for you, which is a Java generic interface.

```
public interface Provider<T> {
  T get();
}
```

For example, in the SAcct server, we need to create a new session token for each session and we create a SessionTokenProvider for this purpose.

```
public class SessionTokenProvider implements Provider<SessionToken> {

    private TokenExpirationPolicy expirationPolicy;

    private TokenIdGenerator idGenerator;

    public TokenIdGenerator getIdGenerator() {
        return idGenerator;
    }

    @Inject
    public void setIdGenerator(TokenIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public TokenExpirationPolicy getExpirationPolicy() {
        return expirationPolicy;
    }

    @Inject
    public void setExpirationPolicy(TokenExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
    }

    public SessionToken get() {
        
        return new SessionToken(this.idGenerator.getNewTokenId(SessionToken.PREFIX), this.expirationPolicy);
    }
}
```

## `@Singleton` ##

By default, Guice returns a new instance each time it supplies a value. This behaviour is configurable via scopes. @Singleton is one of the Scopes to allow you to reuse instances for the lifetime of an application. Our AccountManagerImpl class is a singleton class with the @Singleton annotation.

```
@Singleton
public class AccountManagerImpl implements AccountManager {

    private AccountRegistry registry;

    private AccountRegistryLoader registryLoader;

}
```

## `@Named` ##

I was puzzled for a while when tried to inject primitive variables to a class. Finally, I found the solution, i.e., the @Named annotation. With the @Named annotation, you can inject the variable with the same name from a Java property file. For example, our ServerConfigImpl is a central place to hold the configuration for the SAcct server and it is implemented with the @Named annotation as follows,

```
@Singleton
public class ServerConfigImpl implements ServerConfig {

    private boolean useOTP = false;

    private String accountFileName = "accounts.reg";

    private int serverPort = 9000;

    private int poolSize = 12;

    private int randomIdLength = 12;

    private int maxSessionNum = 100;

    private int numToCleanSession = 25;

    private String sessionExpirationPolicy = "IdleTimeout";

    private long idletimeInMilliSeconds = 3000000;

    public String getSessionExpirationPolicy() {
        return sessionExpirationPolicy;
    }

    public long getIdletimeInMilliSeconds() {
        return idletimeInMilliSeconds;
    }

    public boolean isUseOTP() {
        return useOTP;
    }

    @Inject(optional=true)
    public void setUseOTP(@Named("useOTP") boolean useOTP) {
        this.useOTP = useOTP;
    }

    @Inject(optional=true)
    public void setIdletimeInMilliSeconds(@Named("idletimeInMilliSeconds") long idletimeInMilliSeconds) {
        this.idletimeInMilliSeconds = idletimeInMilliSeconds;
    }

    @Inject(optional=true)
    public void setSessionExpirationPolicy(@Named("sessionExpirationPolicy") String sessionExpirationPolicy) {
        this.sessionExpirationPolicy = sessionExpirationPolicy;
    }

   ......
}
```

Then we create a ConfigModule type class to load the variables from the Java property file, sacct.properties.

```
public class ServerConfigModule extends AbstractModule {
    public static final String SERVER_BASE = "server.base";

    @Override
    protected void configure() {
        // bind the properties
        try {
            Properties properties = loadProperties();
            java.util.Properties p = System.getProperties();
            String serverBase = p.getProperty(SERVER_BASE);
            //set the account File to the data directory
            if (serverBase != null && serverBase.trim().length() > 0) {
                String accountName = properties.getProperty("accountFileName");
                properties.setProperty("accountFileName", serverBase + "/data/" + accountName);
            }
            Names.bindProperties(binder(), properties);
            System.out.println("Loaded SAcct Server Configuration successfully");
        } catch (Exception e) {
            // handle the exception
            System.out.println("Error loading Server Configuration " + e.getMessage());
            System.out.println("Use Server Default Configuration.");
        }
    }

    ......
}
```

In this way, all variables in the sacct.properties are automatically injected to the appropriate fields in the ServerConfigImpl class. The sacct.properties file is as follows,

```
useOTP = true
sessionExpirationPolicy = IdleTimeout
idletimeInMilliSeconds = 3000000
maxSessionNum = 200
numToCleanSession = 25
accountFileName = accounts.reg
serverPort = 9000
poolSize = 12
randomIdLength = 12
```

You may notice that we used the optional attribute in @Inject, i.e., @Inject(optional=true), which is very useful for the SAcct server because the sacct.properties is an optional configuration file and users may not create this file. In such a case, we need to use the default values from the ServerConfigImpl class. If the (optional=true) is not included, Guice will throw exceptions if it could not find variables to bind to the ServerConfigImpl class.

# Summary #

The Google Guice is a cool and light weight dependency injection framework for some applications that do not require an application container such as the SAcct server. You can use annotations to do autowiring for the most part. For some configuration, you have to use a ConfigModule type class to specify the bindings. The only downside I can see so far is that all the bindings are either specified by annotations or a ConfigModule type class and thus, you may have to re-compile the code if you change the bindings, which may not be as flexible as Spring from this point of view. In Spring, you can change the Spring wiring XML files without re-compiling your code. Other than that, I am very impressed by Guice's cool features.

# Resources #

  * [Google Guice](http://code.google.com/p/google-guice/)
  * [SAcct 0.1.0 User Guide](http://code.google.com/p/sacct/wiki/SAcctUserGuide_0_1_0)