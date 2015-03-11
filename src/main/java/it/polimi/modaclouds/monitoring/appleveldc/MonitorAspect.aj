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

import it.polimi.modaclouds.monitoring.appleveldc.metrics.EffectiveResponseTime;
import it.polimi.modaclouds.monitoring.appleveldc.metrics.ResponseTime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public aspect MonitorAspect {

	private static Map<Long, Long> startTimes = new ConcurrentHashMap<Long, Long>();
	static Map<Long, Long> externalStartTimes = new ConcurrentHashMap<Long, Long>();
	static Map<Long, Long> externalTimes = new ConcurrentHashMap<Long, Long>();

	private final Logger logger = LoggerFactory.getLogger(MonitorAspect.class);

	private pointcut monitoredMethod(Monitor methodType) : execution(@Monitor * *(..)) && @annotation(methodType);

	before(Monitor methodType) : monitoredMethod(methodType) {
		logger.debug("Executing monitored method \"{}\"", methodType.type());
		startTimes.put(Thread.currentThread().getId(),
				System.currentTimeMillis());
	}

	after(Monitor methodType): monitoredMethod(methodType){
		long end = System.currentTimeMillis();
		long responseTime = end
				- startTimes.remove(Thread.currentThread().getId());
		Long externalTime = externalTimes
				.remove(Thread.currentThread().getId());
		long effectiveResponseTime = responseTime
				- (externalTime != null ? externalTime : 0);
		logger.debug("Response Time:" + responseTime);
		logger.debug("Effective Response Time:" + effectiveResponseTime);

		AppDataCollectorFactory.collect(String.valueOf(responseTime),
				new ResponseTime(),
				AppDataCollectorFactory.getMethodId(methodType.type()));
		AppDataCollectorFactory.collect(String.valueOf(effectiveResponseTime),
				new EffectiveResponseTime(),
				AppDataCollectorFactory.getMethodId(methodType.type()));
	}

}
