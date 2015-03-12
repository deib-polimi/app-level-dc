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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import it.polimi.modaclouds.monitoring.appleveldc.metrics.ResponseTime;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ResponseTimeTest {

	@Before
	public void setUp() throws ConfigurationException {
		Config config = mock(Config.class);
		Config.setInstance(config);
		when(config.getAppId()).thenReturn("whatever");
		when(config.getDdaUrl()).thenReturn("whatever");
		when(config.getKbUrl()).thenReturn("http://localhost:3030");
		when(config.isStartSyncingWithKB()).thenReturn(false);
		when(config.getKbSyncPeriod()).thenReturn(10);
		AppDataCollectorFactory.init();
	}

	@Test
	public void test() throws Exception {
		register();
	}

	@Monitor(type = "register")
	private void register() throws InterruptedException {
		Thread.sleep(1000);
		outgoingCall();
		AppDataCollectorFactory.startsExternalCall();
		unreachableOutgoingCall();
		AppDataCollectorFactory.endsExternalCall();
	}

	@Monitor(type = "outgoingCall")
	@ExternalCall
	private void outgoingCall() throws InterruptedException {
		Thread.sleep(1000);
	}

	@Monitor(type = "unreachableOutgoingCall")
	private void unreachableOutgoingCall() throws InterruptedException {
		Thread.sleep(1000);
	}

}
