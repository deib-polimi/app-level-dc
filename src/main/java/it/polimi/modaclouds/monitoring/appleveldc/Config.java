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

import org.apache.commons.validator.routines.UrlValidator;

public class Config {

	private static Config _instance = null;
	private UrlValidator validator;
	private String ddaIP;
	private String ddaPort;
	private String kbIP;
	private String kbPort;
	private String kbPath;
	private String ddaUrl;
	private String kbUrl;
	private String mmUrl;
	private int kbSyncPeriod;
	private String appId;
	private String mmIP;
	private String mmPort;

	public static Config getInstance() throws ConfigurationException {
		if (_instance == null)
			_instance = new Config();
		return _instance;
	}

	private Config() throws ConfigurationException {
		validator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
		ddaIP = getMandatoryEnvVar(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_IP);
		ddaPort = getMandatoryEnvVar(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT);
		kbIP = getMandatoryEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP);
		kbPort = getMandatoryEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT);
		kbPath = getMandatoryEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH);
		String kbSyncPeriodString = getOptionalEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_SYNC_PERIOD);
		appId = getMandatoryEnvVar(Env.MODACLOUDS_MONITORED_APP_ID);
		mmIP = getMandatoryEnvVar (Env.MODACLOUDS_MONITORING_MANAGER_ENDPOINT_IP);
		mmPort = getMandatoryEnvVar (Env.MODACLOUDS_MONITORING_MANAGER_ENDPOINT_PORT);
		

		ddaUrl = "http://" + ddaIP + ":" + ddaPort;
		kbUrl = "http://" + kbIP + ":" + kbPort + kbPath;
		mmUrl = "http://" + mmIP + ":" + mmPort;

		if (!validator.isValid(ddaUrl))
			throw new ConfigurationException(ddaUrl + " is not a valid URL");
		if (!validator.isValid(kbUrl))
			throw new ConfigurationException(kbUrl + " is not a valid URL");

		try {
			kbSyncPeriod = Integer.parseInt(kbSyncPeriodString);
		} catch (NumberFormatException e) {
			throw new ConfigurationException(kbSyncPeriodString
					+ " is not a valid value for "
					+ Env.MODACLOUDS_KNOWLEDGEBASE_SYNC_PERIOD);
		}
	}

	public String getDdaUrl() {
		return ddaUrl;
	}

	public String getKbUrl() {
		return kbUrl;
	}

	public int getKbSyncPeriod() {
		return kbSyncPeriod;
	}

	public String getAppId() {
		return appId;
	}
	
	public String getMmUrl(){
		return mmUrl;
	}
	
	public String getMmPort() {
		return mmPort;
	}

	private String getMandatoryEnvVar(String varName)
			throws ConfigurationException {
		String var = System.getProperty(varName);
		if (var == null) {
			var = System.getenv(varName);
		}
		if (var == null) {
			throw new ConfigurationException(varName
					+ " variable was not defined");
		}
		return var;
	}

	private String getOptionalEnvVar(String varName) {
		String var = System.getProperty(varName);
		if (var == null) {
			var = System.getenv(varName);
		}
		if (var == null) {
			var = getDefaultValue(Env.MODACLOUDS_KNOWLEDGEBASE_SYNC_PERIOD);
		}
		return var;
	}

	private String getDefaultValue(String varName) {
		switch (varName) {
		case Env.MODACLOUDS_KNOWLEDGEBASE_SYNC_PERIOD:
			return "10";
		default:
			return "";
		}
	}

}
