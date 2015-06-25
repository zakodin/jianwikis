

# Introduction #

[Circuit Breaker](http://en.wikipedia.org/wiki/Circuit_breaker_design_pattern) is a design pattern used to detect failures and encapsulates logic of preventing a failure to reoccur constantly. Circuit breaker detects failures and prevents the application from trying to perform the action that is doomed to fail.

The circuit breaker has the following three states.

```
public enum CircuitStateCode {
    CLOSED,
    HALF_OPEN,
    OPEN
}
```

  * When the application starts, it is in the closed state
  * If failure threshold reaches, it goes to the open state
  * After some time, the state goes to half open and the application tries to probe if the resource is available
  * If the probe is successful, it goes to the closes state, otherwise, it goes to the open state.

# Implementation #

I implemented the circuit breaker component in Java with Spring AOP and the implementation details are listed as follows.

## Annotation ##

To use the circuit breaker, you can simply use the following Java annotation on the resource that you like to prevent.

```
@Retention(RUNTIME) @Target({METHOD})
public @interface CB {
    
    String id();

    String[] policies();

    long timeout() default 300000;  //5 minutes

    int maxFailures() default 1;

    int maxCapacity() default 100;       //

    long methodExecutionTimeout() default 300000;  //5 minutes

    boolean publishEvent() default true;
}
```

where id is the circuit breaker id, timeout is the interval that the application goes from the open state to half open state. The policies are defined by the following enum type.

```
public enum CircuitPolicyCode {
    MAX_FAILURES,
    MAX_CAPACITY,
    MAX_EXECUTION_TIME,
    OPEN_FOREVER,
    CLOSE_FOREVER
}
```

That is to say, the circuit breaker could be controlled by one or more policies. The OPEN\_FOREVER and CLOSE\_FOREVER are used to put the circuit breaker in a permanent state. Obviously, the maxFailures, maxCapacity, and methodExecutionTimeout
are parameters for policy MAX\_FAILURES, MAX\_CAPACITY, and MAX\_EXECUTION\_TIME, respectively.

If the publishEvent parameter is true, the circuit breaker will publish the following JMX events when state changes.

```
public enum EventTypeCode {
    CIRCUIT_CLOSED,
    CIRCUIT_OPENED,
    CIRCUIT_HALF_OPENED
}
```

## Interceptor ##

The @CB annotation will be intercepted by the circuit breaker interceptor. The interceptor will get the parameters from the annotations and pass them to the circuit control class.

```
public interface CircuitControl {

    void preCall();

    void onSuccess();

    void onFailure();
}
```

The interceptor is a typical Spring Aspect class.

```
@Aspect
public class CircuitBreakerInterceptorImpl {

    //method to get runtime capacity
    private static final String CAPACITY_GETTER = "getCapacity";

    private CircuitRegistry registry;

    private Map<String, CircuitPolicy> policies;

    private EventListener eventListener;


    @Required
    public void setRegistry(CircuitRegistry registry) {
        this.registry = registry;
    }

    @Pointcut("@annotation(org.telluriumsource.component.annotation.CB)")
    public void circuitExecution() {}

    @Around("circuitExecution()")
    public Object interceptCircuit(ProceedingJoinPoint pjp) throws Throwable
    {
        boolean includedMaxCapacityPolicy = false;

        CB cbn = this.retrieveAnnotationForMethod(pjp, CB.class);
        String id = cbn.id();
        CircuitBreaker cb = this.registry.findCircuitBreaker(id);
        if(cb == null){
            //need to initialize CircuitBreaker
            if(log.isDebugEnabled()){
                log.debug("Initialize Circuit Breaker " + id);
            }
            CircuitBreakerImpl ncb = new CircuitBreakerImpl();
            ncb.setId(id);
            String[] policies = cbn.policies();
            if(policies != null && policies.length > 0){
                Map<CircuitPolicyCode, CircuitPolicy> ps = new HashMap<CircuitPolicyCode, CircuitPolicy>();

                for(String policy: policies){
                    CircuitPolicy p = this.policies.get(policy);
                    if(p != null){
                        ps.put(CircuitPolicyCode.valueOf(policy), p);
                    }else{
                        //log warning
                        log.warn("Invalid policy " + policy);
                    }
                }

                ncb.setPolicies(ps);
            }

            CircuitBreakerConfig overwriteConf = this.registry.getOverwriteConfig(id);
            if (overwriteConf == null) {
                ncb.setMaxCapacity(cbn.maxCapacity());
                ncb.setMaxFailures(cbn.maxFailures());
                ncb.setTimeout(cbn.timeout());
                ncb.setMethodExecutionTimeout(cbn.methodExecutionTimeout());
                ncb.setPublishEvent(cbn.publishEvent());
            } else {
                ncb.setMaxCapacity(overwriteConf.getMaxCapacity());
                ncb.setMaxFailures(overwriteConf.getMaxFailures());
                ncb.setTimeout(overwriteConf.getTimeout());
                ncb.setMethodExecutionTimeout(overwriteConf.getMethodExecutionTimeout());
                ncb.setPublishEvent(overwriteConf.isPublishEvent());
            }
            if (this.eventListener != null) {
                ncb.setEventListener(this.eventListener);
            }


            this.registry.addCircuitBreaker(id, ncb);
            cb = ncb;
        }


        if(cb.includePolicy(CircuitPolicyCode.MAX_CAPACITY)){
            includedMaxCapacityPolicy = true;
        }

        if(includedMaxCapacityPolicy){
            if(log.isDebugEnabled()){
                log.debug("Max capacity policy is included, need to get runtime capacity");
            }
            try {
                Method method = pjp.getTarget().getClass().getDeclaredMethod(CAPACITY_GETTER);
                int capacity = (Integer)method.invoke(pjp.getTarget());
                if (log.isDebugEnabled()) {
                    log.debug("Got runtime capacity " + capacity);
                }
                cb.setCurrentCapacity(capacity);
            } catch (NoSuchMethodException e) {
                log.error(e.getMessage());
                throw new ComponentException(e);
            } catch (InvocationTargetException e) {
                log.error(e.getMessage());
                throw new ComponentException(e);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
                throw new ComponentException(e);
            }
        }


        CircuitControl cc = (CircuitControl) cb;
        cc.preCall();
        if (log.isDebugEnabled()) {
            log.debug("Circuit Breaker " + id + " state before execution: " + ((CircuitState)cb).getStateCode().toString());
        }
        try {
            if(!cb.isCallable()){
               log.error( "Circuit breaker " + id + " is open");
               throw new OpenCircuitException(cb, "Circuit breaker " + id + " is open");
            }

            Object result = pjp.proceed();
            if (log.isDebugEnabled()) {
                log.debug("Command executed successfully!");
            }
            cc.onSuccess();

            if (log.isDebugEnabled()) {
                log.debug("Circuit Breaker " + id + " state after execution: " + ((CircuitState) cb).getStateCode().toString());
            }
            return result;
        } catch (Throwable throwable) {
            if (log.isDebugEnabled()) {
                log.debug("Command execution failed!");
            }
            cc.onFailure();

            log.error(throwable.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Circuit Breaker " + id + " state after execution: " + ((CircuitState) cb).getStateCode().toString());
            }

            throw throwable;
        }
    }

}
```

## Circuit Breaker ##

The circuit breaker has the following interface.

```
public interface CircuitBreaker {
    
    String getId();

    CircuitStateCode getStateCode();

    void forceOpen();

    void forceClose();

    void clearForceOpen();

    void clearForceClose();

    boolean isCallable();

    boolean isClosed();

    boolean isOpen();

    boolean isHalfOpen();

    void recordSuccess();

    void recordFailure();

    void setCurrentCapacity(int capacity);

    boolean includePolicy(CircuitPolicyCode code);
}
```

where the method forceOpen() and forceClose() are used to force the circuit breaker to open or close state under certain circumstance.

The circuit breaker state can be accessed by the following interface.

```
public interface CircuitState {

    CircuitStateCode getStateCode();

    long getTimeout();

    int getMaxFailures();

    int getMaxCapacity();

    long getMethodExecutionTimeout();

    long getCurrentMethodCallTimestamp();

    int getCurrentFailures();

    int getCurrentCapacity();

    long getCurrentOpenTimestamp();

    int getTimesOpened();

    int getTotalFailures();

    int getTotalCalls();
}
```

The CircuitBreakerImpl class is the implementation of a circuit breaker.

```
public class CircuitBreakerImpl implements CircuitBreaker, CircuitConfigurator, CircuitState, CircuitControl, EventPublisher {
   private String id;

    private final AtomicInteger currentFailures = new AtomicInteger();
    private final AtomicLong firstCurrentFailureTimestamp = new AtomicLong();
    private final AtomicLong openTimestamp = new AtomicLong();
    private final AtomicInteger totalCalls = new AtomicInteger();
    private final AtomicInteger totalFailures = new AtomicInteger();
    private final AtomicInteger timesOpened = new AtomicInteger();
    private final AtomicLong currentMethodCallTimestamp = new AtomicLong();

    private final AtomicInteger capacity = new AtomicInteger();

    private int maxCapacity;

    private int maxFailures = DEFAULT_MAX_FAILURES;
    private long timeout = DEFAULT_TIMEOUT;
    private long methodExecutionTimeout;

    private boolean forceOpen = false;
    private boolean forceClose = false;

    private CircuitStateCode stateCode = CircuitStateCode.CLOSED;

    private CircuitStateCode currentState = stateCode;

    private Map<CircuitPolicyCode, CircuitPolicy> policies;

    private EventListener eventListener;

    private boolean publishEvent = false;

    public synchronized void onFailure() {
        this.recordCall();
        this.recordFailure();

        //CLOSE_FOREVER and OPEN_FOREVER policies are honored over the runtime forceClose and forceOpen flags
        if (this.policies.get(CircuitPolicyCode.CLOSE_FOREVER) != null) {

            this.stateCode = this.policies.get(CircuitPolicyCode.CLOSE_FOREVER).checkState(this);
        } else if (this.policies.get(CircuitPolicyCode.OPEN_FOREVER) != null) {

            this.stateCode = this.policies.get(CircuitPolicyCode.OPEN_FOREVER).checkState(this);
        } else if (!this.forceClose && !this.forceOpen) {
            if (this.stateCode == CircuitStateCode.HALF_OPEN) {
                this.stateCode = CircuitStateCode.OPEN;
                //reset the timeout so that it will delay for another timeout interval
                this.openTimestamp.set(System.currentTimeMillis());
                this.timesOpened.getAndIncrement();
                if (this.publishEvent) {
                    CircuitBreakerEvent event = new CircuitBreakerEvent(this.id, System.currentTimeMillis(),
                            EventTypeCode.CIRCUIT_OPENED, EventReasonCode.RETRY_FAILED);
                    this.publish(event);
                }

            } else if (this.stateCode == CircuitStateCode.CLOSED) {
                //process policy only if the forceClose and forceOpen flags are not set
                if (this.policies.get(CircuitPolicyCode.MAX_FAILURES) != null) {
                    this.stateCode = this.policies.get(CircuitPolicyCode.MAX_FAILURES).checkState(this);
                    if (this.isStateChanged()) {
                        this.timesOpened.getAndIncrement();
                        this.openTimestamp.set(System.currentTimeMillis());
                        if (this.publishEvent) {
                            CircuitBreakerEvent event = new CircuitBreakerEvent(this.id, System.currentTimeMillis(),
                                    EventTypeCode.CIRCUIT_OPENED, EventReasonCode.MAX_FAILURES_REACHED);
                            this.publish(event);
                        }
                    }
                }
            }
        }
    }

    ......
}
```

## Circuit Breaker Factory Bean ##

Circuit breaker could also be created by the following Spring factory bean.

```
public class CircuitBreakerFactoryBean implements FactoryBean, InitializingBean {
    private CircuitBreaker cb;

    private String id;

    private String[] policies;

    private long timeout = 300000;

    private int maxFailures = 1;

    private int maxCapacity = 10;

    private long methodExecutionTimeout = 3600000;

    private boolean publishEvent = true;

    private EventListener eventListener;

    private static Map<String, CircuitPolicy> policyMap = new CircuitPolicyMap();

    ......

    @Override
    public void afterPropertiesSet() throws Exception {

        CircuitBreakerImpl ncb = new CircuitBreakerImpl();
        ncb.setId(id);
        if (policies != null && policies.length > 0) {
            Map<CircuitPolicyCode, CircuitPolicy> ps = new HashMap<CircuitPolicyCode, CircuitPolicy>();

            for (String policy : policies) {
                CircuitPolicy p = policyMap.get(policy);
                if (p != null) {
                    ps.put(CircuitPolicyCode.valueOf(policy), p);
                } else {
                    //log warning
                }
            }

            ncb.setPolicies(ps);
        }
        ncb.setMaxCapacity(maxCapacity);
        ncb.setMaxFailures(maxFailures);
        ncb.setTimeout(timeout);
        ncb.setMethodExecutionTimeout(methodExecutionTimeout);
        ncb.setPublishEvent(publishEvent);

        if (this.eventListener != null) {
            ncb.setEventListener(this.eventListener);
        }

        this.cb = ncb;
    }
}
```

## JMX Manager Bean ##

Usually, we need to manage circuit breakers via JMX beans. I create a manager class for this purpose.

```
@ManagedResource(
        objectName = "org.telluriumsource.component.jmx:name=CircuitBreakerManager",
        description = "Circuit breaker manager")
public class CircuitBreakerManagerMBean {

    private CircuitRegistry circuitRegistry;


    @ManagedOperation(description="Get all circuit breaker ids")
    public String[] getAllCircuitBreakerIds(){
        List<String> list = this.circuitRegistry.getAllCircuitBreakerIds();
        if(list == null)
            return null;
        return list.toArray(new String[0]);
    }

    @ManagedOperation(description = "Get circuit breaker state")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "id", description = "circuit breaker id")})
    public String getState(String id) {
        CircuitState cb = (CircuitState) this.circuitRegistry.findCircuitBreaker(id);
        if (cb != null)
            return cb.getStateCode().toString();

        return null;
    }


    @ManagedOperation(description = "Set circuit breaker retry timeout")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "id", description = "circuit breaker id"),
            @ManagedOperationParameter(name = "timeout", description = "circuit breaker retry timeout")})
    public void setTimeout(String id, long timeout){
        CircuitConfigurator cb = (CircuitConfigurator) this.circuitRegistry.findCircuitBreaker(id);
        if(cb != null)
            cb.setTimeout(timeout);
    }

    @ManagedOperation(description = "Set circuit breaker Max Failures")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "id", description = "circuit breaker id"),
            @ManagedOperationParameter(name = "num", description = "circuit breaker Max Failures")})
    public void setMaxFailures(String id, int num){
        CircuitConfigurator cb = (CircuitConfigurator) this.circuitRegistry.findCircuitBreaker(id);
        if(cb != null)
            cb.setMaxFailures(num);
    }

    ......
}
```

I also defined a JMX notification publisher to publish circuit breaker JMX events to listeners.

```
@ManagedResource(
        objectName = "org.telluriumsource.component.jmx:name=CircuitBreakerNotificationPublisher",
        description = "Circuit breaker Notification Publisher")
public class CircuitBreakerNotificationPublisher implements EventListener, NotificationPublisherAware {
    private boolean active = true;

    private NotificationPublisher publisher;
    
    @ManagedAttribute(description = "Get active attribute")
    public boolean isActive() {
        return active;
    }

    @ManagedAttribute(description = "Set active attribute")
    public void setActive(boolean active) {
        this.active = active;
    }

    public void listen(CircuitBreakerEvent event) {

        if(active)
            this.publisher.sendNotification(new CircuitBreakerNotification(event, this, 0));

    }

    public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
        this.publisher = notificationPublisher;
    }
}
```

One example JMX notification listener is the LoggingNotificationListener.

```
@ManagedResource(
        objectName = "org.telluriumsource.component.jmx:name=LoggingNotificationListener",
        description = "Circuit breaker Notification Logger")
public class LoggingNotificationListener implements NotificationListener, NotificationFilter {
    private static Log log = LogFactory.getLog(LoggingNotificationListener.class);

    private String lastNotification;

    private int queueLength = 10;

    private Queue<String> nQueue = new LinkedList<String>();

    @ManagedAttribute(description = "Get notification queue length")
    public int getQueueLength() {
        return queueLength;
    }

    @ManagedAttribute(description = "Set notification queue length")
    public void setQueueLength(int queueLength) {
        this.queueLength = queueLength;
    }

    @ManagedAttribute(description = "last notification")
    public String getLastNotification() {
        return lastNotification;
    }

    public void setLastNotification(String lastNotification) {
        this.lastNotification = lastNotification;
    }

    @ManagedOperation(description = "Get all notifications")
    public String[] getAllNotifications(){
        return nQueue.toArray(new String[0]);
    }

    public boolean isNotificationEnabled(Notification notification) {
        return CircuitBreakerNotification.class.isAssignableFrom(notification.getClass());
    }


    public void handleNotification(Notification notification, Object handback) {
        log.info("Circuit Breaker Notification: " + notification.toString());
        synchronized(this){
            this.lastNotification = notification.toString();
            if(nQueue.size() < queueLength){
                nQueue.add(lastNotification);
            }else{
                nQueue.poll();
                nQueue.add(lastNotification);
            }
        }
    }
}
```

# Example #

To demonstrate the usage of the circuit breaker, I created a TreadPool with the following interface.

```
public interface ThreadPool {
    int getCapacity();

    void run();

    void stop();

    void work(boolean isSuccessful);

    void lazy(long milliSeconds);

    void always();

    void never();
}
```

The concrete class ThreadPoolImpl uses circuit breakers to control the thread pool resource.

```
public class ThreadPoolImpl implements ThreadPool, CapacityAwareService {
    private int count;

    public int getCapacity() {
        return count;
    }


    @CB(id="run", policies ={"MAX_CAPACITY"}, maxCapacity=5)
    public void run() {
        ++count;
        System.out.println("Count: " + count);
    }


    public void stop() {
        if(count > 0)
            --count;
        
        System.out.println("Count: " + count);
    }


    @CB(id="work", policies ={"MAX_FAILURES", "MAX_CAPACITY"}, timeout=1000, maxFailures=1, maxCapacity=10)
    public void work(boolean isSuccessful) {
        if(!isSuccessful)
            throw new RuntimeException("Work failed");
    }

    @CB(id="lazy", policies ={"MAX_EXECUTION_TIME"}, timeout=3000, methodExecutionTimeout=1500)
    public void lazy(long milliSeconds){
        Helper.pause(milliSeconds);
    }

    @CB(id="always", policies ={"CLOSE_FOREVER"})
    public void always() {
        throw new RuntimeException("Always fails");
    }

    @CB(id="never", policies ={"OPEN_FOREVER"})
    public void never() {
            
    }
}
```

Then, I can use the following function test class to test the circuit breaker.

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

    @Test
    public void testMaxFailures(){
        try{
            threadPool.work(false);
            fail("Should fail here");
        }catch(Exception e){

        }

        Helper.pause(1500);
        try{
            threadPool.work(false);
            fail("Should fail here");
        }catch(Exception e){

        }

        Helper.pause(500);
        try{
            threadPool.work(true);
            fail("Should fail here");
        }catch(Exception e){

        }

        Helper.pause(1000);
        threadPool.work(true);
        threadPool.work(true);
    }

    @Test
    public void testMaxExecutionTimeout(){
        try{
            threadPool.lazy(2000);
        }catch(Exception e){

        }

        Helper.pause(1500);
        try{
            threadPool.lazy(1000);
            fail("Should fail here");
        }catch(Exception e){

        }

        Helper.pause(1500);

        threadPool.lazy(1000);
        threadPool.lazy(1000);
    }

    @Test
    public void testCloseForever(){
        try{
            threadPool.always();
            fail("Should fail here");
        }catch(Exception e){

        }
         try{
            threadPool.always();
            fail("Should fail here");
        }catch(Exception e){

        }
    }

    @Test
    public void testOpenForever(){
        try{
            threadPool.never();
        }catch(Exception e){

        }
         try{
            threadPool.never();
        }catch(Exception e){

        }
    }

    @Test
    public void testRuntimeForceOpenClose(){
        threadPool.work(true);
        CircuitBreakerImpl cb = (CircuitBreakerImpl) registry.findCircuitBreaker("work");
        cb.forceOpen();

        try{
            threadPool.work(true);
        }catch(Exception e){

        }

        cb.clearForceOpen();
        try{
            threadPool.work(true);
            fail("Should fail here");
        }catch(Exception e){

        }
        cb.forceClose();
        threadPool.work(true);

        cb.clearForceClose();
    }

}

