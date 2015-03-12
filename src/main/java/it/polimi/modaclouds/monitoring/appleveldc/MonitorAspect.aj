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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public aspect MonitorAspect {

	private final Logger logger = LoggerFactory.getLogger(MonitorAspect.class);

	private pointcut monitoredMethod(Monitor methodType) : execution(@Monitor * *(..)) && @annotation(methodType);

	before(Monitor methodType) : monitoredMethod(methodType) {
		logger.debug("Starting method: {}", methodType.type());
		Metric.notifyAllMethodStarts(methodType.type());
		
		/*
		Long threadId = Thread.currentThread().getId();
		Map<String, Long> startTimePerMethod = AppDataCollectorFactory.startTimesPerMethodPerThreadId
				.get(threadId);
		if (startTimePerMethod == null) {
			startTimePerMethod = new ConcurrentHashMap<String, Long>();
			AppDataCollectorFactory.startTimesPerMethodPerThreadId.put(
					threadId, startTimePerMethod);
		}
		startTimePerMethod.put(methodType.type(), System.currentTimeMillis());
		*/
	}

	after(Monitor methodType): monitoredMethod(methodType){
		logger.debug("Ending method: {}", methodType.type());
		Metric.notifyAllMethodEnds(methodType.type());
		
		/*
		long end = System.currentTimeMillis();
		Long threadId = Thread.currentThread().getId();
		long responseTime = end
				- AppDataCollectorFactory.startTimesPerMethodPerThreadId.get(
						threadId).remove(methodType.type());
		Long externalTime = AppDataCollectorFactory.externalTimes.get(threadId);

		if (AppDataCollectorFactory.startTimesPerMethodPerThreadId
				.get(threadId).isEmpty()) {
			AppDataCollectorFactory.startTimesPerMethodPerThreadId
					.remove(threadId);
			AppDataCollectorFactory.externalTimes.remove(threadId);
		}

		long effectiveResponseTime = responseTime
				- (externalTime != null ? externalTime : 0);
		logger.debug("Response Time for method {}: {}", methodType.type(),
				responseTime);
		logger.debug("Effective Response Time for method {}: {}",
				methodType.type(), effectiveResponseTime);

		AppDataCollectorFactory.collect(String.valueOf(responseTime),
				new ResponseTime(),
				AppDataCollectorFactory.getMethodId(methodType.type()));
		AppDataCollectorFactory.collect(String.valueOf(effectiveResponseTime),
				new EffectiveResponseTime(),
				AppDataCollectorFactory.getMethodId(methodType.type()));
				*/
	}

}
