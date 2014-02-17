/**
 * Copyright 2013 deib-polimi
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

import it.polimi.modaclouds.monitoring.ddaapi.DDAConnector;
import it.polimi.modaclouds.monitoring.ddaapi.ValidationErrorException;
import it.polimi.modaclouds.monitoring.objectstoreapi.ObjectStoreConnector;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppDataCollector {

	private DDAConnector ddaConnector;
	private ObjectStoreConnector objectStoreConnector;
	private Map<String, String> operationsIDs;
	private String artefactType;
	private String artefactID;
	
	private Logger logger = LoggerFactory.getLogger(AppDataCollector.class.getName());
	private Config config = Config.getInstance();

	public AppDataCollector() throws MalformedURLException {
		ddaConnector = DDAConnector.getInstance();
		objectStoreConnector = ObjectStoreConnector.getInstance();
		artefactType = config.getArtefactType();
		artefactID = config.getArtefactID();
		initFromObjectStore();
	}

	private void initFromObjectStore() {
		operationsIDs = new HashMap<String, String>();
		for (String operationName: objectStoreConnector.getOperationsNames(artefactType)) {
			operationsIDs.put(operationName, objectStoreConnector.getOperationID(artefactID,operationName));
		}
	}

	protected void send(String value, String metric, String methodName) {
		try {
			ddaConnector.sendAsyncMonitoringDatum(value, metric, operationsIDs.get(methodName));
		} catch (ValidationErrorException e) {
			logger.error("Invalid resource id, resource id must be a valid UUID: " + e.getMessage());
		}
	}
}
