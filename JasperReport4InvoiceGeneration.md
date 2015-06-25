# Introduction #
JasperReports could be used to generate company Invoice from JasperReports template files and XML data source.

# JasperReports Templates #

[iReport](http://jasperforge.org/plugins/mwiki/index.php/Ireport) can be used to create Jasper template.

One interesting problem is how to embed image file in XML data source. I followed most of [this post](http://rgauss.com/2009/12/21/jasperreports-xml-datasource-with-inline-images/) and created the following sections in the template file.

<pre>
<variable name="slImageBytes" class="java.awt.Image"><br>
<variableExpression><![CDATA[ImageIO.read(new ByteArrayInputStream(new Base64().decodeBase64($F{slImage}.getBytes("UTF-8"))))]]><br>
<br>
Unknown end tag for </variableExpression><br>
<br>
<br>
<br>
<br>
Unknown end tag for </variable><br>
<br>
<br>
<br>
......<br>
<br>
<image><br>
<reportElement x="405" y="388" width="357" height="139"/><br>
<imageExpression class="java.awt.Image"><![CDATA[$V{slImageBytes}]]><br>
<br>
Unknown end tag for </imageExpression><br>
<br>
<br>
<br>
<br>
Unknown end tag for </image><br>
<br>
<br>
<br>
</pre>

The image can encoded in base64 format.

<pre>
<smartlabel><br>
<![CDATA[<br>
iVBORw0KGgoAAAANSUhEUgAAA6QAAAGdCAYAAAAFVb6RAAAC7mlDQ1BJQ0MgUHJvZmlsZQAAeAGF<br>
VM9rE0EU/jZuqdAiCFprDrJ4kCJJWatoRdQ2/RFiawzbH7ZFkGQzSdZuNuvuJrWliOTi0SreRe2h<br>
B/+AHnrwZC9KhVpFKN6rKGKhFy3xzW5MtqXqwM5+8943731vdt8ADXLSNPWABOQNx1KiEWlsfEJq<br>
/IgAjqIJQTQlVdvsTiQGQYNz+Xvn2HoPgVtWw3v7d7J3rZrStpoHhP1A4Eea2Sqw7xdxClkSAog8<br>
36Epx3QI3+PY8uyPOU55eMG1Dys9xFkifEA1Lc5/TbhTzSXTQINIOJT1cVI+nNeLlNcdB2luZsbI<br>
EL1PkKa7zO6rYqGcTvYOkL2d9H5Os94+wiHCCxmtP0a4jZ71jNU/4mHhpObEhj0cGDX0+GAVtxqp<br>
+DXCFF8QTSeiVHHZLg3xmK79VvJKgnCQOMpkYYBzWkhP10xu+LqHBX0m1xOv4ndWUeF5jxNn3tTd<br>
...<br>
]]><br>
<br>
<br>
Unknown end tag for </smartlabel><br>
<br>
<br>
</pre>

# Maven #

To create a Maven project for JasperReports, we need the following plugin in our pom.xml.

<pre>
<build><br>
<resources><br>
<resource><br>
<directory>src/main/resources<br>
<br>
Unknown end tag for </directory><br>
<br>
<br>
<br>
<br>
Unknown end tag for </resource><br>
<br>
<br>
<resource><br>
<directory>target/jasper<br>
<br>
Unknown end tag for </directory><br>
<br>
<br>
<br>
<br>
Unknown end tag for </resource><br>
<br>
<br>
<br>
<br>
Unknown end tag for </resources><br>
<br>
<br>
<br>
<plugins><br>
<plugin><br>
<groupId>org.apache.maven.plugins<br>
<br>
Unknown end tag for </groupId><br>
<br>
<br>
<artifactId>maven-compiler-plugin<br>
<br>
Unknown end tag for </artifactId><br>
<br>
<br>
<configuration><br>
<source>1.6<br>
<br>
Unknown end tag for </source><br>
<br>
<br>
<target>1.6<br>
<br>
Unknown end tag for </target><br>
<br>
<br>
<br>
<br>
Unknown end tag for </configuration><br>
<br>
<br>
<br>
<br>
Unknown end tag for </plugin><br>
<br>
<br>
<plugin><br>
<groupId>org.codehaus.mojo<br>
<br>
Unknown end tag for </groupId><br>
<br>
<br>
<artifactId>jasperreports-maven-plugin<br>
<br>
Unknown end tag for </artifactId><br>
<br>
<br>
<configuration><br>
<compiler>net.sf.jasperreports.compilers.JRGroovyCompiler<br>
<br>
Unknown end tag for </compiler><br>
<br>
<br>
<outputDirectory>${project.build.directory}/jasper<br>
<br>
Unknown end tag for </outputDirectory><br>
<br>
<br>
<br>
<br>
Unknown end tag for </configuration><br>
<br>
<br>
<executions><br>
<execution><br>
<goals><br>
<goal>compile-reports<br>
<br>
Unknown end tag for </goal><br>
<br>
<br>
<br>
<br>
Unknown end tag for </goals><br>
<br>
<br>
<br>
<br>
Unknown end tag for </execution><br>
<br>
<br>
<br>
<br>
Unknown end tag for </executions><br>
<br>
<br>
<dependencies><br>
<dependency><br>
<groupId>jasperreports<br>
<br>
Unknown end tag for </groupId><br>
<br>
<br>
<artifactId>jasperreports<br>
<br>
Unknown end tag for </artifactId><br>
<br>
<br>
<version>3.7.2<br>
<br>
Unknown end tag for </version><br>
<br>
<br>
<br>
<br>
Unknown end tag for </dependency><br>
<br>
<br>
<dependency><br>
<groupId>log4j<br>
<br>
Unknown end tag for </groupId><br>
<br>
<br>
<artifactId>log4j<br>
<br>
Unknown end tag for </artifactId><br>
<br>
<br>
<version>1.2.15<br>
<br>
Unknown end tag for </version><br>
<br>
<br>
<exclusions><br>
<exclusion><br>
<groupId>javax.mail<br>
<br>
Unknown end tag for </groupId><br>
<br>
<br>
<artifactId>mail<br>
<br>
Unknown end tag for </artifactId><br>
<br>
<br>
<br>
<br>
Unknown end tag for </exclusion><br>
<br>
<br>
<exclusion><br>
<groupId>javax.jms<br>
<br>
Unknown end tag for </groupId><br>
<br>
<br>
<artifactId>jms<br>
<br>
Unknown end tag for </artifactId><br>
<br>
<br>
<br>
<br>
Unknown end tag for </exclusion><br>
<br>
<br>
<exclusion><br>
<groupId>com.sun.jdmk<br>
<br>
Unknown end tag for </groupId><br>
<br>
<br>
<artifactId>jmxtools<br>
<br>
Unknown end tag for </artifactId><br>
<br>
<br>
<br>
<br>
Unknown end tag for </exclusion><br>
<br>
<br>
<exclusion><br>
<groupId>com.sun.jmx<br>
<br>
Unknown end tag for </groupId><br>
<br>
<br>
<artifactId>jmxri<br>
<br>
Unknown end tag for </artifactId><br>
<br>
<br>
<br>
<br>
Unknown end tag for </exclusion><br>
<br>
<br>
<br>
<br>
Unknown end tag for </exclusions><br>
<br>
<br>
<br>
<br>
Unknown end tag for </dependency><br>
<br>
<br>
<dependency><br>
<groupId>org.codehaus.groovy<br>
<br>
Unknown end tag for </groupId><br>
<br>
<br>
<artifactId>groovy-all<br>
<br>
Unknown end tag for </artifactId><br>
<br>
<br>
<version>1.7.2<br>
<br>
Unknown end tag for </version><br>
<br>
<br>
<br>
<br>
Unknown end tag for </dependency><br>
<br>
<br>
<br>
<br>
Unknown end tag for </dependencies><br>
<br>
<br>
<br>
<br>
Unknown end tag for </plugin><br>
<br>
<br>
<br>
<br>
Unknown end tag for </plugins><br>
<br>
<br>
<br>
<br>
<br>
Unknown end tag for </build><br>
<br>
<br>
<br>
</pre>

# Invoice Component #

I created an Invoice component project so that it can be packaged as a jar file for people to use.

The API is defined as follows.

<pre>
public interface InvoiceService {<br>
<br>
void generateInvoice(String id, ReportType type, String inputFile);<br>
void generateInvoice(String id, ReportType type, Document inputDoc);<br>
}<br>
</pre>

where ReportType is the type of different invoice templates, for example.

<pre>
public enum ReportType {<br>
REGULAR,<br>
GIFT,<br>
SPECIAL<br>
}<br>
</pre>


The implementation class is InvoiceServiceImpl.

<pre>
public class InvoiceServiceImpl implements InvoiceService {<br>
<br>
private String inputFileDir;<br>
<br>
private String outputFileDir;<br>
<br>
private int poolSize;<br>
<br>
private Map<String, String> typeMapping;<br>
<br>
private Map<ReportType, JasperReportPool> pool;<br>
<br>
public InvoiceServiceImpl(String inputFileDir, String outputFileDir, int poolSize, Map<String, String> typeMapping) {<br>
this.inputFileDir = inputFileDir;<br>
this.outputFileDir = outputFileDir;<br>
this.poolSize = poolSize;<br>
this.typeMapping = typeMapping;<br>
this.pool = new HashMap<ReportType, JasperReportPool>();<br>
createJasperReportPool();<br>
}<br>
......<br>
<br>
<br>
public void generateInvoice(String id, ReportType type, String inputFile) {<br>
try{<br>
Document document= JRXmlUtils.parse(JRLoader.getLocationInputStream(inputFile));<br>
this.generateInvoice(id, type, document);<br>
} catch (JRException e) {<br>
throw new InvoiceGenerationException(e);<br>
}<br>
}<br>
<br>
public void generateInvoice(String id, ReportType type, Document document) {<br>
String threadName = Thread.currentThread().getName();<br>
log.debug(threadName + " -> Generating invoice for id: " + id + ", type: " + type );<br>
JasperReportPool jrPool = pool.get(type);<br>
if(jrPool == null){<br>
log.error("Cannot find JasperReport pool for type " + type);<br>
throw new InvoiceGenerationException("Cannot find JasperReport pool for type " + type);<br>
}<br>
<br>
try {<br>
Map params = new HashMap();<br>
params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);<br>
params.put(JRXPathQueryExecuterFactory.XML_LOCALE, Locale.ENGLISH);<br>
params.put(JRParameter.REPORT_LOCALE, Locale.US);<br>
<br>
//get a pooled JasperReport object<br>
JasperReport report = jrPool.getFromPool();<br>
<br>
if(report == null){<br>
log.warn(threadName + " -> Cannot find a polled JasperReport object, have to create one from scratch");<br>
//cannot find a pooled JasperReport object, have to create one from scratch<br>
String template = inputFileDir + "/" + typeMapping.get(type.toString());<br>
JasperDesign jasperDesign = JRXmlLoader.load(template);<br>
report = JasperCompileManager.compileReport(jasperDesign);<br>
}else{<br>
log.debug(threadName + " -> Found a pooled JasperReport object");<br>
}<br>
<br>
log.debug(threadName + " -> Filling report...");<br>
JasperPrint jasperPrint = JasperFillManager.fillReport(report, params);<br>
log.debug(threadName + " -> Exporting to pdf file " + id + ".pdf");<br>
JasperExportManager.exportReportToPdfFile(jasperPrint, outputFileDir + "/" + id + ".pdf");<br>
//add the report back to the pool<br>
boolean succeeded = jrPool.addToPool(report);<br>
if(succeeded){<br>
log.debug(threadName + " -> Adding JasperReport object back to pool");<br>
}else{<br>
log.debug(threadName + " -> Pool is full, discard JasperReport object");<br>
}<br>
<br>
<br>
} catch (JRException e) {<br>
log.error(threadName + " -> Error generating invoice: " + e.getMessage());<br>
throw new InvoiceGenerationException(e);<br>
}<br>
}<br>
}<br>
</pre>

As you can see, I used a pool to cache a predefined number of JasperReport objects for each invoice template. In this way, we can improve the speed for multiple thread processing.

The JasperReportPool is pretty straightforward and it is a current queue.

<pre>
public class JasperReportPool {<br>
<br>
private int max;<br>
<br>
private ConcurrentLinkedQueue<JasperReport> pool;<br>
<br>
......<br>
<br>
public synchronized boolean addToPool(JasperReport report){<br>
if(pool.size() < max){<br>
pool.add(report);<br>
<br>
return true;<br>
}<br>
<br>
return false;<br>
}<br>
<br>
public JasperReport getFromPool(){<br>
return pool.poll();<br>
}<br>
}<br>
</pre>

# Testing #

To test the Invoice component, I used TestNG to run the test with multiple threads.

<pre>

public class InvoiceImpl_FuncTest {<br>
<br>
private static InvoiceService invoiceService;<br>
private static InvoiceService invoiceServiceStress;<br>
private static AtomicInteger seed = new AtomicInteger(1000);<br>
private static long start;<br>
private static long end;<br>
<br>
@BeforeClass<br>
public static void setUp(){<br>
String currentDir = new File(".").getAbsolutePath();<br>
Map<String, String> typeMapping = new HashMap<String, String>();<br>
typeMapping.put("TE", "half-pg-landscape.jrxml");<br>
<br>
invoiceService = new InvoiceServiceImpl(currentDir + "/src/main/jasperreports", currentDir + "/target/jasper", 5, typeMapping);<br>
<br>
<br>
@AfterClass<br>
public void end(){<br>
end = System.nanoTime();<br>
System.out.println("Test took " + ((end - start) / 1E6) + " milliseconds" );<br>
}<br>
<br>
private static String getId(){<br>
return "Test-" + seed.getAndIncrement();<br>
}<br>
<br>
@Test(threadPoolSize = 5, invocationCount = 10)<br>
public void testBounded() {<br>
String currentDir = new File(".").getAbsolutePath();<br>
String inputFile = currentDir + "/target/test-classes/com/jtv/invoice/impl/order_half_pg.xml";<br>
invoiceService.generateInvoice(getId(), ReportType.REGULAR, inputFile);<br>
}<br>
<br>
......<br>
}<br>
</pre>