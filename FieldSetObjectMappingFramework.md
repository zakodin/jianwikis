# Introduction #

## Motivations ##

The Field Set Object Mapping Framework (FSOM) is created to convert Java Object to and from field sets. What are field sets? A field set is
a set of data to represent the format of a flat file or other types of files. As a result, the direct use of the Field Set Object Mapping Framework
is to convert Java object to or from flat files. This is very useful in batch processing. Actually, the FSOM was first designed for the extended warranty batch
processing to convert our warranty sales to a pipe delimited format for our vendor NEW.

Why we need the FSOM framework? I worked on couple batch processing projects, the first one was payment processing and we had to create many converters
for different data records in that project, which were cumbersome and not flexible. As a result, I was hit by the idea: why we cannot create a
general framework to do the job more efficiently?

Another motivation behind the invention of the FSOM framework is iBatis. I am very impressed by its capability to do mapping from Java object to SQL parameters
only using XML configurations and the expressiveness it offers. As a result, you would find some similarities between the FSOM framework and iBatis.

## Architecture ##

The Architecture of the FSOM framework is shown in the following diagram,

http://sacct-users.googlegroups.com/web/FsomArchitecture.png?gda=myxegUcAAABwsiXcufpgHYK0Fq-t18-VcduyJajMZkHyExt4IRNTJkxBcav97KKqIIHPw8_lwXUVeY4b49xGcMK802iZZ8SFeV4duv6pDMGhhhZdjQlNAw&gsc=ypY6FgsAAADV3ZfoSfo7zlUr2Cv4XdfJ

The main task of the FSOM framework is to convert Java object to a file, whose format is specified by the Field Set definition, or read the file and convert
it to Java objects automatically.

The field reader is used to read the data from the file and the field writer is to write data into the file. FSOM will automatically convert Primitive types into
appropriate Java object types. Moreover, the FSOM framework also provides field validation handlers and type handlers for you to add your custom code for
special fields. Once data are in memory, the FSOM framework will automatically do the Field Set and Java Object mapping for you using Java reflection.

One biggest advantage of the FSOM framework is that you can define your file format and to which Java objects you want to convert the file by using XML configurations
in the same way as iBatis so that you can have the flexible to process any formats of files without writing specific code for each individual format. To achieve this goal,
the FSOM provides XML configuration parser to read your XML configurations.

Since we are using the Spring framework, a Spring bean factory is also provided for you to wiring in the framework very easily.

## Components ##

The main classes for FSOM are listed as follows,

### Data Field ###

A data field is used to describe the format of one data column and the Java class is as follows

```
public class DataField {
    //Field name
    private String name;

    //optional description of the Field
    private String description;

    //If the value can be null, default is true
    private boolean nullable = true;

    //optional null value if the value is null or not specified
    private String nullValue;

    //If the length is not specified, it is -1
    private int length = -1;

    //optional String pattern for the value
    //if specified, we must use it for String validation
    private String pattern;

    //optional custom field validator
    //call back handler to call custom validation for the data field value
    private FieldValidator validator;
}
```

### Field Set ###

A field set includes multiple data fields and it could be a record or subset of a record.

```
public class FieldSet {

    private String id;

    private String description;

    private List<DataField> fields;
```

### Object Property ###

Object property describes the Java object properties,

```
public class ObjectProperty {
    //the name of Java object field
    private String objectField;

    //the name of data field
    private String fieldName;

    //optional type handler, users can define their own custom type handlers
    private TypeHandler typeHandler;
}
```

### ObjectFieldMap ###

Map Java object fields to data fields,

```
public class ObjectFieldMap {

    private String id;

    private Class clazz;

    private List<ObjectProperty> properties;
}
```

### FieldValidator ###

Call back handler to validate the data field value

```
public interface FieldValidator {

    public boolean validate(String value);

}
```

### TypeHandler ###

Type handler, users can it to define custom type conversion during the object mapping.

```
public interface TypeHandler {

    //convert a string input to a java type object
    public Object valueOf(String s);

    //reverse process, convert a java object to a String format
    public String toString(Object object);
}
```

### FieldSetReader ###

Read field set from a stream, i.e., a file or a byte array,

```
public interface FieldSetReader {

    public Map<String, String> readFieldSet(FieldSet fieldSet, BufferedReader reader);

}
```

You can create your own FieldSetReader based on your file format. By default, the FSOM provides FieldSetReaders for pipe delimited format and CSV format.

### FieldSetWriter ###

Write the field set to a file or a byte stream,

```
public interface FieldSetWriter {

    public boolean writeFieldSet(FieldSet fieldSet, OutputStream outputStream, Map<String, String> dataFields);

}
```

