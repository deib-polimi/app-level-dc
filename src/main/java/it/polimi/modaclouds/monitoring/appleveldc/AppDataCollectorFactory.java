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
import it.polimi.modaclouds.monitoring.dcfactory.DataCollectorFactory;
import it.polimi.modaclouds.qos_models.monitoring_ontology.InternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Resource;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppDataCollectorFactory extends DataCollectorFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(AppDataCollectorFactory.class);

	private static AppDataCollectorFactory _INSTANCE;
	private static int kbSyncPeriod;
	private static String appId;
	private static String ddaURL;
	private static String kbURL;

	private static Config config;

	private static boolean initialized = false;

	private static Set<it.polimi.modaclouds.qos_models.monitoring_ontology.Method> monitoredMethods;

	private static InternalComponent monitoredApp;

	static {
		try {
			init();
		} catch (Exception e) {
			logger.error("Could not initialize {} properly: {}",
					AppDataCollectorFactory.class.getSimpleName(),
					e.getMessage());
		}
	}

	private AppDataCollectorFactory(String ddaURL, String kbURL) {
		super(ddaURL, kbURL);
	}

	@Override
	protected void syncedWithKB() {
		Metric.notifyAllSyncedWithKB();
	}

	// public static String getMethodId(String methodType) {
	// return appId + "-" + methodType;
	// }

	static void send(String value, Metric metric, Resource resource) {
		if (_INSTANCE == null) {
			logger.error("{} not initialized properly, could not collect data",
					AppDataCollectorFactory.class.getSimpleName());
			return;
		}
		_INSTANCE.sendAsyncMonitoringDatum(value, metric.getName(), resource);
	}

	/**
	 * Start the synchronization with the KB. Nothing will happen if it was
	 * already started.
	 */
	public static void startSyncingWithKB() {
		logger.info("Sarting syncing with KB");
		_INSTANCE.startSyncingWithKB(kbSyncPeriod);
	}

	/**
	 * Delimit the starting of an external call programmatically
	 */
	public static void startsExternalCall() {
		logger.debug("Starting external call");
		Metric.notifyAllExternalMethodStarts();
	}

	/**
	 * Delimit the ending of an external call programmatically
	 */
	public static void endsExternalCall() {
		logger.debug("Ending external call");
		Metric.notifyAllExternalMethodEnds();
	}

	public static void init()
			throws ConfigurationException {
		if (!initialized) {
			logger.debug("DEBUG enabled");
			logger.info("Initializing {}",
					AppDataCollectorFactory.class.getSimpleName());

			config = Config.getInstance();
			ddaURL = config.getDdaUrl();
			kbURL = config.getKbUrl();
			kbSyncPeriod = config.getKbSyncPeriod();
			appId = config.getAppId();

			_INSTANCE = new AppDataCollectorFactory(ddaURL, kbURL);

			logger.info(
					"{} initialized with:\n\tddaURL: {}\n\tkbURL: {}\n\tkbSyncPeriod: {}",
					AppDataCollectorFactory.class.getSimpleName(), ddaURL,
					kbURL, kbSyncPeriod);

			logger.info("Parsing monitored methods");
			monitoredMethods = parseMonitoredMethods("");
			monitoredApp = (InternalComponent) _INSTANCE.kb
					.getResourceById(appId);

			logger.info("Starting synchronization with KB");
			if (config.isStartSyncingWithKB())
				startSyncingWithKB();
			initialized = true;
		}
	}

	static Set<it.polimi.modaclouds.qos_models.monitoring_ontology.Method> parseMonitoredMethods(
			String monitoredClassesPackagePrefix) {
		Reflections.log = null;
		Reflections reflections = new Reflections(new ConfigurationBuilder()
				.setUrls(
						ClasspathHelper
								.forPackage(monitoredClassesPackagePrefix))
				.setScanners(new MethodAnnotationsScanner()));
		Set<Method> annotatedMethods = reflections
				.getMethodsAnnotatedWith(Monitor.class);
		Set<it.polimi.modaclouds.qos_models.monitoring_ontology.Method> monitoredMethods = new HashSet<it.polimi.modaclouds.qos_models.monitoring_ontology.Method>();
		for (Method method : annotatedMethods) {
			String methodType = method.getAnnotation(Monitor.class).type();
			it.polimi.modaclouds.qos_models.monitoring_ontology.Method monitoredMethod = new it.polimi.modaclouds.qos_models.monitoring_ontology.Method(
					AppDataCollectorFactory.getAppId(), methodType);
			monitoredMethods.add(monitoredMethod);
			logger.info("Monitored method found: type={}, id={}",
					monitoredMethod.getType(), monitoredMethod.getId());
		}
		return monitoredMethods;
	}

	protected static Set<DCConfig> getConfiguration(Resource resource,
			Metric metric) {
		if (_INSTANCE == null) {
			logger.error("{} not initilialized properly",
					AppDataCollectorFactory.class.getSimpleName());
			return new HashSet<DCConfig>();
		}
		return _INSTANCE.getConfiguration(resource, metric.getName());
	}

	public static String getAppId() {
		return appId;
	}

	public static Set<it.polimi.modaclouds.qos_models.monitoring_ontology.Method> getMonitoredMethods() {
		return monitoredMethods;
	}

	public static InternalComponent getMonitoredApp() {
		if (monitoredApp == null)
			monitoredApp = (InternalComponent) _INSTANCE.kb
					.getResourceById(appId);
		return monitoredApp;
	}

}
