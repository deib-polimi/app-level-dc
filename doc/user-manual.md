[Documentation table of contents](TOC.md)

# User Manual

## Installation

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
		<version>VERSION</version>
	</dependency>
</dependencies>
```

Then we need to include in your build life cycle the aspectj plugin:

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

## Configuration

Add to your project build path a properties file named "objectostore.properties" with information about the
object store address and port. Example:

```
objs_server.address=localhost
objs_server.port=8800
```

## Usage

Just add annotation `@MonitoredMetric("<MetricID>")` to the method to be monitored.
For this first release <MetricID> can 
only be either "ResponseTime" or "Throughput".

## Code Samples

```java
@MonitoredMetric("ResponseTime")
public void login() {
	// do login
}
```