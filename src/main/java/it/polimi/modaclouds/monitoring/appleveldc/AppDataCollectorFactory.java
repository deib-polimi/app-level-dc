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

import it.polimi.modaclouds.monitoring.appleveldc.metrics.Metric;
import it.polimi.modaclouds.monitoring.dcfactory.DCConfig;
import it.polimi.modaclouds.monitoring.dcfactory.DataCollectorFactory;
import it.polimi.modaclouds.monitoring.dcfactory.wrappers.DDAConnector;
import it.polimi.modaclouds.monitoring.dcfactory.wrappers.KBConnector;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppDataCollectorFactory extends DataCollectorFactory {

	// TODO these should be metric specific
	static Map<Long, Map<String,Long>> startTimesPerMethodPerThreadId;
	static Map<Long, Long> externalStartTimes;
	static Map<Long, Long> externalTimes;

	private static final Logger logger = LoggerFactory
			.getLogger(AppDataCollectorFactory.class);

	private static AppDataCollectorFactory _INSTANCE;
	private static int kbSyncPeriod;
	private static String appId;
	private static String ddaURL;
	private static String kbURL;

	private static Config config;

//	public static void init() {
//		
//	}

	static {
		logger.info("Initializing {}",
				AppDataCollectorFactory.class.getSimpleName());

		startTimesPerMethodPerThreadId = new ConcurrentHashMap<Long, Map<String,Long>>();
		externalStartTimes = new ConcurrentHashMap<Long, Long>();
		externalTimes = new ConcurrentHashMap<Long, Long>();
		try {
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
		} catch (Exception e) {
			logger.error("Could not initilize {} properly",
					AppDataCollectorFactory.class.getSimpleName(), e);
		}

	}

	// public static boolean isInitialized() {
	// return _INSTANCE != null;
	// }

	// public static AppDataCollectorFactory getInstance() {
	// if (_INSTANCE == null)
	// logger.error(
	// "{} not initialized. Please run {}.initialize() before",
	// AppDataCollectorFactory.class.getSimpleName(),
	// AppDataCollectorFactory.class.getSimpleName());
	// return _INSTANCE;
	// }

	private AppDataCollectorFactory(DDAConnector dda, KBConnector kb) {
		super(dda, kb);
	}

	@Override
	protected void syncedWithKB() {
		// nothing changes since response time is collected in push mode and
		// installed data collectors are checked when a request arrives
	}

	static String getMethodId(String name) {
		return appId + "-" + name;
	}

	static void collect(String value, Metric metric, String monitoredResourceId) {
		if (_INSTANCE == null) {
			logger.error("Data Collector not initialized properly, could not collect data");
			return;
		}
		Set<DCConfig> dcs = _INSTANCE.getConfiguration(monitoredResourceId,
				metric.getName());
		if (dcs != null && !dcs.isEmpty()) {
			DCConfig dc = metric.selectDC(dcs);
			if (metric.shouldSend(dc))
				_INSTANCE.sendAsyncMonitoringDatum(value, metric.getName(),
						monitoredResourceId);
		}
	}

	public static void startSyncingWithKB() {
		_INSTANCE.startSyncingWithKB(kbSyncPeriod);
	}

	public static void startsExternalCall() {
		Long externalCallStartTime = externalStartTimes
				.get(Thread.currentThread().getId());
		if (externalCallStartTime != null) {
			logger.error("Declaring the beginning of an external call inside a "
					+ "declared external call. Effective Response Time may be inaccurate");
		} else {
			externalStartTimes.put(
					Thread.currentThread().getId(), System.currentTimeMillis());
		}
	}

	public static void endsExternalCall() {
		Long time = System.currentTimeMillis();
		Long threadId = Thread.currentThread().getId();
		Long externalCallStartTime = externalStartTimes
				.remove(threadId);
		if (externalCallStartTime == null) {
			logger.error("Declaring the end of an external call outside the scope of an external call."
					+ " Effective Response Time may be inaccurate");
		} else {
			Long currentExternalTime = externalTimes
					.get(threadId);
			externalTimes.put(threadId,
					currentExternalTime != null ? currentExternalTime + time
							- externalCallStartTime : time
							- externalCallStartTime);
		}
	}

}
