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
package it.polimi.modaclouds.monitoring;

import java.net.MalformedURLException;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public aspect AspectResponseTimeMonitoring {
	
	private Logger logger = LoggerFactory.getLogger(AspectResponseTimeMonitoring.class
			.getName());
	private long start;
	private long end;
	long elapse;
	
	private pointcut startMethod(MonitoredMetric myAnnotation) : execution(@MonitoredMetric * *(..)) && @annotation(myAnnotation);	
	private pointcut endMethod(MonitoredMetric myAnnotation) : execution(@MonitoredMetric * *(..)) && @annotation(myAnnotation);	

	
	@SuppressAjWarnings({"adviceDidNotMatch"})
	before(MonitoredMetric myAnnotation) : startMethod(myAnnotation) {
		start = System.currentTimeMillis();
		logger.info("start time: " + start);
		
	}
		
	@SuppressAjWarnings({"adviceDidNotMatch"})
	after (MonitoredMetric myAnnotation): endMethod(myAnnotation){

		end =  System.currentTimeMillis();
		long elapse = end - start;
		logger.info("end Time: " + end );
		logger.info("Aspect elapsed time:" + elapse );
		try {
			AppDataCollector.getInstance().send(String.valueOf(elapse), "response-time", thisEnclosingJoinPointStaticPart.getSignature().getName().toString());
		} catch (MalformedURLException e) {
			logger.error("Error while sending data to the DDA", e);
		}

	}

}
