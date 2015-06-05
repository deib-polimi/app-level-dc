~~The MODAClouds Application Level Data Collector~~
=======================
> The library was migrated and included in the new [Tower 4Clouds Repository](https://github.com/deib-polimi/tower4clouds) under the data-collectors module, it was renamed java-app-dc
------

In the context of MODAClouds European project (www.modaclouds.eu), Politecnico was
one of the partners involved in the development of the Monitoring Platform.

The application level data collector is a java library that allow developers to collect monitoring data
at the application level. It implements the [MODAClouds Data Collector Factory Library](https://github.com/deib-polimi/modaclouds-data-collector-factory).

Please refer to deliverable [D6.3.2](http://www.modaclouds.eu/publications/public-deliverables/) 
to better understand the role of this component in the MODAClouds Monitoring Platform.

Refer to the [Monitoring Platform Wiki](https://github.com/deib-polimi/modaclouds-monitoring-manager/wiki) for installation and usage of the whole platform.

## Documentation

Take a look at the [documentation table of contents](doc/TOC.md).

## Change List

v0.6.3:
* bug fix
* updated to [data-collector-factory 0.3.4](https://github.com/deib-polimi/modaclouds-data-collector-factory/releases/tag/v0.3.4) 

v0.6.2:
* fixed error that did not allow to parse annotated methods using reflections
* added default configuration for simplifying local testing
* performance improvements, minimized access to the KB
* fixed conversion problems that caused average throughput to be sent at a wrong sampling time and sending wrong values
* updated to [data-collector-factory 0.3.3](https://github.com/deib-polimi/modaclouds-data-collector-factory/releases/tag/v0.3.3)

v0.6.1:
* Throughput can be collected also for the entire application

v0.6:
* Throughput added as new metric

v0.5.2:
* Synchronization with KB can be started manually, see the doc

v0.5.1:
* Fixed a problem that prevented methods called by monitored methods to be monitored 

v0.5:
* EffectiveResponseTime added as new metric
* API changed, check the doc

v0.4.1:
* updated to [data-collector-factory 0.3.1](https://github.com/deib-polimi/modaclouds-data-collector-factory/releases/tag/v0.3.1)

v0.4:
* updated to [data-collector-factory 0.3](https://github.com/deib-polimi/modaclouds-data-collector-factory/releases/tag/v0.3)

v0.3.4:
* configuration can be done through system properties as well as
environment variables now (system properties have priority)

v0.3.3:

* localhost is now allowed in configuration
* in the FakeServletExample the DC meta data upload was removed to avoid missunderstanding, the monitoring
manager is responsible of loading the configuration on the KB, so use the example by installing a monitoring rule
on the monitoring platform 

