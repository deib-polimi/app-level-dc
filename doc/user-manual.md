[Documentation table of contents](TOC.md)

# User Manual

## Usage

In order to use the library you should first add our library as a dependency in your maven project:

Releases repository:
```xml
<repositories>
	<repository>
        <id>deib-polimi-releases</id>
        <url>https://github.com/deib-polimi/deib-polimi-mvn-repo/raw/master/releases</url>
	</repository>
</repositories>
```

Snapshots repository:
```xml
<repositories>
	<repository>
        <id>deib-polimi-snapshots</id>
        <url>https://github.com/deib-polimi/deib-polimi-mvn-repo/raw/master/snapshots</url>
	</repository>
</repositories>
```

Dependency:
```xml
<dependencies>
	<dependency>
		<groupId>it.polimi.modaclouds.monitoring</groupId>
		<artifactId>app-level-dc</artifactId>
		<version>0.2-SNAPSHOT</version>
	</dependency>
</dependencies>
```

Then you need to include in your build life cycle the aspectj plugin:

```xml
<build>
	<plugins>
		<plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>aspectj-maven-plugin</artifactId>
            <version>1.5</version>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>test-compile</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <complianceLevel>1.7</complianceLevel>
                <source>1.7</source>
                <target>1.7</target>
                <aspectLibraries>
                    <aspectLibrary>
                        <groupId>it.polimi.modaclouds.monitoring</groupId>
                        <artifactId>app-level-dc</artifactId>
                    </aspectLibrary>
                </aspectLibraries>
            </configuration>
        </plugin>
	</plugins>
</build>
```

Have a look at the examples in the examples package for understanding how to use the library.

In short:
- Annotate methods you want to monitor the ServiceTime, passing the name (type) of the monitored method
- Have the data collector initialized at startup
- Expose required environment variables (See class Env for the complete list of environment variables)

KB and DDA must be running for the data collectors to be able to 
retrieve their configuration from the KB and to be able to feed the DDA.

In the FakeServleExample a new DC is installed in the KB after 5 seconds. This is just done
for the purpose of the example, the monitoring manager will take care of installing data collectors
on the KB in a normal scenario.