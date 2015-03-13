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
	protected void methodStarts(String methodId) {
		// nothing to do
	}

	@Override
	protected void methodEnds(String methodId) {
		Integer count = counterPerMethodId4Methods.get(methodId);
		if (count != null) {
			counterPerMethodId4Methods.put(methodId, count + 1);
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
		Set<String> methodsIds = getMonitoredMethodsIds();
		for (String methodId : methodsIds) {
			if (shouldSend(methodId)) {
				long newSamplingTime = getSamplingTime(methodId);
				if (timerPerMethodId.containsKey(methodId)
						&& samplingTimePerMethodId.get(methodId) != newSamplingTime) {
					timerPerMethodId.remove(methodId).cancel();
				}
				if (!timerPerMethodId.containsKey(methodId)) {
					counterPerMethodId4Methods.put(methodId, 0);
					Timer timer = new Timer();
					timerPerMethodId.put(methodId, timer);
					samplingTimePerMethodId.put(methodId, newSamplingTime);
					timer.scheduleAtFixedRate(new ThroughputSender4Methods(
							methodId), 0, newSamplingTime);
				}
			} else {
				Timer timer = timerPerMethodId.remove(methodId);
				if (timer != null)
					timer.cancel();
				counterPerMethodId4Methods.remove(methodId);
			}
		}
		if (shouldSend(getAppId())) {
			long newSamplingTime = getSamplingTime(getAppId());
			if (appTimer != null && appSamplingTime != newSamplingTime) {
				appTimer.cancel();
				appTimer = null;
			}
			if (appTimer == null) {
				counter4App = 0;
				appTimer = new Timer();
				appSamplingTime = newSamplingTime;
				appTimer.scheduleAtFixedRate(new ThroughputSender4App(
						getAppId()), 0, newSamplingTime);
			}
		} else {
			if (appTimer != null)
				appTimer.cancel();
			appTimer = null;
		}
	}

	private long getSamplingTime(String resourceId) {
		Set<DCConfig> dcsConfigs = getConfiguration(resourceId);
		return selectSamplingTime(dcsConfigs);
	}

	private static int getSamplingTime(Map<String, String> parameters) {
		String samplingTime = parameters.get(Parameter.samplingTime.name());
		if (!isValidSamplingTime(samplingTime)) {
			logger.warn(
					"{} is not a valid sampling time. "
							+ "A valid sampling time should be an integer greater than 10. "
							+ "The default value {} will be used",
					samplingTime, Parameter.samplingTime.defaultValue);
			samplingTime = Parameter.samplingTime.defaultValue;
		}
		return Integer.valueOf(samplingTime);
	}

	private static boolean isValidSamplingTime(String samplingTime) {
		return isInteger(samplingTime) && Integer.valueOf(samplingTime) > 10;
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

	private boolean shouldSend(String resourceId) {
		Set<DCConfig> dcsConfigs = getConfiguration(resourceId);
		return dcsConfigs != null && dcsConfigs.isEmpty();
	}

	private final class ThroughputSender4Methods extends TimerTask {
		private String methodId;

		public ThroughputSender4Methods(String methodId) {
			this.methodId = methodId;
		}

		@Override
		public void run() {
			send(Long.toString(counterPerMethodId4Methods.get(methodId)
					/ samplingTimePerMethodId.get(methodId)), methodId);
			counterPerMethodId4Methods.put(methodId, 0);
		}
	}

	private final class ThroughputSender4App extends TimerTask {
		private String appId;

		public ThroughputSender4App(String appId) {
			this.appId = appId;
		}

		@Override
		public void run() {
			send(Long.toString(counter4App / appSamplingTime), appId);
			counter4App = 0;
		}
	}

}
