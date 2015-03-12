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

import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Metric {

	private static final Logger logger = LoggerFactory.getLogger(Metric.class);
	private static Set<Metric> metrics = new HashSet<Metric>();

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

	// protected abstract DCConfig selectDC(Set<DCConfig> dcs);

	protected String getName() {
		return getClass().getSimpleName();
	}

	// protected abstract boolean shouldSend(DCConfig dc);

	static void notifyAllMethodStarts(String type) {
		for (Metric metric : metrics) {
			metric.methodStarts(type);
		}
	}

	static void notifyAllMethodEnds(String type) {
		for (Metric metric : metrics) {
			metric.methodEnds(type);
		}
	}

	protected abstract void methodStarts(String type);

	protected abstract void methodEnds(String type);

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

	protected final void send(String value, String monitoredResourceId) {
		Set<DCConfig> dcsConfigs = AppDataCollectorFactory.getConfiguration(
				monitoredResourceId, this);
		if (dcsConfigs == null || dcsConfigs.isEmpty()) {
			logger.warn("Attempting to send data even if not required by the configuration, data won't be sent");
			return;
		}
		AppDataCollectorFactory.send(value, this, monitoredResourceId);
	}

	protected final Set<DCConfig> getConfiguration(String monitoredResourceId) {
		Set<DCConfig> dcsConfigs = AppDataCollectorFactory.getConfiguration(monitoredResourceId,
				this);
		if (dcsConfigs==null) dcsConfigs = new HashSet<DCConfig>();
		return dcsConfigs;
	}

}
