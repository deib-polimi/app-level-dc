[Documentation table of contents](TOC.md)

# Developer Manual

To add a new Metric, just create a new class with the name of the required metric inside the package `it.polimi.modaclouds.monitoring.appleveldc.metrics` extending the class `it.polimi.modaclouds.monitoring.appleveldc.Metric` and implement required methods. The metric will be added as observer of the events on monitored metrics through reflection.

Remember to update the [Provided Metrics table](user-manual.md#provided-metrics).
