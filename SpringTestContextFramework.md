# Introduction #

Seems Spring provides a [TextContext framework](http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/testing.html#testcontext-framework) to load up test context starting from 2.5. I didn't have a chance to try that feature until now. The TextContext is very elegant to define the context by annotations. Here I list my steps to make it work.

# Maven Dependency #

The TextContext framework can be included by the following Maven dependency.

```
       <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>org.springframework.test</artifactId>
            <version>${spring.version}</version>
        </dependency>
```

# Test Case #

Take a circuit breaker application as an example, I want to test the interceptor. The test case is defined as follows.

```
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CircuitBreakerInterceptor_FuncTest {
    @Autowired
    private ThreadPool threadPool;

    @Autowired
    private CircuitRegistry registry;

    @Test
    public void testMaxCapacity(){
        for(int i=0; i<5; i++){
            threadPool.run();
        }

        try{
            threadPool.run();
            fail("Should fail here");
        }catch(Exception e){

        }
    }

...
}
```

If we use @ContextConfiguration without any parameters, the framework assumes that our  context xml file uses the name convention, i.e., the class name plus "-context.xml". In our case, the file name is CircuitBreakerInterceptor\_FuncTest-context.xml.

If we want a different file name, we can specify it either by class path resource

```
@ContextConfiguration(locations={"classpath:/com/mycompany/component/aop/impl/spring/CircuitBreakerInterceptor_FuncTest.xml"})
```

or by the file name directly.

```
@ContextConfiguration(locations={"CircuitBreakerInterceptor_FuncTest.xml"})
```

# Wiring #

The wiring file is as follows.

```
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
        xmlns:aop="http://www.springframework.org/schema/aop" xmlns:context="http://www.springframework.org/schema/context"
        xsi:schemaLocation="
                        http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-3.0.xsd
                        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
                        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd">
    
    <context:annotation-config/>
    
    <context:component-scan  base-package="com.mycompany.component"/>

    <!-- Advice -->
    <aop:aspectj-autoproxy/>

    <bean id="circuitbreaker_interceptor" class="com.mycompany.component.aop.impl.spring.CircuitBreakerInterceptorImpl">

        <property name="registry" ref="circuitbreaker_registry"/>
        <property name="eventListener" ref="eventlistener_chain"/>

    </bean>

    <bean id="circuitbreaker_registry" class="com.mycompany.component.circuitbreaker.impl.spring.CircuitRegistryImpl">

    </bean>

    <bean id="threadPool" class="com.mycompany.component.threadpool.impl.spring.ThreadPoolImpl"/>

...

</beans>
```

The the first two lines are used to enable the @Autowired annotation.