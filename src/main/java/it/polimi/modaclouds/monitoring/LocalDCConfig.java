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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LocalDCConfig {
	private static LocalDCConfig _instance = null;
	private static final Logger logger = LoggerFactory.getLogger(LocalDCConfig.class); 
	
	private Configuration config;
	
	private LocalDCConfig(){
		try {
			config = new PropertiesConfiguration("dc.properties");
		} catch (ConfigurationException e) {
			logger.error("Error while reading the configuration file", e);
		}
	}
			
	public static LocalDCConfig getInstance(){
		if(_instance==null)
			_instance=new LocalDCConfig();
		return _instance;
	}
	
	public String getComponentID(){
		return config.getString("component.id");
	}

	public String getComponentType() {
		return config.getString("component.type");
	}

	public long getRefreshingPeriod() {
		return config.getLong("refreshing.period");
	}

	public long getRefreshingDelay() {
		return config.getLong("refreshing.delay");
	}

	public String getDataCollectorID() {
		return config.getString("datacollector.id");
	}
	
	
	
}
