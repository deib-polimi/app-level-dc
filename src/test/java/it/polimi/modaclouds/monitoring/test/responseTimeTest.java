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
package it.polimi.modaclouds.monitoring.test;

import it.polimi.modaclouds.monitoring.MonitoredMetric;

import org.junit.Before;
import org.junit.Test;

public class responseTimeTest {
	
	private FakeServlet fakeServlet;
	
	@Before
	public void init() {
		fakeServlet = new FakeServlet();
	}

	@Test
	public void test() {
		fakeServlet.login();		
	}
	
	public class FakeServlet {

		@MonitoredMetric("response-time")
		public void login() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


}
