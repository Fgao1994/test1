package cn.edu.whu.openmi.smw.examples.simpleadding;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class RunOffComponentC extends LinkableEngine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RunOffComponentC(){
	}
	@Override
	protected void setEngineApiAccess() {
		this.engineApiAccess = new RunOffEngineC();
	}

	public double getTimeStep(){
		return ((RunOffEngineC)this.engineApiAccess).getTimeStep();
	}
}
