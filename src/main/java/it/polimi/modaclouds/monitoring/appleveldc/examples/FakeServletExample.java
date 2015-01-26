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
package it.polimi.modaclouds.monitoring.appleveldc.examples;

import it.polimi.modaclouds.monitoring.appleveldc.AppDataCollectorFactory;
import it.polimi.modaclouds.monitoring.appleveldc.Monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Before launching the example, start the KB, the DDA and the MM. In order to
 * see the result: register a rule with outputMetric action, and attach an
 * observer to the metric.
 * 
 * @author miglie
 * 
 */
public class FakeServletExample {

	private static final Logger logger = LoggerFactory
			.getLogger(FakeServletExample.class);

	public static void main(String[] args) {
		try {
			AppDataCollectorFactory.initialize(FakeServlet.class.getPackage()
					.getName());
			AppDataCollectorFactory.getInstance().startSyncingWithKB();
			;
			new Thread(new Runnable() {

				@Override
				public void run() {
					FakeServlet fakeServlet = new FakeServlet();
					while (true) {
						fakeServlet.login();
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
						}
					}
				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class FakeServlet {

		@Monitor(name = "register")
		public void login() {
			logger.info("registering...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			logger.info("registered!");
		}

	}

}
