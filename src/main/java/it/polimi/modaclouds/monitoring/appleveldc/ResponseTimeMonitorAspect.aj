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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public aspect ResponseTimeMonitorAspect {

	private final Logger logger = LoggerFactory
			.getLogger(ResponseTimeMonitorAspect.class);

	private long start;
	private long end;

	private pointcut monitoredMethod(Monitor methodType) : execution(@Monitor * *(..)) && @annotation(methodType);

	before(Monitor methodType) : monitoredMethod(methodType) {
		logger.debug("Executing monitored method \"{}\"", methodType.name());
		start = System.currentTimeMillis();
	}

	after(Monitor methodType): monitoredMethod(methodType){
		end = System.currentTimeMillis();
		long elapsed = end - start;
		logger.debug("Response time:" + elapsed);
		if (AppDataCollectorFactory.getInstance() != null) {
			AppDataCollectorFactory.getInstance().collect(
					String.valueOf(elapsed), ResponseTime.id,
					AppDataCollectorFactory.getMethodId(methodType.name()));
		}
	}

}