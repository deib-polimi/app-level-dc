[Documentation table of contents](TOC.md)

# User Manual

## Provided Metrics

|Metric Name|Target Class|Required Parameters|Description|
|-----------|------------|-------------------|-----------|
|ResponseTime|Method|<ul><li>samplingProbability</li></ul>|Whenever a monitored method is called, the response time for such method is collected and sent with the given probability|
|EffectiveResponseTime|Method|<ul><li>samplingProbability</li></ul>|Like response time, but external calls are excluded|

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
- Annotate methods you want to monitor, passing the [Resource Type](https://github.com/deib-polimi/modaclouds-qos-models/blob/master/doc/user-manual.md#the-monitoring-ontology) of the monitored method

	```java
	@Monitor(type = "register")
	private void register() {
		//...
	}
	```
- Annotate methods that perform external calls. The processing time of such methods will be excluded by the EffectiveResponseTime.

	```java
	@ExternalCall
	private void outgoingCall() {
		// write to DB
	}
	```
- Alternatively, the scope of outgoing calls can be delimited programmatically:

	```java
	private void register() {
		AppDataCollectorFactory.startsExternalCall();
		// write to DB
		AppDataCollectorFactory.endsExternalCall();
	}
	```
- In order for the data collector to work properly the following configuration must be set either through environement variables or through system properties (System properties have priority if a variable is specified in both ways):
	* `MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP`
	* `MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT`
	* `MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH`
	* `MODACLOUDS_MONITORING_DDA_ENDPOINT_IP`
	* `MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT`
	* `MODACLOUDS_MONITORED_APP_ID`
	* `MODACLOUDS_KNOWLEDGEBASE_SYNC_PERIOD` (optional, default: 10)
	* `MODACLOUDS_START_SYNC_WITH_KB` (optional, default: true)

- if `MODACLOUDS_START_SYNC_WITH_KB` is set to false, KB synchronization won't start automatically, and it can be started manually as follows:

	```java
	AppDataCollectorFactory.startSyncingWithKB();
	```

KB and DDA must be running for the data collectors to be able to 
retrieve their configuration from the KB and to be able to feed the DDA.
