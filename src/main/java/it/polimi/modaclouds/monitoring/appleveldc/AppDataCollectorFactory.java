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
import it.polimi.modaclouds.monitoring.dcfactory.wrappers.DDAConnector;
import it.polimi.modaclouds.monitoring.dcfactory.wrappers.KBConnector;

import java.util.Set;

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

	static {
		try {
			init();
		} catch (Exception e) {
			logger.error("Could not initilize {} properly",
					AppDataCollectorFactory.class.getSimpleName(), e);
		}
	}

	private AppDataCollectorFactory(DDAConnector dda, KBConnector kb) {
		super(dda, kb);
	}

	@Override
	protected void syncedWithKB() {
		Metric.notifyAllSyncedWithKB();
	}

	public static String getMethodId(String methodType) {
		return appId + "-" + methodType;
	}

	static void send(String value, Metric metric, String monitoredResourceId) {
		if (_INSTANCE == null) {
			logger.error("Data Collector not initialized properly, could not collect data");
			return;
		}
		_INSTANCE.sendAsyncMonitoringDatum(value, metric.getName(),
				monitoredResourceId);
	}

	/**
	 * Start the synchronization with the KB. Nothing will happen if it was
	 * already started.
	 */
	public static void startSyncingWithKB() {
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

	/**
	 * Initialize the data collector
	 * 
	 * @throws ConfigurationException
	 */
	public static void init() throws ConfigurationException {
		if (!initialized) {
			logger.info("Initializing {}",
					AppDataCollectorFactory.class.getSimpleName());

			config = Config.getInstance();
			ddaURL = config.getDdaUrl();
			kbURL = config.getKbUrl();
			kbSyncPeriod = config.getKbSyncPeriod();
			appId = config.getAppId();

			DDAConnector dda = new DDAConnector(ddaURL);
			KBConnector kb = new KBConnector(kbURL);
			_INSTANCE = new AppDataCollectorFactory(dda, kb);

			logger.info(
					"{} initialized with:\n\tddaURL: {}\n\tkbURL: {}\n\tkbSyncPeriod: {}",
					AppDataCollectorFactory.class.getSimpleName(), ddaURL,
					kbURL, kbSyncPeriod);

			logger.info("Starting synchronization with KB");

			if (config.isStartSyncingWithKB())
				_INSTANCE.startSyncingWithKB(kbSyncPeriod);
			initialized = true;
		}
	}

	protected static Set<DCConfig> getConfiguration(String monitoredResourceId,
			Metric metric) {
		return _INSTANCE.getConfiguration(monitoredResourceId, metric.getName());
	}

}
