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
package it.polimi.modaclouds.monitoring.appleveldc.metrics;

import it.polimi.modaclouds.monitoring.appleveldc.Metric;
import it.polimi.modaclouds.monitoring.dcfactory.DCConfig;
import it.polimi.modaclouds.qos_models.monitoring_ontology.InternalComponent;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Resource;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Throughput extends Metric {

	private static final Logger logger = LoggerFactory
			.getLogger(Throughput.class);

	private static final Map<String, Integer> counterPerMethodId4Methods = new ConcurrentHashMap<String, Integer>();
	private static int counter4App;
	private static final Map<String, Timer> timerPerMethodId = new ConcurrentHashMap<String, Timer>();
	private static final Map<String, Long> samplingTimePerMethodId = new ConcurrentHashMap<String, Long>();

	private enum Parameter {
		samplingTime("60");

		private String defaultValue;

		private Parameter(String defaultValue) {
			this.defaultValue = defaultValue;
		}
	}

	private Timer appTimer;

	private long appSamplingTime;

	@Override
	protected void methodStarts(Method method) {
		// nothing to do
	}

	@Override
	protected void methodEnds(Method method) {
		Integer count = counterPerMethodId4Methods.get(method.getId());
		if (count != null) {
			counterPerMethodId4Methods.put(method.getId(), count + 1);
		}
		counter4App++;
	}

	@Override
	protected void externalMethodStarts() {
		// nothing to do
	}

	@Override
	protected void externalMethodEnds() {
		// nothing to do
	}

	@Override
	protected void syncedWithKB() {
		Set<Method> methods = getMonitoredMethods();
		for (Method method : methods) {
			if (shouldSend(method)) {
				long newSamplingTime = getSamplingTime(method);
				if (timerPerMethodId.containsKey(method.getId())
						&& samplingTimePerMethodId.get(method.getId()) != newSamplingTime) {
					timerPerMethodId.remove(method.getId()).cancel();
				}
				if (!timerPerMethodId.containsKey(method.getId())) {
					counterPerMethodId4Methods.put(method.getId(), 0);
					Timer timer = new Timer();
					timerPerMethodId.put(method.getId(), timer);
					samplingTimePerMethodId
							.put(method.getId(), newSamplingTime);
					timer.scheduleAtFixedRate(new ThroughputSender4Methods(
							method), 0, newSamplingTime * 1000);
				}
			} else {
				Timer timer = timerPerMethodId.remove(method.getId());
				if (timer != null)
					timer.cancel();
				counterPerMethodId4Methods.remove(method.getId());
			}
		}
		InternalComponent app = getMonitoredApp();
		if (app != null && shouldSend(app)) {
			long newSamplingTime = getSamplingTime(getMonitoredApp());
			if (appTimer != null && appSamplingTime != newSamplingTime) {
				appTimer.cancel();
				appTimer = null;
			}
			if (appTimer == null) {
				counter4App = 0;
				appTimer = new Timer();
				appSamplingTime = newSamplingTime;
				appTimer.scheduleAtFixedRate(new ThroughputSender4App(
						getMonitoredApp()), 0, newSamplingTime * 1000);
			}
		} else {
			if (appTimer != null)
				appTimer.cancel();
			appTimer = null;
		}
	}

	private long getSamplingTime(Resource resource) {
		Set<DCConfig> dcsConfigs = getConfiguration(resource);
		return selectSamplingTime(dcsConfigs);
	}

	private static int getSamplingTime(Map<String, String> parameters) {
		String samplingTime = parameters.get(Parameter.samplingTime.name());
		if (!isValidSamplingTime(samplingTime)) {
			logger.warn(
					"{} is not a valid sampling time. "
							+ "A valid sampling time should be an integer equal to or greater than 10. "
							+ "The default value {} will be used",
					samplingTime, Parameter.samplingTime.defaultValue);
			samplingTime = Parameter.samplingTime.defaultValue;
		}
		return Integer.valueOf(samplingTime);
	}

	private static boolean isValidSamplingTime(String samplingTime) {
		return isInteger(samplingTime) && Integer.valueOf(samplingTime) >= 10;
	}

	private int selectSamplingTime(Set<DCConfig> dcs) {
		int smallerST = Integer.MAX_VALUE;
		for (DCConfig dcConfig : dcs) {
			int samplingTime = getSamplingTime(dcConfig.getParameters());
			if (samplingTime < smallerST) {
				smallerST = samplingTime;
			}
		}
		return smallerST;
	}

	private boolean shouldSend(Resource resource) {
		Set<DCConfig> dcsConfigs = getConfiguration(resource);
		return dcsConfigs != null && !dcsConfigs.isEmpty();
	}

	private final class ThroughputSender4Methods extends TimerTask {
		private Resource resource;

		public ThroughputSender4Methods(Resource resource) {
			this.resource = resource;
		}

		@Override
		public void run() {
			send(Double.toString((double) counterPerMethodId4Methods
					.get(resource.getId())
					/ samplingTimePerMethodId.get(resource.getId())), resource);
			counterPerMethodId4Methods.put(resource.getId(), 0);
		}
	}

	private final class ThroughputSender4App extends TimerTask {
		private Resource app;

		public ThroughputSender4App(Resource app) {
			this.app = app;
		}

		@Override
		public void run() {
			send(Double.toString((double) counter4App / appSamplingTime), app);
			counter4App = 0;
		}
	}

}
