I used the Maven release plugin to release my Smart Account Management Framework ([SAcct](http://code.google.com/p/sacct/)). It was pretty smooth and I listed my steps here.

## Add Maven Release Plugin ##

Add the Maven release plugin to the SAcct project super POM as follows.

```
     <build>
        <pluginManagement>
           <plugins>
               ......

                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-release-plugin</artifactId>
                    <version>2.0-beta-9</version>
                    <configuration>
                        <tagBase>https://sacct.googlecode.com/svn/tags</tagBase>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
```

Note, I put the tagBase there so that the Maven release plugin knows where to cut the release tag.


## Add Maven Deployment Credential ##

To deploy the release artifacts to our Maven Repository, I need to add the Maven deployment credential to the file, MY\_HOME/.m2/setting.xml, as follows,

```
<settings>
    <servers>
       ......
       <server>
           <id>kungfuters-public-releases-repo</id>
           <username>MY_USER_NAME</username>
           <password>MY_PASSWORD</password>
      </server>
    </servers>
   ......
```

## Prepare Maven Release ##

Check out the SAcct code from trunk, then go to the project and run the Maven release prepare command

```
mvn -Dusername=MY_USENAME -Dpassword=MY_PASSWORD release:prepare
```

Be aware that here the username and password are the ones for my source code repository, i.e., subversion repository in GoogleCode in my case. You will be asked to confirm what is the release version for each project module and what will be the version of each project module for the next deployment phase.

Sometimes, you may like to have a dry run to test the procedure, you can use the following  command

```
mvn -Dusername=MY_USENAME -Dpassword=MY_PASSWORD release:prepare -DdryRun=true
```

After the dry run, you can use the clean command to clear all the generated file

```
mvn release:clean
```

The Maven release:prepare command will update the POM version for each module and cut a release tag. If you look at the Subversion history, you will see the committs as follows,

http://sacct-users.googlegroups.com/web/MavenPrepare4SAcct010.png?gda=WvF_vUwAAABwsiXcufpgHYK0Fq-t18-VeL5-84BcIQKjqKWcs5nNlpgWu9g6a3McrETuCNdBQZV9I3qX8W09aeKeW-JwEwfz_Vpvmo5s1aABVJRO3P3wLQ&gsc=HWN0mgsAAACDqjODsnEBV4QszHJLkSQN

## Perform Maven Release ##

The last step is to run the following command to actually deploy artifacts to my Maven repository

```
mvn release:perform
```

After that check [the Maven repository](http://kungfuters.org/nexus/content/repositories/releases/org/osomit/) and you will see all artifacts are there.

## Resources ##

  * [Smart Account Management (SAcct)](http://code.google.com/p/sacct/)
  * [Maven Release Plugin](http://maven.apache.org/plugins/maven-release-plugin/usage.html)
