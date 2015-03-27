[Documentation table of contents](TOC.md)

# User Manual

## Provided Metrics

|Metric Name|Target Class|Required Parameters|Description|
|-----------|------------|-------------------|-----------|
|ResponseTime|Method|<ul><li>samplingProbability (default: 1)</li></ul>|Collect the response time (in milliseconds) for the target method specified in the monitoring rule with the given probability (in [0,1])|
|EffectiveResponseTime|Method|<ul><li>samplingProbability (default: 1)</li></ul>|Like ResponseTime, but execution time in external calls is subtracted from the total response time|
|Throughput|Method|<ul><li>samplingTime (default: 60)</li></ul>|Collect the throughput (in requests per second) for the target method specified in the monitoring rule with the given sampling time (in seconds)|
|Throughput|InternalComponent|<ul><li>samplingTime (default: 60)</li></ul>|Collect the cumulative throughput (in requests per second) for all monitored methods in the application with the given sampling time (in seconds)|

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
	* `MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP` (optional, default: "127.0.0.1")
	* `MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT` (optional, default: "3030")
	* `MODACLOUDS_MONITORING_DDA_ENDPOINT_IP` (optional, default: "127.0.0.1")
	* `MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT` (optional, default: "8175")
	* `MODACLOUDS_MONITORED_APP_ID` (mandatory)
	* `MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH` (optional, default: "/modaclouds/kb")
	* `MODACLOUDS_KNOWLEDGEBASE_SYNC_PERIOD` (optional, default: "10")
	* `MODACLOUDS_START_SYNC_WITH_KB` (optional, default: "true")

- If `MODACLOUDS_START_SYNC_WITH_KB` is set to false, KB synchronization won't start automatically. It can be started manually as follows:

	```java
	AppDataCollectorFactory.startSyncingWithKB();
	```
- The data collector is automatically initialized as soon as the first monitored method is called. Configuration errors will be logged but won't raise any exception during automatic initialization. In order to check the correctness of the configuration and to avoid the initialization time to compromise the first collected datum it is suggested to initialize it manually at startup as follows:

	```java
	AppDataCollectorFactory.init(monitoredClassesPackagePrefix);
	```
Setting a `monitoredClassesPackagePrefix` such as `"it.polimi.modaclouds.myapp"` will speedup the initial parsing of monitored methods. Automatic initialization will use the empty string `""`, which will make the library look for annotated methods in all possible packages.   

KB and DDA must be running for the data collectors to be able to 
retrieve their configuration from the KB and to be able to feed the DDA.
