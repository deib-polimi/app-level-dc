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

import it.polimi.modaclouds.monitoring.appleveldc.metrics.ResponseTime;
import it.polimi.modaclouds.monitoring.dcfactory.DCMetaData;
import it.polimi.modaclouds.monitoring.dcfactory.DataCollectorFactory;
import it.polimi.modaclouds.monitoring.dcfactory.ddaconnectors.DDAConnector;
import it.polimi.modaclouds.monitoring.dcfactory.ddaconnectors.RCSConnector;
import it.polimi.modaclouds.monitoring.dcfactory.kbconnectors.FusekiConnector;
import it.polimi.modaclouds.monitoring.dcfactory.kbconnectors.KBConnector;
import it.polimi.modaclouds.qos_models.util.Model;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AppDataCollectorFactory extends DataCollectorFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(AppDataCollectorFactory.class);

	private static AppDataCollectorFactory _INSTANCE = null;
	private static int kbSyncPeriod;
	private static String appId;
	private static String ddaURL;
	private static String kbURL;
	private static String mmURL;

	private static Config config;

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

		DDAConnector dda = new RCSConnector(ddaURL);
		KBConnector kb = new FusekiConnector(kbURL);
		_INSTANCE = new AppDataCollectorFactory(dda, kb);

		retrieveMonitoredMethods(monitoredPackagePrefix);

		logger.info(
				"{} initialized with:\n\tddaURL: {}\n\tkbURL: {}\n\tkbSyncPeriod: {}",
				AppDataCollectorFactory.class.getSimpleName(), ddaURL, kbURL,
				kbSyncPeriod);
	}

	private static void retrieveMonitoredMethods(String monitoredPackagePrefix) {
		Reflections reflections = new Reflections(monitoredPackagePrefix,
				new MethodAnnotationsScanner());
		Set<Method> methods = reflections
				.getMethodsAnnotatedWith(Monitor.class);
 		Set <it.polimi.modaclouds.qos_models.monitoring_ontology.Method> toSend = new HashSet<it.polimi.modaclouds.qos_models.monitoring_ontology.Method>();
		for (Method m : methods) {
			Monitor monitor = m.getAnnotation(Monitor.class);
			toSend.add(new it.polimi.modaclouds.qos_models.monitoring_ontology.Method(appId, monitor.name())); 
		}
		sendMethods(toSend);
		
		
	}

	private static void loadConfiguration() throws ConfigurationException {
		config = Config.getInstance();
		ddaURL = config.getDdaUrl();
		kbURL = config.getKbUrl();
		kbSyncPeriod = config.getKbSyncPeriod();
		appId = config.getAppId();
		mmURL = config.getMmUrl();	
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

	private AppDataCollectorFactory(DDAConnector dda, KBConnector kb) {
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
		DCMetaData dc = getDataCollector(monitoredResourceId, metric);
		if (dc != null) {
			Map<String,String> parameters = dc.getParameters();
			double samplingProbability = ResponseTime.getSamplingProbability(parameters);
			if( Math.random() < samplingProbability )
				sendAsyncMonitoringDatum(value, metric, monitoredResourceId);
		}
	}

	public void startSyncingWithKB() {
		startSyncingWithKB(kbSyncPeriod);
	}
	
	private static void sendMethods(Set<it.polimi.modaclouds.qos_models.monitoring_ontology.Method> methods){
		Model update = new Model();
		update.setMethods(methods);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(update);
		int result;
		do{
			result = HttpRequest.get(mmURL+"/v1/model/resources/"+appId).code();
			try {
				Thread.sleep(120000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		while(result!=200);
		
		result = HttpRequest.put(config.getMmUrl()).send(json).code();
		
		}

}
