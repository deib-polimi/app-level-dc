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

	private static final Map<String, Integer> counterPerMethodId = new ConcurrentHashMap<String, Integer>();
	private static final Map<String, Timer> timerPerMethodId = new ConcurrentHashMap<String, Timer>();
	private static final Map<String, Long> samplingTimePerMethodId = new ConcurrentHashMap<String, Long>();

	private enum Parameter {
		samplingTime("60");

		private String defaultValue;

		private Parameter(String defaultValue) {
			this.defaultValue = defaultValue;
		}
	}

	@Override
	protected void methodStarts(String methodId) {
		//nothing to do
	}

	@Override
	protected void methodEnds(String methodId) {
		Integer count = counterPerMethodId.get(methodId);
		if (count!=null){
			counterPerMethodId.put(methodId, count+1);
		}
	}

	@Override
	protected void externalMethodStarts() {
		//nothing to do
	}

	@Override
	protected void externalMethodEnds() {
		//nothing to do
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
					counterPerMethodId.put(methodId, 0);
					Timer timer = new Timer();
					timerPerMethodId.put(methodId, timer);
					samplingTimePerMethodId.put(methodId, newSamplingTime);
					timer.scheduleAtFixedRate(new throughputSender(methodId),
							0, newSamplingTime);
				}
			} else {
				Timer timer = timerPerMethodId.remove(methodId);
				if (timer != null)
					timer.cancel();
				counterPerMethodId.remove(methodId);
			}
		}
	}

	private long getSamplingTime(String methodId) {
		Set<DCConfig> dcsConfigs = getConfiguration(methodId);
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

	private boolean shouldSend(String methodId) {
		Set<DCConfig> dcsConfigs = getConfiguration(methodId);
		return dcsConfigs != null && dcsConfigs.isEmpty();
	}

	private final class throughputSender extends TimerTask {
		private String methodId;

		public throughputSender(String methodId) {
			this.methodId = methodId;
		}

		@Override
		public void run() {
			send(Long.toString(counterPerMethodId.get(methodId)
					/ samplingTimePerMethodId.get(methodId)), methodId);
			counterPerMethodId.put(methodId, 0);
		}
	}

}