```

The Spring wiring file is listed as follows.

```
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xmlns:p="http://www.springframework.org/schema/p"
        xmlns:aop="http://www.springframework.org/schema/aop" 
xmlns:context="http://www.springframework.org/schema/context"
        xsi:schemaLocation="
                        http://www.springframework.org/schema/aop 
http://www.springframework.org/schema/aop/spring-aop-3.0.xsd
                        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
                        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd">
    
    <context:annotation-config/>
    
    <context:component-scan  base-package="org.telluriumsource.component"/>

    <!-- Advice -->
    <aop:aspectj-autoproxy/>

    <bean id="circuitbreaker_interceptor" class="org.telluriumsource.component.aop.impl.spring.CircuitBreakerInterceptorImpl">

        <property name="registry" ref="circuitbreaker_registry"/>
        <property name="eventListener" ref="eventlistener_chain"/>

    </bean>

    <bean id="circuitbreaker_registry" class="org.telluriumsource.component.circuitbreaker.impl.spring.CircuitRegistryImpl">

    </bean>

    <bean id="eventlistener_chain" class="org.telluriumsource.component.event.impl.spring.EventListenerChainImpl">
        <property name="listeners">
            <list>
                <ref bean="eventlogger"/>
            </list>
        </property>
    </bean>

    <bean id="eventlogger" class="org.telluriumsource.component.event.impl.spring.EventLoggerImpl">
    </bean>

    <bean id="threadPool" class="org.telluriumsource.component.threadpool.impl.spring.ThreadPoolImpl"/>

    <bean id="circuitBreakerManager" class="org.telluriumsource.component.jmx.CircuitBreakerManagerMBean">

        <property name="circuitRegistry" ref="circuitbreaker_registry"/>
    </bean>

    <bean id="mbeanServer" class="org.springframework.jmx.support.MBeanServerFactoryBean"/>

    <bean id="exporter" class="org.springframework.jmx.export.MBeanExporter">
        <property name="beans">
            <map>
                <entry key="bean:name=testBean1" value-ref="circuitbreaker_registry"/>
                <entry key="bean:name=CircuitBreakerManager" value-ref="circuitBreakerManager"/>
            </map>
        </property>
        <property name="server" ref="mbeanServer"/>
    </bean>

</beans>
```

# Resources #

  * [John's Blogs](http://johnjianfang.blogspot.com/)
  * [John's twitter](http://twitter.com/johnjianfang)
