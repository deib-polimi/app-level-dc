package it.polimi.modaclouds.monitoring.appleveldc.metrics;

import it.polimi.modaclouds.monitoring.dcfactory.DCConfig;

import java.util.Set;

public abstract class Metric {

	public abstract DCConfig selectDC(Set<DCConfig> dcs);

	public String getName() {
		return getClass().getSimpleName();
	}

	public abstract boolean shouldSend(DCConfig dc);

}
