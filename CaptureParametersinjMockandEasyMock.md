# MVP #

I have a unit test written with jMock for a MVP framework. The presenter looks as follows:

```
public class MyPresenter {
    public MyPresenter(final IMyModel model, final IMyAdapter adapter) {
        // handling events between model and view
        adapter.whenScanned(new ScannedHandler() {
            public void canned() {
                String id = adapter.getScanned();
                model.retrieveShipment(id);
            }
        });

        model.whenRetrieved(new RetrievedHandler() {
            public void retrieve(Shipment shipment) {
                adapter.updateShipment(shipment);
            }
        });
    }
}
```

# jMock #

jMock uses an InstanceCatcher class to capture the handler passed in MyPresenter constructor.

```
public class InstanceCatcher<T> extends BaseMatcher<T> {
    private T instance;

    public T getInstance() {
        return instance;
    }

    public boolean matches(Object o) {
        try {
            instance = (T) o;
            return true;
        } catch (ClassCastException ex) {
            return false;
        }
    }
}
```

The magic is that jMock will call the matches method if you define the jMock
object as follows:

```
    protected JUnit4Mockery context = new JUnit4Mockery();
    InstanceCatcher<ScannedHandler> scannedHandlerCatcher = new InstanceCatcher<ScannedHandler>();
    InstanceCatcher<ScannedHandler> retrievedHandlerCatcher = new InstanceCatcher<RetrievedHandler>();

    context.checking(new Expectations() {
      {
     one(adapter).whenScanned(with(scannedHandlerCatcher));
     one(model).whenRetrieved(with(retrievedHandlerCatcher));
      }
    });

    new MyPresenter(model, adapter);

```

since scannedHandlerCatcher and retrievedHandlerCatcher capture the handlers during MyPresenter initialization, we can call on the handler in unit in the following way.

```
context.checking(new Expectations(){{
   one(adapter).getScanned(); will(returnValue(identifier));
   one(model).retrieve(identifier);
 }});
scannedHandlerCatcher.getInstance().identifierScanned();
```

# EasyMock #

For EasyMock, I need to use the Capture command to capture the handler instance.

```
ScannedHandler scannedHandler;
RetrievedHandler retrievedHandler;

model = EasyMock.createMock(IMyModel.class);
adapter = EasyMock.createMock(IMyAdapter.class);
        
Capture<ScannedHandler> ishCapture= new Capture<ScannedHandler>();
adapter.whenIdentifierScanned(EasyMock.and(EasyMock.capture(ishCapture), EasyMock.isA(ScannedHandler.class)));
EasyMock.expectLastCall().once();

Capture<RetrievedHandler> sorhCapture = new Capture<RetrievedHandler>();
model.whenRetrieved(EasyMock.and(EasyMock.capture(sorhCapture), EasyMock.isA(RetrievedHandler.class)));
EasyMock.expectLastCall().once();
EasyMock.replay(adapter, model);

new MyPresenter(model, adapter);
EasyMock.verify(adapter, model);
scannedHandler = ishCapture.getValue();
retrievedHandler = sorhCapture.getValue();
EasyMock.reset(adapter, model);
```

After that, I can call on the handlers directly.

```
EasyMock.expect(adapter.getScanned()).andReturn(identifier).once();
model.retrieveShipment(identifier);
EasyMock.expectLastCall().once();
EasyMock.replay(adapter, model);
scannedHandler.identifierScanned(); 

EasyMock.verify(adapter, model);
```

# Resources #

  * [Model-view-presenter (MVP)](http://en.wikipedia.org/wiki/Model-view-presenter)
  * [EasyMock](http://easymock.org/)
  * [EasyMock Tutorial](http://www.slideshare.net/subin123/easymock-tutorial)
  * [jMock](http://www.jmock.org/)