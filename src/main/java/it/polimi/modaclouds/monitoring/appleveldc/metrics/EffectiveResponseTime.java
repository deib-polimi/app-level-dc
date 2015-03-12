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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffectiveResponseTime extends Metric {

	private static final Logger logger = LoggerFactory
			.getLogger(EffectiveResponseTime.class);

	private static Map<Long, Map<String, Long>> startTimesPerMethodPerThreadId = new ConcurrentHashMap<Long, Map<String, Long>>();
	private static Map<Long, Long> externalStartTimes = new ConcurrentHashMap<Long, Long>();

	private enum Parameter {
		samplingProbability("1");

		private String defaultValue;

		private Parameter(String defaultValue) {
			this.defaultValue = defaultValue;
		}
	}

	private static double getSamplingProbability(Map<String, String> parameters) {
		String samplingProbability = parameters
				.get(Parameter.samplingProbability.name());
		if (samplingProbability == null)
			samplingProbability = Parameter.samplingProbability.defaultValue;
		return Double.valueOf(samplingProbability);
	}

	private Double selectSamplingProbability(Set<DCConfig> dcs) {
		DCConfig chosen = null;
		double biggerSP = 0;
		for (DCConfig dcConfig : dcs) {
			double samplingProbability = getSamplingProbability(dcConfig
					.getParameters());
			if (samplingProbability > biggerSP) {
				chosen = dcConfig;
				biggerSP = samplingProbability;
			}
		}
		Map<String, String> parameters = chosen.getParameters();
		return getSamplingProbability(parameters);
	}

	private boolean shouldSend(String methodId) {
		Set<DCConfig> dcsConfigs = getConfiguration(methodId);
		if (dcsConfigs == null || dcsConfigs.isEmpty())
			return false;
		Double samplingProbability = selectSamplingProbability(dcsConfigs);
		return (Math.random() < samplingProbability);
	}

	@Override
	protected void methodStarts(String methodId) {
		Long threadId = Thread.currentThread().getId();
		Map<String, Long> startTimePerMethod = startTimesPerMethodPerThreadId
				.get(threadId);
		if (startTimePerMethod == null) {
			startTimePerMethod = new ConcurrentHashMap<String, Long>();
			startTimesPerMethodPerThreadId.put(threadId, startTimePerMethod);
		}
		startTimePerMethod.put(methodId, System.currentTimeMillis());
	}

	@Override
	protected void methodEnds(String methodId) {
		long endTime = System.currentTimeMillis();
		Long threadId = Thread.currentThread().getId();
		long effectiveResponseTime = endTime
				- startTimesPerMethodPerThreadId.get(threadId).remove(methodId);
		if (startTimesPerMethodPerThreadId.get(threadId).isEmpty()) {
			startTimesPerMethodPerThreadId.remove(threadId);
		}

		logger.debug("Effective Response Time for method {}: {}", methodId,
				effectiveResponseTime);

		if (shouldSend(methodId)) {
			send(String.valueOf(effectiveResponseTime), methodId);
		}
	}

	@Override
	protected void externalMethodStarts() {
		long threadId = Thread.currentThread().getId();
		Long externalCallStartTime = externalStartTimes.get(threadId);
		if (externalCallStartTime != null) {
			logger.warn("The beginning of an external call was declared inside the "
					+ "scope of an external call. Effective Response Time may be inaccurate");
		} else {
			externalStartTimes.put(threadId, System.currentTimeMillis());
		}
	}

	@Override
	protected void externalMethodEnds() {
		Long endTime = System.currentTimeMillis();
		Long threadId = Thread.currentThread().getId();
		Long externalCallStartTime = externalStartTimes.remove(threadId);
		if (externalCallStartTime == null) {
			logger.error("Declaring the end of an external call outside the scope of an external call."
					+ " Effective Response Time may be inaccurate");
		} else {
			long externalTime = endTime - externalCallStartTime;
			Map<String, Long> startTimesPerMethod = startTimesPerMethodPerThreadId
					.get(threadId);
			if (startTimesPerMethod == null) {
				return;
			}
			for (Entry<String, Long> entry : startTimesPerMethod.entrySet()) {
				entry.setValue(entry.getValue() + externalTime);
			}
		}
	}

	@Override
	protected void syncedWithKB() {
		// Nothing to do
	}

}
