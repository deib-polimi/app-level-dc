/**
 * Copyright 2014 deib-polimi
 * Contact: deib-polimi <marco.miglierina@polimi.it>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package it.polimi.modaclouds.monitoring.appleveldc;

import it.polimi.modaclouds.monitoring.dcfactory.DCConfig;
import it.polimi.modaclouds.qos_models.monitoring_ontology.InternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Resource;

import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Metric {

	private static final Logger logger = LoggerFactory.getLogger(Metric.class);
	static final Set<Metric> metrics = new HashSet<Metric>();
	// private static Set<Method> monitoredMethods;
	// private static InternalComponent monitoredApp;

	static {
		Reflections.log = null;
		Reflections reflections = new Reflections(
				"it.polimi.modaclouds.monitoring.appleveldc.metrics");
		Set<Class<? extends Metric>> metricsClasses = reflections
				.getSubTypesOf(Metric.class);
		for (Class<? extends Metric> metricClass : metricsClasses) {
			try {
				metrics.add(metricClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error(
						"Error while trying to instantiate metric class {}",
						metricClass.getName(), e);
			}
		}
	}

	protected static Set<Method> getMonitoredMethods() {
		Set<Method> monitoredMethods = AppDataCollectorFactory
				.getMonitoredMethods();
		if (monitoredMethods == null) {
			monitoredMethods = new HashSet<Method>();
		}
		return monitoredMethods;
	}

	protected static InternalComponent getMonitoredApp() {
		return AppDataCollectorFactory.getMonitoredApp();
	}

	//
	// protected static void setMonitoredApp(InternalComponent monitoredApp) {
	// Metric.monitoredApp = monitoredApp;
	// }
	//
	// static void setMonitoredMethods(Set<Method> methods) {
	// monitoredMethods = methods;
	// }

	protected String getName() {
		return getClass().getSimpleName();
	}

	static void notifyAllMethodStarts(Method method) {
		for (Metric metric : metrics) {
			metric.methodStarts(method);
		}
	}

	static void notifyAllMethodEnds(Method method) {
		for (Metric metric : metrics) {
			metric.methodEnds(method);
		}
	}

	protected abstract void methodStarts(Method method);

	protected abstract void methodEnds(Method method);

	static void notifyAllExternalMethodStarts() {
		for (Metric metric : metrics) {
			metric.externalMethodStarts();
		}
	}

	static void notifyAllExternalMethodEnds() {
		for (Metric metric : metrics) {
			metric.externalMethodEnds();
		}
	}

	protected abstract void externalMethodStarts();

	protected abstract void externalMethodEnds();

	static void notifyAllSyncedWithKB() {
		for (Metric metric : metrics) {
			metric.syncedWithKB();
		}
	}

	protected abstract void syncedWithKB();

	protected final void send(String value, Resource resource) {
		Set<DCConfig> dcsConfigs = AppDataCollectorFactory.getConfiguration(
				resource, this);
		if (dcsConfigs == null || dcsConfigs.isEmpty()) {
			logger.warn("Attempting to send data even if not required by the configuration, data won't be sent");
			return;
		}
		AppDataCollectorFactory.send(value, this, resource);
	}

	protected final Set<DCConfig> getConfiguration(Resource resource) {
		Set<DCConfig> dcsConfigs = AppDataCollectorFactory.getConfiguration(
				resource, this);
		if (dcsConfigs == null)
			dcsConfigs = new HashSet<DCConfig>();
		return dcsConfigs;
	}

	protected static boolean isDouble(String value) {
		if (value == null)
			return false;
		try {
			Double.parseDouble(value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	protected static boolean isInteger(String value) {
		if (value == null)
			return false;
		try {
			Integer.parseInt(value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
