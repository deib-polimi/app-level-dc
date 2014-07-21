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

import it.polimi.modaclouds.monitoring.appleveldc.metrics.ServiceTime;
import it.polimi.modaclouds.monitoring.dcfactory.DataCollectorFactory;
import it.polimi.modaclouds.monitoring.dcfactory.connectors.DDAHandler;
import it.polimi.modaclouds.monitoring.dcfactory.connectors.FusekiKBHandler;
import it.polimi.modaclouds.monitoring.dcfactory.connectors.KBHandler;
import it.polimi.modaclouds.monitoring.dcfactory.connectors.RspCsparqlServerDDAHandler;
import it.polimi.modaclouds.qos_models.monitoring_ontology.DataCollector;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppDataCollectorFactory extends DataCollectorFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(AppDataCollectorFactory.class);

	private static AppDataCollectorFactory _INSTANCE = null;
	private static int kbSyncPeriod;
	private static String appId;
	private static String ddaURL;
	private static String kbURL;

	/**
	 * 
	 * @param monitoredPackagePrefix
	 *            the package name prefix where monitored methods are (e.g.,
	 *            it.polimi.myapp).
	 * @throws ConfigurationException
	 */
	public static void initialize(String monitoredPackagePrefix)
			throws ConfigurationException {
		logger.info("Initializing {}...",
				AppDataCollectorFactory.class.getSimpleName());
		if (_INSTANCE != null) {
			logger.warn("{} is already initialized. Nothing to do.");
			return;
		}

		loadConfiguration();

		DDAHandler dda = new RspCsparqlServerDDAHandler(ddaURL);
		KBHandler kb = new FusekiKBHandler(kbURL);
		_INSTANCE = new AppDataCollectorFactory(dda, kb);

		parseMonitoredMethods(monitoredPackagePrefix);

		logger.info(
				"{} initialized with:\n\tddaURL: {}\n\tkbURL: {}\n\tkbSyncPeriod: {}",
				AppDataCollectorFactory.class.getSimpleName(), ddaURL, kbURL,
				kbSyncPeriod);
	}

	private static void parseMonitoredMethods(String monitoredPackagePrefix) {
		Reflections reflections = new Reflections(monitoredPackagePrefix,
				new MethodAnnotationsScanner());
		Set<Method> methods = reflections
				.getMethodsAnnotatedWith(Monitor.class);
		for (Method m : methods) {
			Monitor monitor = m.getAnnotation(Monitor.class);
			_INSTANCE.addMonitoredResourceId(getMethodId(monitor.name()));
		}
	}

	private static void loadConfiguration() throws ConfigurationException {
		ddaURL = Config.getDDAURL();
		kbURL = Config.getKBURL();
		kbSyncPeriod = Config.getKBSyncPeriod();
		appId = Config.getAppId();
	}

	public void start() {
		startSyncingWithKB(kbSyncPeriod);
		logger.info("{} started", AppDataCollectorFactory.class.getSimpleName());
	}

	public static boolean isInitialized() {
		return _INSTANCE != null;
	}

	public static AppDataCollectorFactory getInstance() {
		if (_INSTANCE == null)
			logger.error(
					"{} not initialized. Please run {}.initialize() before",
					AppDataCollectorFactory.class.getSimpleName(),
					AppDataCollectorFactory.class.getSimpleName());
		return _INSTANCE;
	}

	private AppDataCollectorFactory(DDAHandler dda, KBHandler kb) {
		super(dda, kb);
	}

	@Override
	protected void syncedWithKB() {
		// nothing changes since response time is collected in push mode and
		// installed data collectors are checked when a request arrives
	}

	public static String getMethodId(String name) {
		return appId + "-" + name;
	}

	public void collect(String value, String metric, String monitoredResourceId) {
		DataCollector dc = getDataCollector(monitoredResourceId, metric);
		if (dc != null) {
			Map<String,String> parameters = getParameters(dc);
			double samplingProbability = ServiceTime.getSamplingProbability(parameters);
			if( Math.random() < samplingProbability )
				sendAsyncMonitoringDatum(value, metric, monitoredResourceId);
		}
	}

}
