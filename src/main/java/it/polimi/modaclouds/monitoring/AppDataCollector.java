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
import it.polimi.modaclouds.monitoring.kb.api.KBConnector;
import it.polimi.modaclouds.qos_models.monitoring_ontology.DCFactory;
import it.polimi.modaclouds.qos_models.monitoring_ontology.DataCollector;
import it.polimi.modaclouds.qos_models.monitoring_ontology.MonitorableResource;

import java.io.FileNotFoundException;
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
	private KBConnector kbConnector;
	private long refreshingPeriod;
	private long refreshingDelay;
	private String dcFactoryURI;
	private DCFactory dcFactory;
	private String targetURI;

	public String getDcFactoryURI() {
		return dcFactoryURI;
	}

	public void setDcFactoryURI(String dcFactoryURI) {
		this.dcFactoryURI = dcFactoryURI;
	}

	private static AppDataCollector _instance = null;

	public static AppDataCollector getInstance() throws MalformedURLException,
			FileNotFoundException {
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

	public AppDataCollector() throws MalformedURLException,
			FileNotFoundException {
		ddaConnector = DDAConnector.getInstance();
		kbConnector = KBConnector.getInstance();
		startRefreshing();
	}

	private void startRefreshing() {
		refreshingThreadHandler = scheduler.scheduleAtFixedRate(configLoader,
				refreshingDelay, refreshingPeriod, TimeUnit.SECONDS);
	}

	protected void refresh() {
		DCFactory newDCFactory = (DCFactory) kbConnector.get(dcFactoryURI);
		dcFactory = newDCFactory;
	}

	protected void send(String value, String metric, String methodName) {
		logger.info(value + " " + metric + " " + methodName);
		for (DataCollector dc : dcFactory.getInstantiatedDCs()) {
			String methodURI = targetURI + "/" + methodName;
			if (dc.getCollectedMetric().equals(metric)
					&& hasTargetMethod(methodURI, dc)) {
				ddaConnector.sendAsyncMonitoringDatum(value, metric, methodURI);
			}
		}
	}

	private boolean hasTargetMethod(String methodURI, DataCollector dc) {
		for (MonitorableResource target : dc.getTargetResources()) {
			if (target.getUri().equals(methodURI))
				return true;
		}
		return false;
	}

	public String getTargetURI() {
		return targetURI;
	}

	public void setTargetURI(String targetURI) {
		this.targetURI = targetURI;
	}

}
