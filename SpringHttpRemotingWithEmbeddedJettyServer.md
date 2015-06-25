

# Introduction #

I like to create a standalone Java application but still use Spring http remoting. Usually Spring http remoting requires a web container or you can use Java 6's http server in the following way

```
    <bean name="helloServiceExporter"
          class="org.springframework.remoting.httpinvoker.SimpleHttpInvokerServiceExporter">
        <property name="service" ref="helloService"/>
        <property name="serviceInterface" value="com.mycompany.service.iface.HelloService"/>
    </bean>

    <bean id="httpServer"
          class="org.springframework.remoting.support.SimpleHttpServerFactoryBean">
        <property name="contexts">
            <util:map>
                <entry key="/http/HelloService" value-ref="helloServiceExporter"/>
            </util:map>
        </property>
        <property name="port" value="8088"/>
    </bean>
```

But if run the application in production, it would be better to use a more reliable http server. Jetty is a light weight framework and embedded jetty server fits perfectly for this purpose.

In the past, I created a separate Jetty wrapper to load a war file and run it. The basic concept can be shown by the following code snippet.

```
        Server server = new Server();

        ThreadPool threadPool = new ThreadPool();
        threadPool.setCorePoolSize(corePoolSize);
        threadPool.setMaximumPoolSize(maxPoolSize);
        server.setThreadPool(threadPool);

        Connector connector = new SelectChannelConnector();
        connector.setPort(this.serverPort);
        connector.setMaxIdleTime(this.maxIdleTime);
        server.setConnectors(new Connector[]{connector});

        WebAppContext webappcontext = new WebAppContext();
        webappcontext.setContextPath("/");
        File tempDirectory = new File(this.tempDir);
        webappcontext.setTempDirectory(tempDirectory);
        webappcontext.setWar(this.warDir);
        webappcontext.addServlet(new ServletHolder(new org.apache.jasper.servlet.JspServlet()), "*.jsp");

        HandlerCollection handlers = new HandlerCollection();
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        handlers.setHandlers(new Handler[]{webappcontext, new DefaultHandler(), requestLogHandler});
        server.setHandler(handlers);

        NCSARequestLog requestLog = new NCSARequestLog("../logs/" + this.logFormat);
        requestLog.setExtended(false);
        requestLogHandler.setRequestLog(requestLog);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            logger.error("Error starting embedded Jetty server, " + LogUtils.describeException(e));
            throw new RuntimeException("Error starting Jetty server", e);
        }
```

But this time, I like to create a real standalone Java application instead of a web application project. I tried two ways and both work fine.

# Programmatically #

The code is similar to the above jetty code, but it uses a DispatcherServlet instead of a war file.

```
            Server server = new Server();
            SelectChannelConnector connector = new SelectChannelConnector();
            connector.setPort(port);

            Context context = new Context(server, "/", Context.SESSIONS);

            DispatcherServlet dispatcherServlet = new DispatcherServlet();
            dispatcherServlet.setContextConfigLocation("classpath:com/mycompany/config/DefaultServlet-servlet.xml");

            ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
            context.addServlet(servletHolder, "/*")
```

The key is the servlet XML file DefaultServlet-servlet.xml.

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:mvc="http://www.springframework.org/schema/mvc"
	xsi:schemaLocation="http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc-3.0.xsd
		http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd">

 	<!-- This default handler takes care of each of the services enumerated below -->
	<bean id="defaultHandlerMapping"
		class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping" />

    <bean id="helloService" class="com.mycompany.service.impl.HelloServiceImpl"/>

	<!-- SpringHTTP Service Exposure -->

	<bean name="/HelloService"
		class="org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter"
		lazy-init="true">
		<property name="service" ref="helloService" />
		<property name="serviceInterface"
			value="com.mycompany.service.iface.HelloService" />
	</bean>