You can create your own FieldSetWriter based on your file format. By default, the FSOM provides FieldSetWriters for pipe delimited format and CSV format.

### FieldSetObjectMapConfigParser ###

Used to parse the root Field set object map configuration file, i.e., the file with the tag ```
fsoMapConfig```, which
will load different mapping XML files specified by the resource tag.

### FieldSetObjectMapper ###

The Interface for the field set object mapping and its role is the same as SqlMapClient in ibatis.

```
public interface FieldSetObjectMapper {

    /**
     * @param outputStream The stream the converted byte stream should be written to
     * @param nameSpace    name space actually specifies which FSO map file you are using.
     *                     Each FSO map comes with a name space
     * @param fieldSetId   The field set id specified in the FSO map file, which you want the objects convert to
     * @param objectMaps   It includes the object map ids specified in the FSO map file and the corresponding
     *                     Java object you pass in.
     */
    public void marshalFieldSet(OutputStream outputStream, String nameSpace, String fieldSetId,
                                Map<String, Object> objectMaps);

    /**
     * @param inputReader The input reader, from which the mapper to read data
     * @param nameSpace   name space actually specifies which FSO map file you are using
     * @param fieldSetId  The field set id specified in the FSO map file, which you want the objects convert to
     * @return objectMaps  It includes the object map ids specified in the FSO map file and the corresponding
     *         Java object
     */
    public Map<String, Object> unmarshalFieldSet(BufferedReader inputReader, String nameSpace, String fieldSetId);

}
```

### FieldSetObjectMapperFactoryBean ###

Create Spring bean from FSOM Configuration file.

# How to Use #

We will take the demo project as an example to illustrate how to use the FSOM framework.

## Configuration ##

### FsoMapConfig.xml ###

The FSOM configuration files include a main configuration file called FsoMapConfig.xml, which is the root configuration file and includes
all detailed FieldSet Object Mapping configuration files. For example, for demo, this file is defined as follows,

```
<?xml version="1.0" encoding="UTF-8"?>

<fsoMapConfig>
	
	<fsoMap resource="com/jtv/batch/config/data/fsom/FsoDemo.xml"/>

</fsoMapConfig>
```

### demo Data Mapping Configuration ###

The detailed Java Object and Field Set mapping XML configuration file is shown as follows,

```
<?xml version="1.0" encoding="UTF-8"?>
<fsoMap namespace="demo">

  	<typeHandler id="simpleDateHandler" class="com.mycompany.batch.mapping.impl.spring.type.SimpleDateTypeHandler"/>
	<typeHandler id="simpleDateTimeHandler" class="com.mycompany.batch.mapping.impl.spring.type.SimpleDateTimeTypeHandler"/>
	<typeHandler id="dollarAmountHandler" class="com.mycompany.batch.mapping.impl.spring.type.DollarAmountTypeHandler"/>
	<typeHandler id="usPhoneTypeHandler" class="com.mycompany.batch.mapping.impl.spring.type.USPhoneFormatTypeHandler"/>

	<fieldValidator id="emailValidator" class="com.mycompany.batch.mapping.impl.spring.validation.EmailValidator"/>

    <fieldSet id="clientRecord" objectMap="batchInfo customerInfo accountInfo transactionInfo chargeBackInfo">
        <field name="batchId" description="Batch ID" nullable="false"/>
        <field name="lastName" description="Last Name" nullable="false" length="20"/>
        <field name="firstName" description="FirstName Mi" nullable="false" length="20"/>
        <field name="addressOne" description="Address1" nullable="false" length="20"/>
        <field name="addressTwo" description="Address2" nullable="true" length="20"/>
        <field name="city" description="City" nullable="false" length="20"/>
        <field name="state" description="State" nullable="false" length="2"/>
        <field name="zip" description="Zip" nullable="false" length="9" pattern="[0-9]{5,9}"/>
        <field name="homePhone" description="Home Phone"/>
        <field name="workPhone" description="Work Phone"/>
        <field name="otherPhone" description="Other Phone"/>
        <field name="email" description="Email"/>
        <field name="driverLicense" description="Driver's License"/>
        <field name="ssn" description="SSN" length="9"/>
        <field name="amount" description="Amount of Check"/>
        <field name="accountNo" description="Bank Account Number"/>
        <field name="routingNo" description="Bank Routing Number"/>
        <field name="sevDate" description="Service Date"/>
        <field name="checkNo" description="CheckNumber"/>
        <field name="payee" description="Payee" length="20"/>
        <field name="returnDate" description="ChargeBack Date"/>
    </fieldSet>

    <objectMap id="batchInfo" class="com.mycompany.batch.entity.transfer.demo.BatchInfo">
        <variable name="batchId" field="batchId"/>
    </objectMap>

    <objectMap id="customerInfo" class="com.mycompany.batch.entity.transfer.demo.CustomerInfo">
        <variable name="firstName" field="firstName"/>
        <variable name="lastName" field="lastName"/>
        <variable name="addressLineOne" field="addressOne"/>
        <variable name="addressLineTwo" field="addressTwo"/>
        <variable name="city" field="city"/>
        <variable name="state" field="state"/>
        <variable name="zip" field="zip"/>
        <variable name="homePhone" field="homePhone" typeHandler="usPhoneTypeHandler"/>
        <variable name="workPhone" field="workPhone" typeHandler="usPhoneTypeHandler"/>
        <variable name="otherPhone" field="otherPhone" typeHandler="usPhoneTypeHandler"/>
        <variable name="email" field="email"/>
        <variable name="driverLicense" field="driverLicense"/>
        <variable name="ssn" field="ssn"/>
    </objectMap>

    <objectMap id="accountInfo" class="com.mycompany.batch.entity.transfer.demo.AccountInfo">
        <variable name="accountNo" field="accountNo"/>
        <variable name="routingNo" field="routingNo"/>
    </objectMap>

    <objectMap id="transactionInfo" class="com.mycompany.batch.entity.transfer.demo.TransactionInfo">
        <variable name="payee" field="payee"/>
        <variable name="checkNo" field="checkNo"/>
        <variable name="amountInCents" field="amount" typeHandler="dollarAmountHandler"/>
        <variable name="sevDate" field="sevDate" typeHandler="simpleDateHandler"/>
    </objectMap>

    <objectMap id="chargeBackInfo" class="com.mycompany.batch.entity.transfer.demo.ChargeBackInfo">
        <variable name="returnDate" field="returnDate" typeHandler="simpleDateHandler"/>
    </objectMap>
 
</fsoMap>
```

