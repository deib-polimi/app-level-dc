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
import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;
import it.polimi.modaclouds.qos_models.monitoring_ontology.Resource;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseTime extends Metric {

	private static final Logger logger = LoggerFactory
			.getLogger(ResponseTime.class);

	private static Map<Long, Map<String, Long>> startTimesPerMethodIdPerThreadId = new ConcurrentHashMap<Long, Map<String, Long>>();

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
		if (!isValidSamplingProbability(samplingProbability)) {
			logger.warn(
					"{} is not a valid sampling probability. "
							+ "A valid sampling probability should be a double between 0 and 1. "
							+ "The default value {} will be used",
					samplingProbability,
					Parameter.samplingProbability.defaultValue);
			samplingProbability = Parameter.samplingProbability.defaultValue;
		}
		return Double.valueOf(samplingProbability);
	}

	private static boolean isValidSamplingProbability(String samplingProbability) {
		return isDouble(samplingProbability)
				&& Math.abs(Double.valueOf(samplingProbability) - 0.5) <= 0.5;
	}

	private Double selectSamplingProbability(Set<DCConfig> dcs) {
		double biggerSP = 0;
		for (DCConfig dcConfig : dcs) {
			double samplingProbability = getSamplingProbability(dcConfig
					.getParameters());
			if (samplingProbability > biggerSP) {
				biggerSP = samplingProbability;
			}
		}
		return biggerSP;
	}

	private boolean shouldSend(Resource resource) {
		Set<DCConfig> dcsConfigs = getConfiguration(resource);
		if (dcsConfigs == null || dcsConfigs.isEmpty())
			return false;
		Double samplingProbability = selectSamplingProbability(dcsConfigs);
		return (Math.random() < samplingProbability);
	}

	@Override
	protected void methodStarts(Method method) {
		Long threadId = Thread.currentThread().getId();
		Map<String, Long> startTimePerMethodId = startTimesPerMethodIdPerThreadId
				.get(threadId);
		if (startTimePerMethodId == null) {
			startTimePerMethodId = new ConcurrentHashMap<String, Long>();
			startTimesPerMethodIdPerThreadId.put(threadId, startTimePerMethodId);
		}
		startTimePerMethodId.put(method.getId(), System.currentTimeMillis());
	}

	@Override
	protected void methodEnds(Method method) {
		long endTime = System.currentTimeMillis();
		Long threadId = Thread.currentThread().getId();
		long responseTime = endTime
				- startTimesPerMethodIdPerThreadId.get(threadId).remove(method.getId());
		if (startTimesPerMethodIdPerThreadId.get(threadId).isEmpty()) {
			startTimesPerMethodIdPerThreadId.remove(threadId);
		}

		logger.debug("Response Time for method {}: {}", method.getId(),
				responseTime);
		if (shouldSend(method)) {
			send(String.valueOf(responseTime), method);
		}
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
