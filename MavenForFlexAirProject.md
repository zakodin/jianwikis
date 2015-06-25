

# Introduction #

I worked on an [AIR](http://www.adobe.com/products/air/develop/flex/) project with [Pure MVC](http://puremvc.org/), [Spring ActionScript](http://www.springsource.org/extensions/se-springactionscript-as), and [Flex Unit 4](http://opensource.adobe.com/wiki/display/flexunit/FlexUnit+4+feature+overview) recently, which was not a very pleasant experience. Many Flex projects use ant for build and the Air project is even more difficult to maventize. I post my experience on the Adobe AIR Maven project and hope it could some people's time.

# Prerequisites #

  * _Flex SDK 3_: Download Flex 3 SDK from [Adobe site](http://opensource.adobe.com/wiki/display/flexsdk/Download+Flex+3). Unpack it and then set up the FLEX\_HOME environment variable.

  * _Flex AIR_: Downloaded the AIR from [Adobe AIR site](http://get.adobe.com/air/) and install it. Windows installation is easy and more details of Linux installation could be found from my previous post [Install Adobe AIR, BlazDS, and Blaze Monster on Fedora 11](http://code.google.com/p/jianwikis/wiki/InstallAIRonFedora11#Adobe_Air).

  * _Maven_: Download Maven from [Maven site](http://maven.apache.org/download.html). If you never installed Maven before, please follow [the official Maven Installation Guide](http://maven.apache.org/download.html#Installation).

  * _Proxy Server_: If you work behind a firewall like me, you can download [NTLM Authorization Proxy Server](http://ntlmaps.sourceforge.net/) and use it as a proxy server. You may need to configure your `HOME/.m2/settings.xml` as follows,

```
  <proxies>
   <proxy>
      <active>true</active>
      <protocol>http</protocol>
      <host>localhost</host>
      <port>5865</port>
      <!--username>proxyuser</username>
      <password>somepassword</password-->
      <nonProxyHosts>*.mycompany.com|*.othersites_do_not_need_proxy</nonProxyHosts>
    </proxy>
  </proxies>
```

# Flex AIR Maven Project #

## Sample Project ##

I use the sample project which uses Spring ActionScript, Pure MVC, Flex AIR, and Flex Unit 4. The project name is "AirSample".

## Create A Maven Project ##

You can use Maven archetype to create a skeleton project, for example,

```
mvn archetype:generate
```

then choose maven-archetype-quickstart or manually create a Maven project. After manually set up some directory, the project structure looks as follows,

```
[jfang@Mars AirSample]$ tree .
.
|-- pom.xml
|-- src
|   |-- main
|   |   |-- flex
|   |   |   |-- AirSample-app.xml
|   |   |   |-- AirSample.mxml
|   |   |   |-- LinkageEnforcer.as
|   |   |   |-- TestRunner-app.xml
|   |   |   |-- TestRunner.mxml
|   |   `-- resources
|   |       |-- AirSample-app.xml
|   |       |-- applicationContext.xml
|   |       |-- assets
|   |       |   `-- styles
|   |       |       `-- appStyle.css
|   |       |-- cert.p12
|   |       `-- configurationClassesCompilerConfig.xml
|   `-- test
|       |-- flex
|       `-- resources
|           `-- TestRunner-app.xml
|           `-- testApplicationContext.xml
```

Where the AirSample-app.xml is created for Flash Builder 3 and it includes the following meta data,

```
<!-- The main SWF or HTML file of the application. Required. -->
<!-- Note: In Flex Builder, the SWF reference is set automatically. -->
<content>[This value will be overwritten by Flex Builder in the output app.xml]</content>
```

That is why we need another copy of the AirSample-app.xml in the resources directory with the content as follows,

```
<content>AirSample.swf</content>
```

Of course, you can use [the Maven antrun plugin](http://maven.apache.org/plugins/maven-antrun-plugin/) to replace the meta data in the AirSample-app.xml file as follows,

```
<copy file=”${basedir}/src/main/flex/${APP_NAME}-app.xml” todir=”${basedir}/target/classes” overwrite=”true” />
<replace file=”${basedir}/target/classes/${APP_NAME}-app.xml”
token=”[This value will be overwritten by Flex Builder in the output app.xml]” value=”${APP_NAME}.swf” />
```

For simplicity, we simple keep another copy of the AirSample-app.xml in the resources directory for Maven build since this file is unlikely to change. Similarly, we have two TestRunner-app.xml files for Flex Unit 4, which will be covered later.

The AirSample.mxml is the AIR application main file and it usually comes with the following XML name spaces,

```
<?xml version="1.0" encoding="utf-8"?>
<mx:WindowedApplication xmlns:mx="http://www.adobe.com/2006/mxml" 
	xmlns:components="com.mycompany.view.components.*" frameRate="7"
	layout="vertical" applicationComplete="onCreationComplete();" width="100%" height="100%">
	<mx:Script>
		<![CDATA[

			private function onCreationComplete():void
			{
			    facade = ApplicationFacade.getInstance(["applicationContext.xml"]);
				facade.addEventListener(Event.COMPLETE, startupApp);
				facade.initializeIocContainer(null);
				......
			}
                        ......
             ]]>
	</mx:Script>	
</mx:WindowedApplication>
```

where applicationContext.xml is the Spring ActionScript wiring file. configurationClassesCompilerConfig.xml is a custom compilation configuration file and cert.p12 is a cert file to sign the AIR package.

## Maven POM ##

### Repositories ###

To build an Air application with Spring ActionScript, Pure MVC, and Flex Unit 4, we need to use the following Maven repositories:

```
        <repository>
            <id>flexmojos-repository</id>
            <url>http://repository.sonatype.org/content/groups/public/</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
        
        <repository>
	    <id>ObjectWEB</id>
	    <url>http://maven.ow2.org/maven2/</url>
	    <releases>
		<enabled>true</enabled>
	    </releases>
            <snapshots>
		<enabled>false</enabled>
	    </snapshots>
	</repository>

        <repository>
            <id>yoolab.org-releases</id>
            <url>http://projects.yoolab.org/maven/content/repositories/releases</url>
            <releases>
                <enabled>true</enabled>
            </releases>
        </repository>
        <repository>
            <id>yoolab.org-snapshots</id>
            <url>http://projects.yoolab.org/maven/content/repositories/snapshots</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
        <repository>
            <id>kungfuters-thirdparty-releases-repo</id>
            <name>Kungfuters.org Third Party Releases Repository</name>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <url>http://kungfuters.org/nexus/content/repositories/thirdparty</url>
        </repository>
```

You may also need to use your local repository to upload some Flex dependencies since some of them could not be found on any Maven repository. For example, we have a local repository:

```
        <repository>
            <id>mycompany-thirdparty-repo</id>
            <name>Mycompany Third Party Releases Repository</name>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <url>http://nexus.mycompany.com/nexus/content/repositories/thirdparty</url>
        </repository
```

Then, I can use the following Maven command to upload a jar artifact to the local Maven repository

```
mvn deploy:deploy-file -Dfile=./FlexUnit4CIListener.swc -Durl=http://nexus.mycompany.com/nexus/content/repositories/thirdparty -DgroupId=com.adobe.flexunit -DartifactId=cilistener -Dversion=4.0-beta-2 -Dpackaging=swc -DrepositoryId=mycompany-thirdparty-repo
```

To make it work, you need to set up your server upload user account in your Maven settings.xml, for example,

```
      <server>
           <id>mycompany-thirdparty-repo</id>
           <username>deployment</username>
           <password>mypassword</password>
      </server>  
```

### Maven dependencies ###

Our application depends on Spring ActionScript, Pure MVC, ActionScript 3, and Flex AIR sdk. The Maven dependencies are shown as follows,

```
        <dependency>
            <groupId>org.springextensions.actionscript</groupId>
            <artifactId>spring-actionscript-core</artifactId>
            <version>0.8</version>
            <type>swc</type>
            <exclusions>
                <exclusion>
                    <groupId>com.adobe.flex.framework</groupId>
                    <artifactId>playerglobal</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springextensions.actionscript</groupId>
            <artifactId>spring-actionscript-puremvc-standard</artifactId>
            <version>0.8</version>
            <type>swc</type>
             <exclusions>
                <exclusion>
                    <groupId>com.adobe.flex.framework</groupId>
                    <artifactId>playerglobal</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.puremvc</groupId>
            <artifactId>puremvc-as3-standard</artifactId>
            <version>2.0.4</version>
            <type>swc</type>
        </dependency>
        <dependency>
            <groupId>org.as3commons</groupId>
            <artifactId>as3commons-lang</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <scope>merged</scope>
            <type>swc</type>
            <exclusions>
                <exclusion>
                    <groupId>com.adobe.flex.framework</groupId>
                    <artifactId>playerglobal</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.as3commons</groupId>
            <artifactId>as3commons-logging</artifactId>
            <version>1.0.0</version>
            <scope>merged</scope>
            <type>swc</type>
            <exclusions>
                <exclusion>
                    <groupId>com.adobe.flexunit</groupId>
                    <artifactId>flexunit</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.as3commons</groupId>
            <artifactId>as3commons-reflect</artifactId>
            <version>1.0.0</version>
            <scope>merged</scope>
            <exclusions>
                <exclusion>
                    <groupId>com.adobe.flexunit</groupId>
                    <artifactId>flexunit</artifactId>
                </exclusion>
            </exclusions>           
            <type>swc</type>
        </dependency>
        <dependency>
            <groupId>com.adobe.flex.framework</groupId>
            <artifactId>air-framework</artifactId>
            <version>${flex-sdk.version}</version>
            <type>pom</type>
        </dependency>
        <dependency>
            <groupId>flexlib</groupId>
            <artifactId>flexlib-bin</artifactId>
            <version>2.4</version>
            <type>swc</type>
        </dependency>
```

Obviously, I excluded couple transitive dependencies such as _playerglobal_ and _FlexUnit 0.9_. For AIR application, you must exclude _playerglobal_ and use _airglobal_, otherwise, you will get the following error:

```
[-1,-1] Type was not found or was not a compile-time constant: NativeWindow.
```

Be aware that the _air-framework_ dependency is a POM file, which includes all the dependencies you need for an AIR application. For example, the _air-framework_ POM file for Flex sdk 3.3.0.4852 looks like:

```
<?xml version="1.0" encoding="UTF-8"?><project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.adobe.flex.framework</groupId>
  <artifactId>air-framework</artifactId>
  <packaging>pom</packaging>
  <version>3.3.0.4852</version>
  <description>POM was created from flex-mojos:install-sdk</description>
  <dependencies>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>airframework</artifactId>
      <version>3.3.0.4852</version>
      <type>swc</type>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>airglobal</artifactId>
      <version>3.3.0.4852</version>
      <type>swc</type>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>applicationupdater</artifactId>
      <version>3.3.0.4852</version>
      <type>swc</type>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>applicationupdater_ui</artifactId>
      <version>3.3.0.4852</version>
      <type>swc</type>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>servicemonitor</artifactId>
      <version>3.3.0.4852</version>
      <type>swc</type>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>flex</artifactId>
      <version>3.3.0.4852</version>
      <type>swc</type>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>framework</artifactId>
      <version>3.3.0.4852</version>
      <type>swc</type>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>rpc</artifactId>
      <version>3.3.0.4852</version>
      <type>swc</type>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>utilities</artifactId>
      <version>3.3.0.4852</version>
      <type>swc</type>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>airframework</artifactId>
      <version>3.3.0.4852</version>
      <type>rb.swc</type>
      <classifier></classifier>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>rpc</artifactId>
      <version>3.3.0.4852</version>
      <type>rb.swc</type>
      <classifier></classifier>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>framework</artifactId>
      <version>3.3.0.4852</version>
      <type>rb.swc</type>
      <classifier></classifier>
    </dependency>
    <dependency>
      <groupId>com.adobe.flex.framework</groupId>
      <artifactId>framework</artifactId>
      <version>3.3.0.4852</version>
      <type>zip</type>
      <classifier>configs</classifier>
    </dependency>
  </dependencies>
</project>
```

Apart from that, the Flex Unit 4 dependencies are listed here:

```
        <dependency>
            <groupId>com.adobe.flexunit</groupId>
            <artifactId>flexunit</artifactId>
            <version>${flexunit.version}</version>
            <type>swc</type>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.adobe.flexunit</groupId>
            <artifactId>uirunner</artifactId>
            <version>${flexunit.version}</version>
            <type>swc</type>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.adobe.flexunit</groupId>
            <artifactId>cilistener</artifactId>
            <version>${flexunit.version}</version>
            <type>swc</type>
            <scope>test</scope>
        </dependency>
```

where the uirunner is used for Flex Unit UI and cilistener is a listener to the FlexUnitCore so that it knows when the tests are done and it is mainly for CI integration.

Apart from the above, you also need the _adt_ tool to package the AIR application, which only exists in Flex sdk. As a result, I use the jar from Flex sdk directly,

```
        <dependency>
            <groupId>com.adobe.flex.compiler</groupId>
            <artifactId>adt</artifactId>            
            <version>${flex-sdk.version}</version>
            <scope>system</scope>
            <systemPath>${flex.home}/lib/adt.jar</systemPath>
        </dependency>
```

You can put `flex.home` as a property in the POM file, but I would rather to put it in the Maven settings.xml because different operating systems have different file paths.

Finally, you can check your project dependencies using the following Maven command:

```
mvn dependency:tree
```

For example, my project dependency tree is shown as follows,

```
[INFO] [dependency:tree]
[INFO] com.mycompany:AirSample:swf:1.0-SNAPSHOT
[INFO] +- org.springextensions.actionscript:spring-actionscript-core:swc:0.8:compile
[INFO] +- org.springextensions.actionscript:spring-actionscript-puremvc-standard:swc:0.8:compile
[INFO] +- org.puremvc:puremvc-as3-standard:swc:2.0.4:compile
[INFO] +- org.as3commons:as3commons-lang:swc:1.0.0-SNAPSHOT:merged
[INFO] |  \- com.adobe.flex.framework:flex-framework:pom:3.2.0.3958:runtime
[INFO] +- org.as3commons:as3commons-logging:swc:1.0.0:merged
[INFO] +- org.as3commons:as3commons-reflect:swc:1.0.0:merged
[INFO] +- com.adobe.flex.framework:air-framework:pom:3.3.0.4852:compile
[INFO] |  +- com.adobe.flex.framework:airframework:swc:3.3.0.4852:compile
[INFO] |  +- com.adobe.flex.framework:airglobal:swc:3.3.0.4852:compile
[INFO] |  +- com.adobe.flex.framework:applicationupdater:swc:3.3.0.4852:compile
[INFO] |  +- com.adobe.flex.framework:applicationupdater_ui:swc:3.3.0.4852:compile
[INFO] |  +- com.adobe.flex.framework:servicemonitor:swc:3.3.0.4852:compile
[INFO] |  +- com.adobe.flex.framework:flex:swc:3.2.0.3958:compile
[INFO] |  +- com.adobe.flex.framework:framework:swc:3.2.0.3958:compile
[INFO] |  +- com.adobe.flex.framework:rpc:swc:3.2.0.3958:compile
[INFO] |  +- com.adobe.flex.framework:utilities:swc:3.3.0.4852:compile
[INFO] |  +- com.adobe.flex.framework:airframework:rb.swc:3.3.0.4852:compile
[INFO] |  +- com.adobe.flex.framework:rpc:rb.swc:3.2.0.3958:compile
[INFO] |  +- com.adobe.flex.framework:framework:rb.swc:3.2.0.3958:compile
[INFO] |  \- com.adobe.flex.framework:framework:zip:configs:3.3.0.4852:compile
[INFO] +- flexlib:flexlib-bin:swc:2.4:compile
[INFO] +- com.adobe.flex.compiler:adt:jar:3.3.0.4852:system
[INFO] +- org.sonatype.flexmojos:flexmojos-unittest-support:swc:3.3.0:test
[INFO] |  +- org.funit:funit:swc:0.50.0245:test
[INFO] |  +- com.swirlyvision:swirly-vision:swc:1.0:test
[INFO] |  +- net.digitalprimates:fluint:swc:v1:test
[INFO] |  +- com.asunit:asunit:swc:20071011:test
[INFO] |  \- advancedflex:debugger:swc:0.2alpha2:test
[INFO] +- com.adobe.flexunit:flexunit:swc:4.0-beta-2:test (scope not updated to runtime)
[INFO] +- com.adobe.flexunit:uirunner:swc:4.0-beta-2:test
[INFO] +- ant-contrib:ant-contrib:jar:1.0b2:compile
[INFO] |  \- ant:ant:jar:1.5:compile
[INFO] \- com.adobe.flexunit:cilistener:swc:4.0-beta-2:test

```

### Flex Mojos ###

[Flex Mojos](http://code.google.com/p/flex-mojos/) is the Maven plugin I used to compile Flex Air application into a swf file and then use the _adt_ tool to package it into an AIR file.

```
            <plugin>
                <groupId>org.sonatype.flexmojos</groupId>
                <artifactId>flexmojos-maven-plugin</artifactId>
                <version>${flexmojos.version}</version>
                <extensions>true</extensions>
                <configuration>
                    <configurationReport>true</configurationReport>
                    <output>${target.bin.dir}/${target.swf.name}</output>
                    <sourceFile>${source.mxml.name}</sourceFile>
                    <configFile>${config.dir}/configurationClassesCompilerConfig.xml</configFile>
                    <rslUrls>
                        <url>{artifactId}-{version}.{extension}</url>
                    </rslUrls>
                    <!--headlessServer>true</headlessServer>
                    <verboseStacktraces>true</verboseStacktraces-->
                </configuration>
                <executions>
                    <execution>
                        <id>flexmojos-wrapper-execution</id>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>wrapper</goal>
                        </goals>
                        <configuration>
                            <htmlName>${target.name}</htmlName>
                            <outputDirectory>${target.bin.dir}</outputDirectory>
                            <parameters>
                                <swf>${target.name}</swf>
                                <application>${target.name}</application>
                                <version_major>${flashplayer.version.major}</version_major>
                                <version_minor>${flashplayer.version.minor}</version_minor>
                                <version_revision>${flashplayer.version.revision}</version_revision>
                            </parameters>
                            <templateURI>embed:express-installation-with-history</templateURI>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
```

Flex Mojos comes with Flex Unit support, for example, you can use the following dependency for Flex Unit,

```
        <dependency>
            <groupId>org.sonatype.flexmojos</groupId>
            <artifactId>flexmojos-unittest-support</artifactId>
            <version>${flexmojos.version}</version>
            <type>swc</type>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>com.adobe.flex.framework</groupId>
                    <artifactId>playerglobal</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```

However, Flex Mojos does not support Flex Unit 4 at this point, I have to use the antrun plugin to run the Flex Unit 4. I will discuss Flex Unit 4 more later.

Flex Mojos also supports asdoc generation. For example, you can use the following execution to bind the asdoc goal to the Maven default install process.

```
                   <execution>
                        <id>generate-asdoc</id>
                        <phase>install</phase>
                        <goals>
                            <goal>asdoc</goal>
                        </goals>
                        <configuration>                       
                            <outputDirectory>${project.build.directory}/asdoc</outputDirectory>
                            <sourcePaths>
                                <path>${project.build.sourceDirectory}</path>
                                <path>${project.build.testSourceDirectory}</path>
                            </sourcePaths>
                            <excludeClasses>
                                <class>TestRunner</class>
                            </excludeClasses>
                        </configuration>
                    </execution>
```

### Package AIR Application ###

The AIR application usually needs to be signed. To sign the application, you need to create a cert first, please read my post [Tomcat SSL with Apr](http://code.google.com/p/jianwikis/wiki/TomcatSSLWithAPR) for how to create a cert. You can also use Maven to generate a cert,

```
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>1.1.1</version>
                <executions>
		    <execution>
			<id>generate-cert</id>
			<phase>package</phase>
			<configuration>
			   <executable>java</executable>
		           <workingDirectory>${basedir}/target/air</workingDirectory>
			   <arguments>
			     <argument>-jar</argument>
			     <argument>${flex.home}/lib/adt.jar</argument>
			     <argument>-certificate</argument>
			     <argument>-cn</argument>
			     <argument>${target.name}</argument>
			     <argument>2048-RSA</argument>
			     <argument>cert.p12</argument>
			     <argument>MY_PASSWORD</argument>
			   </arguments>
			</configuration>
		        <goals>
			   <goal>exec</goal>
			</goals>
                </executions>
            </plugin>
```

To package the AIR application, you need to run the _adt_ command using the exec-maven-plugin,

```
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>exec</goal>
                        </goals>
                        <configuration>
                            <executable>java</executable>
                            <workingDirectory>${basedir}/target/air</workingDirectory>
                            <arguments>
                                <argument>-classpath</argument>
                                <classpath/>
                                <argument>com.adobe.air.ADT</argument>
                                <argument>-package</argument>
                                <argument>-storetype</argument>
                                <argument>pkcs12</argument>
                                <argument>-storepass</argument>
                                <argument>MY_PASSWORD</argument>
                                <argument>-keystore</argument>
                                <argument>cert.p12</argument>
                                <argument>-keypass</argument>
                                <argument>MY_PASSWORD</argument>
                                <argument>-tsa</argument>
                                <argument>none</argument>
                                <argument>${basedir}/target/air/${target.name}.air</argument>
                                <argument>${target.name}-app.xml</argument>
                                <argument>-C</argument>
                                <argument>${target.bin.dir}</argument>
                                <argument>${target.name}.swf</argument>
                                <argument>-C</argument>
                                <argument>${basedir}/target/air</argument>
                                <argument>applicationContext.xml</argument>
                                <argument>-C</argument>
                                <argument>${project.build.directory}/classes</argument>
                                <argument>assets</argument>
                            </arguments>
                        </configuration>
                    </execution>
```

For more details about _adt_, please check out [Packaging an AIR installation file using the AIR Developer Tool (ADT)](http://livedocs.adobe.com/flex/3/html/help.html?content=CommandLineTools_5.html).

### Flex Unit 4 ###

To compile Flex Unit 4 test code, I used the Maven antrun plugin

```
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-antrun-plugin</artifactId>
                <version>1.3</version>
                <executions>
                    <execution>
                        <id>flexunit4-test-compile</id>
                        <phase>test-compile</phase>
                        <configuration>
                            <tasks>
                                <property name="FLEX_HOME" value="${flex.home}"/>
                                <taskdef resource="flexTasks.tasks"/>
                                <taskdef resource="net/sf/antcontrib/antlib.xml"
                                         classpathref="maven.compile.classpath"/>

                                <!-- Compile TestRunner.mxml as a SWF -->
                                <mxmlc file="${project.build.testSourceDirectory}/TestRunner.mxml"
                                       output="${project.build.testOutputDirectory}/TestRunner.swf">
                                    <source-path path-element="${project.build.sourceDirectory}"/>
                                    <source-path path-element="${project.build.testSourceDirectory}"/>
                                    <load-config filename="${FLEX_HOME}/frameworks/air-config.xml"/>
                                    <compiler.library-path dir="${basedir}/target/dependency" append="true">
                                        <include name="*.*"/>
                                    </compiler.library-path>
                                    <compiler.verbose-stacktraces>true</compiler.verbose-stacktraces>
                                    <compiler.headless-server>true</compiler.headless-server>
                                </mxmlc>
                            </tasks>
                        </configuration>
                        <goals>
                            <goal>run</goal>
                        </goals>
                    </execution>
        ......
```

The trick is the line

```
<load-config filename="${FLEX_HOME}/frameworks/air-config.xml"/>
```

You may run into a lot problems without it. In the following line

```
<compiler.library-path dir="${basedir}/target/dependency" append="true">
```

I used the Maven dependencies for the Ant, thus, I need the maven dependency plugin,

```
         <plugin>
            <artifactId>maven-dependency-plugin</artifactId>
            <executions>
               <!-- Copy all library swc's we're dependent upon into a folder so we can use Ant -->
               <execution>
                  <id>copy-dependencies</id>
                  <phase>process-test-resources</phase>
                  <goals>
                     <goal>copy-dependencies</goal>
                  </goals>
                  <configuration>
                     <includeTypes>swc</includeTypes>
                     <overWriteIfNewer>true</overWriteIfNewer>
                     <stripVersion>true</stripVersion> 
                  </configuration>
               </execution>
            </executions>
         </plugin>
```


Another way to compile the Flex Unit 4 test code is to use Flex mojos to compile it, but you need to set up a custom template file, otherwise, the compiled swf will not be Flex Unit 4 compatible. For example, you can add the following line in the configuration section to the flexmojos-maven-plugin,

```
                <testRunnerTemplate>${project.build.testSourceDirectory}/TestRunner.mxml</testRunnerTemplate>

```

I spent a lot of time to try to run the AIR Flex Unit 4 test case using the the Flex Unit ant task as follows,

```
  <!--flexunit swf="${project.build.testOutputDirectory}/TestRunner.swf"
      toDir="${project.build.directory}/surefire-reports"
      haltonfailure="true"
      verbose="true"
      localTrusted="false"/-->
```

But unfortunately, it does not work. I had to use the _adl_ tool directly with the antrun plugin to run the AIR test case,

```
  <execution>
    <id>flexunit4-test-run</id>
    <phase>test</phase>
    <configuration>
       <tasks>
          <!-- Execute TestRunner.swf as FlexUnit tests and publish reports -->
          <taskdef resource="flexUnitTasks.tasks"/>

          <exec executable="${flex.home}/bin/adl" failonerror="false" osfamily="unix">
               <arg value="${project.build.testOutputDirectory}/TestRunner-app.xml"/>
                <arg value="--"/>
                <arg value="${project.build.directory}/surefire-reports"/>
          </exec>
          <exec executable="${flex.home}/bin/adl.exe" failonerror="false" osfamily="windows">
                 <arg value="${project.build.testOutputDirectory}/TestRunner-app.xml"/>
                 <arg value="--"/>
                 <arg value="${project.build.directory}/surefire-reports"/>
          </exec>
      </tasks>
    </configuration>
    <goals>
       <goal>run</goal>
    </goals>
  </execution>
```

# Troubleshootings #

## Error: "Type was not found or was not a compile-time constant: NativeWindow" ##

_playerglobal_ is sneaking into your build, use

```
mvn dependency:tree
```

to see which artifact has the transitive dependency and then exclude _playerglobal_ from it.

## The AIR Application Cannot Be Installed ##

If suddenly, your AIR application cannot be installed even you haven't change any of your POM file, you may need to check your cert file. Try to use a new cert to see if the problem goes away.

## Cannot Load AIR Tests in Flash Player ##

Try to use _adl_ directly to run the AIR test cases.

## How to Close the Test Window Using adl ##

Since we need to _adl_ to run the AIR test cases, how to close the test window would be an issue for CI integration. One way is to use

```
NativeApplication.nativeApplication.exit(); 
```

For example, we have the following TestRunner.xmxl,

```
<?xml version="1.0" encoding="utf-8"?>
<mx:WindowedApplication xmlns:mx="http://www.adobe.com/2006/mxml" xmlns="*" width="100%" height="100%"
    xmlns:flexUnitUIRunner="http://www.adobe.com/2009/flexUnitUIRunner"
    creationComplete="onCreationComplete()">

	<mx:Script>
	    <![CDATA[

	    import com.mycompany.AirTestSuite;
            import flash.desktop.NativeApplication;
            
            import org.flexunit.listeners.CIListener;
	    import org.flexunit.listeners.UIListener;
	    import org.flexunit.runner.FlexUnitCore;
            import org.flexunit.runner.notification.async.XMLListener;

	    private var flexUnitCore:FlexUnitCore;
            private var loadTimer:Timer;
            private var timeoutInMilliseconds:Number = 10000;

	    protected function onCreationComplete():void
	    {
                loadTimer = new Timer(timeoutInMilliseconds);
                loadTimer.addEventListener(TimerEvent.TIMER, closeTest);
                loadTimer.start();

		flexUnitCore = new FlexUnitCore();
 		//Listener for the UI, optional
 		flexUnitCore.addListener( new UIListener( testRunner ));
//              flexUnitCore.addListener( new XMLListener( "testRunner" ) );
                flexUnitCore.addListener(new CIListener());
 		//This run statements executes the unit tests for the FlexUnit4 framework
 		flexUnitCore.run(AirTestSuite);

 	    }

            private function closeTest(event:TimerEvent):void
            {
                NativeApplication.nativeApplication.clear();
                NativeApplication.nativeApplication.exit();
            }
		
           ]]>
	</mx:Script>

	<flexUnitUIRunner:TestRunnerBase id="testRunner" width="100%" height="100%" />
</mx:WindowedApplication>
```

That is to say, we use a timer to close test after some time-out threshold.

## Why I got "Problem finding external stylesheet" ##

You need to point the style source to the one in the resources directory, i.e.,

```
	<mx:Style source="../resources/assets/styles/appStyle.css"/>
```

# Summary #

The Maven build for the Flex AIR application may have a lot of problems and you may not even be able to find any solution to them. Hope this post could help your Adobe AIR Maven build somehow. You might like to create a Flex AIR Maven archetype so that you don't need to manually create the Flex AIR project. For how to manually create a Maven archetype, please check my wiki page [How Tellurium Maven Archetypes Are Created](http://code.google.com/p/aost/wiki/TelluriumMavenArchetypes#How_Tellurium_Maven_Archetypes_Are_Created). If you have an existing AIR project, you can create the Maven archetype from it. For more details, please read [How to Create a Maven Archetype From an Existing Project](http://code.google.com/p/jianwikis/wiki/HowToCreateMavenArchetypeFromProject).

# Resources #

  * [Adobe AIR](http://www.adobe.com/products/air/develop/flex/)
  * [Adobe Flex 3 SDK](http://opensource.adobe.com/wiki/display/flexsdk/Download+Flex+3)
  * [Flex 3 LiveDocs](http://livedocs.adobe.com/flex/3/)
  * [Adobe AIR Download](http://get.adobe.com/air/)
  * [Install Adobe AIR, BlazDS, and Blaze Monster on Fedora 11](http://code.google.com/p/jianwikis/wiki/InstallAIRonFedora11#Adobe_Air)
  * [Pure MVC](http://puremvc.org/)
  * [Spring ActionScript](http://www.springsource.org/extensions/se-springactionscript-as)
  * [Flex Mojos](http://code.google.com/p/flex-mojos/)
  * [Flex Mojos Group](http://groups.google.com/group/flex-mojos)
  * [Flex Builder 3](http://www.adobe.com/cfusion/entitlement/index.cfm?e=flexbuilder3)
  * [Flex Unit 4](http://opensource.adobe.com/wiki/display/flexunit/FlexUnit+4+feature+overview)
  * [Flex Unit Forum](http://forums.adobe.com/community/opensource/flexunit)
  * [Tomcat SSL with Apr](http://code.google.com/p/jianwikis/wiki/TomcatSSLWithAPR)
  * [How Tellurium Maven Archetypes Are Created](http://code.google.com/p/aost/wiki/TelluriumMavenArchetypes#How_Tellurium_Maven_Archetypes_Are_Created)
  * [How to Create a Maven Archetype From an Existing Project](http://code.google.com/p/jianwikis/wiki/HowToCreateMavenArchetypeFromProject)
  * [Tellurium Maven Guide](http://code.google.com/p/aost/wiki/MavenHowTo)
  * [Packaging an AIR installation file using the AIR Developer Tool (ADT)](http://livedocs.adobe.com/flex/3/html/help.html?content=CommandLineTools_5.html)
  * [Adobe Flash Player](http://www.adobe.com/support/flashplayer/downloads.html)
  * [Maven](http://maven.apache.org/download.html)
  * [the official Maven Installation Guide](http://maven.apache.org/download.html#Installation)
  * [Maven antrun plugin](http://maven.apache.org/plugins/maven-antrun-plugin/)
  * [NTLM Authorization Proxy Server](http://ntlmaps.sourceforge.net/)