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
package it.polimi.modaclouds.monitoring;

import it.polimi.modaclouds.monitoring.ddaapi.DDAConnector;
import it.polimi.modaclouds.monitoring.ddaapi.ValidationErrorException;
import it.polimi.modaclouds.monitoring.objectstoreapi.ObjectStoreConnector;
import it.polimi.modaclouds.monitoring.objectstoreapi.dto.ComponentConfig;
import it.polimi.modaclouds.monitoring.objectstoreapi.dto.DataCollectorConfig;

import java.net.MalformedURLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppDataCollector {

	private Logger logger = LoggerFactory.getLogger(AppDataCollector.class
			.getName());

	private final ScheduledExecutorService scheduler = Executors
			.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> refreshingThreadHandler;

	private DDAConnector ddaConnector;
	private ObjectStoreConnector objectStoreConnector;
	private String componentType;
	private String componentID;
	private String dataCollectorID;
	private long refreshingPeriod;
	private long refreshingDelay;
	private LocalDCConfig localDCConfig = LocalDCConfig.getInstance();

	private ComponentConfig componentConfig;
	private DataCollectorConfig dataCollectorConfig;
	
	private static AppDataCollector _instance = null;

	public static AppDataCollector getInstance() throws MalformedURLException {
		if (_instance == null) {
			_instance = new AppDataCollector();
		}
		return _instance;
	}

	final Runnable configLoader = new Runnable() {
		public void run() {
			refresh();
		}
	};

	public AppDataCollector() throws MalformedURLException {
		ddaConnector = DDAConnector.getInstance();
		objectStoreConnector = ObjectStoreConnector.getInstance();
		componentType = localDCConfig.getComponentType();
		componentID = localDCConfig.getComponentID();
		dataCollectorID = localDCConfig.getDataCollectorID();
		refreshingPeriod = localDCConfig.getRefreshingPeriod();
		refreshingDelay = localDCConfig.getRefreshingDelay();
		startRefreshing();
	}

	private void startRefreshing() {
		refreshingThreadHandler = scheduler.scheduleAtFixedRate(configLoader,
				refreshingDelay, refreshingPeriod, TimeUnit.SECONDS);
	}

	protected void refresh() {
		ComponentConfig newComponentConfig = objectStoreConnector
				.getComponentConfig(componentID);
		if (newComponentConfig.getVersion() > this.componentConfig.getVersion())
			this.componentConfig = newComponentConfig;

		DataCollectorConfig newDataCollectorConfig = objectStoreConnector
				.getDataCollectorConfig(dataCollectorID);
		if (newDataCollectorConfig.getVersion() > this.dataCollectorConfig
				.getVersion())
			this.dataCollectorConfig = newDataCollectorConfig;
	}

	protected void send(String value, String metric, String methodName) {
		logger.info(value+metric+methodName);
//		if (dataCollectorConfig.isEnabled()) {
//			try {
//				ddaConnector.sendAsyncMonitoringDatum(value, metric,
//						componentConfig.getMethodsIDs().get(methodName));
//			} catch (ValidationErrorException e) {
//				logger.error("Invalid resource id, resource id must be a valid UUID: "
//						+ e.getMessage());
//			}
//		}
	}

}