## Use FSOM as a Converter ##

In demo project, we use the FSOM framework to convert Java object to CSV format flat file. The Java code for the
converter is very simple as illustrated by the following code snippet,

```
public class demoConverterImpl  extends BaseConverterImpl implements demoConverter, InitializingBean {
    private FieldSetObjectMapper mapper;

    protected void convertBadCheckRecord(BatchJobContext context, demoRequest request,
			ByteArrayOutputStream baos, ClientRecord clientRecord){

	   	Map<String, Object> objectMaps = new HashMap<String, Object>();
	   	objectMaps.put("batchInfo", clientRecord.getBatchInfo());
	   	objectMaps.put("customerInfo", clientRecord.getCustomerInfo());
	   	objectMaps.put("accountInfo", clientRecord.getAccountInfo());
	   	objectMaps.put("transactionInfo", clientRecord.getTransactionInfo());
	   	objectMaps.put("chargeBackInfo", clientRecord.getChargeBackInfo());
	   	//temporally hold the byte array for the record, so that we can discard it in case
	   	//of invalid data and only add valid records to the converted data stream
	   	ByteArrayOutputStream tbaos = new ByteArrayOutputStream();
    	try{
    		//convert the Client Bad Check Record field set
    		mapper.marshalFieldSet(tbaos, "demo", "clientRecord", objectMaps);
    	}catch(CoreException e){
                ...
    	}
    }
}
```

## Spring Wiring ##

The Spring wiring file uses the FieldSetObjectMapperFactoryBean class to create the Field Set and Object mapping bean,

```
  <bean id="fsoBeanFactory" class="com.mycompany.batch.mapping.impl.spring.mapper.FieldSetObjectMapperFactoryBean">
	<property name="configLocation" value="classpath:com/jtv/batch/config/data/fsom/FsoMapConfig.xml"/>
        <property name="reader" ref="csvFieldSetReader"/>
        <property name="writer" ref="csvFieldSetWriter"/>
  </bean>

  <bean id="csvFieldSetReader" class="com.mycompany.batch.mapping.impl.spring.io.CsvFieldSetReader"/>
  <bean id="csvFieldSetWriter" class="com.mycompany.batch.mapping.impl.spring.io.CsvFieldSetWriter"/>

  <bean id="demo-converter" parent="base-converter"
	class="com.mycompany.batch.converter.impl.spring.demo.demoConverterImpl">
     	<property name="mapper" ref="fsoBeanFactory"/>
  </bean>
```

If you have different file formats, you can simply write your custom Field Set reader and writer and then wire them into the factory bean.