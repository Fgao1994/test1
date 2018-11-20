package cn.edu.whu.openmi.smw.examples.simpleadding;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class RunOffComponentB extends LinkableEngine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RunOffComponentB(){
	}
	@Override
	protected void setEngineApiAccess() {
		this.engineApiAccess = new RunOffEngineB();
	}

}
