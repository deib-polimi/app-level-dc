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

import it.polimi.modaclouds.monitoring.appleveldc.AppDataCollectorFactory;
import it.polimi.modaclouds.monitoring.appleveldc.Metric;
import it.polimi.modaclouds.monitoring.dcfactory.DCConfig;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseTime extends Metric {

	private static final Logger logger = LoggerFactory
			.getLogger(ResponseTime.class);

	private static Map<Long, Map<String, Long>> startTimesPerMethodPerThreadId = new ConcurrentHashMap<Long, Map<String, Long>>();

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

	@Override
	protected DCConfig selectDC(Set<DCConfig> dcs) {
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
		return chosen;
	}

	@Override
	protected boolean shouldSend(DCConfig dc) {
		Map<String, String> parameters = dc.getParameters();
		double samplingProbability = getSamplingProbability(parameters);
		return (Math.random() < samplingProbability);
	}

	@Override
	protected void methodStarts(String type) {
		Long threadId = Thread.currentThread().getId();
		Map<String, Long> startTimePerMethod = startTimesPerMethodPerThreadId
				.get(threadId);
		if (startTimePerMethod == null) {
			startTimePerMethod = new ConcurrentHashMap<String, Long>();
			startTimesPerMethodPerThreadId.put(threadId, startTimePerMethod);
		}
		startTimePerMethod.put(type, System.currentTimeMillis());
	}

	@Override
	protected void methodEnds(String type) {
		long endTime = System.currentTimeMillis();
		Long threadId = Thread.currentThread().getId();
		long responseTime = endTime
				- startTimesPerMethodPerThreadId.get(threadId).remove(type);
		if (startTimesPerMethodPerThreadId.get(threadId).isEmpty()) {
			startTimesPerMethodPerThreadId.remove(threadId);
		}

		logger.debug("Response Time for method {}: {}", type, responseTime);

		collect(String.valueOf(responseTime),
				AppDataCollectorFactory.getMethodId(type));
	}

	@Override
	protected void externalMethodStarts() {
		// Nothing to do

	}

	@Override
	protected void externalMethodEnds() {
		// Nothing to do

	}

	@Override
	protected void syncedWithKB() {
		// Nothing to do
	}

}