</beans>
```

Here, no need to define a web.xml file.

# Spring Configuration #

Similarly, we could achieve the same goal by using the following Spring configuration.

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://www.springframework.org/schema/context
                           http://www.springframework.org/schema/context/spring-context.xsd">

    <!-- Manually start server after setting parent context. (init-method="start") -->
    <bean id="jettyServer"
          class="org.mortbay.jetty.Server"
          destroy-method="stop">
        <property name="threadPool">
            <bean id="ThreadPool"
                  class="org.mortbay.thread.concurrent.ThreadPool">
                <constructor-arg value="0" />
                <property name="corePoolSize" value="${jetty.server.thread.pool.core.pool.size}"/>
                <property name="maximumPoolSize" value="${jetty.server.thread.pool.max.pool.size}"/>
            </bean>
        </property>
        <property name="connectors">
            <list>
                <bean id="Connector"
                      class="org.mortbay.jetty.nio.SelectChannelConnector"
                      p:port="${jetty.server.port}"
                      p:maxIdleTime="${jetty.server.max.idle.time}"
                      p:acceptors="${jetty.server.acceptor.num}"
                      p:confidentialPort="${jetty.server.ssl.port}" />
            </list>
        </property>

        <property name="handler">
            <bean class="org.mortbay.jetty.handler.HandlerCollection">
                <property name="handlers">
                    <list>
                        <bean class="org.mortbay.jetty.servlet.Context">
                          <property name="contextPath" value="/"/>
                          <property name="sessionHandler">
                            <bean class="org.mortbay.jetty.servlet.SessionHandler"/>
                          </property>
                          <property name="resourceBase" value="."/>
                          <property name="servletHandler">
                            <bean class="org.mortbay.jetty.servlet.ServletHandler">
                              <property name="servlets"> <!-- servlet definition -->
                                <list>
                                <!-- default servlet -->
                                <bean class="org.mortbay.jetty.servlet.ServletHolder">
                                  <property name="name" value="DefaultServlet"/>
                                  <property name="servlet">
                                    <!--<bean class="org.mortbay.jetty.servlet.DefaultServlet"/>-->
                                      <bean class="org.springframework.web.servlet.DispatcherServlet"/>
                                  </property>
                                  <property name="initParameters">
                                    <map>
                                        <entry key="contextConfigLocation" value="classpath:com/mycompany/config/DefaultServlet-servlet.xml" />
                                    </map>
                                  </property>
                                </bean>
                                </list>
                              </property>
                              <property name="servletMappings">
                                <list><!-- servlet mapping -->
                                <bean class="org.mortbay.jetty.servlet.ServletMapping">
                                  <property name="pathSpecs">
                                    <list><value>/</value></list>
                                  </property>
                                  <property name="servletName" value="DefaultServlet"/>
                                </bean>
                                </list>
                              </property>
                            </bean>
                          </property>
                        </bean>
                        <bean class="org.mortbay.jetty.handler.RequestLogHandler">
                            <property name="requestLog">
                                <bean class="org.mortbay.jetty.NCSARequestLog">
                                    <constructor-arg value="${jetty.server.log.dir}/jetty-yyyy_mm_dd.log"/>
                                    <property name="extended" value="false"/>
                                </bean>
                            </property>
                        </bean>
                    </list>
                </property>
            </bean>
        </property>
    </bean>

</beans>
```

# Client #

To test the embedded Jetty application, we can use the following Spring http remoting client.

```

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:util="http://www.springframework.org/schema/util"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
       http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.0.xsd">

    <bean id="httpRemotingHelloService"
		class="org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean">
		<property name="serviceUrl"
			value="http://localhost:8087/HelloService">
		</property>
		<property name="serviceInterface"
			value="com.mycompany.service.iface.HelloService">
		</property>
	</bean>

</beans>

```

Test case

```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/mycompany/service/HelloServiceClient_FuncTest.xml"})
public class HelloServiceClient_FuncTest {
    @Autowired
    @Qualifier("httpRemotingHelloService")
    private HelloService helloService;

    @Test
    public void testSayHello(){
        String hello = helloService.sayHello("John");
        assertNotNull(hello);
        System.out.println("sayHello: " + hello);
    }
}
```


The HelloService interface is defined as

```
public interface HelloService {

    String sayHello(String name);

}
```

and the implementation is simple.

```
public class HelloServiceImpl implements HelloService {

    public String sayHello(String name) {
        return "Hello " + name + "!";
    }
}

```