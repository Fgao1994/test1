package cn.edu.whu.openmi.smw.examples.simpleadding;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class RunOffComponentA extends LinkableEngine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RunOffComponentA(){
	}
	@Override
	protected void setEngineApiAccess() {
		this.engineApiAccess = new RunOffEngineA();
	}

	public double getTimeStep(){
		return ((RunOffEngineA)this.engineApiAccess).getTimeStep();
	}
}
