package it.polimi.modaclouds.monitoring.appleveldc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class ResponseTimeTest {

	@Before
	public void setUp() {
		Config config = mock(Config.class);
		Config.setInstance(config);
		when(config.getAppId()).thenReturn("whatever");
		when(config.getDdaUrl()).thenReturn("whatever");
		when(config.getKbUrl()).thenReturn("http://localhost:3030");
		when(config.getKbSyncPeriod()).thenReturn(10);
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

	@ExternalCall
	private void outgoingCall() throws InterruptedException {
		Thread.sleep(1000);
	}

	private void unreachableOutgoingCall() throws InterruptedException {
		Thread.sleep(1000);
	}

}
