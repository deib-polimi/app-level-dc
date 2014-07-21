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

	public static String getDDAURL() throws ConfigurationException {
		String ddaIP = getMandatoryEnvVar(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_IP);
		String ddaPort = getMandatoryEnvVar(Env.MODACLOUDS_MONITORING_DDA_ENDPOINT_PORT);
		String ddaUrl = "http://" + ddaIP + ":" + ddaPort;
		UrlValidator validator = new UrlValidator();
		if (!validator.isValid(ddaUrl))
			throw new ConfigurationException(ddaUrl + " is not a valid URL");
		return ddaUrl.toString();
	}

	public static String getKBURL() throws ConfigurationException {
		String kbIP = getMandatoryEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_IP);
		String kbPort = getMandatoryEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_ENDPOINT_PORT);
		String kbPath = getMandatoryEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_DATASET_PATH);
		String kbUrl = "http://" + kbIP + ":" + kbPort + kbPath;
		UrlValidator validator = new UrlValidator();
		if (!validator.isValid(kbUrl))
			throw new ConfigurationException(kbUrl + " is not a valid URL");
		return kbUrl.toString();
	}

	private static String getMandatoryEnvVar(String varName)
			throws ConfigurationException {
		String var = System.getenv(varName);
		if (var == null)
			throw new ConfigurationException(varName
					+ " variable was not defined");
		return var;
	}

	public static int getKBSyncPeriod() throws ConfigurationException {
		String kbSyncPeriodString = getOptionalEnvVar(Env.MODACLOUDS_KNOWLEDGEBASE_SYNC_PERIOD);
		try {
			return Integer.parseInt(kbSyncPeriodString);
		} catch (NumberFormatException e) {
			throw new ConfigurationException(kbSyncPeriodString
					+ " is not a valid value for "
					+ Env.MODACLOUDS_KNOWLEDGEBASE_SYNC_PERIOD);
		}
	}

	private static String getOptionalEnvVar(String varName) {
		String var = System.getenv(varName);
		if (var == null) {
			var = getDefaultValue(Env.MODACLOUDS_KNOWLEDGEBASE_SYNC_PERIOD);
		}
		return var;
	}

	private static String getDefaultValue(String varName) {
		switch (varName) {
		case Env.MODACLOUDS_KNOWLEDGEBASE_SYNC_PERIOD:
			return "10";
		default:
			return "";
		}
	}

	public static String getAppId() throws ConfigurationException {
		return getMandatoryEnvVar(Env.MODACLOUDS_MONITORED_APP_ID);
	}
}
