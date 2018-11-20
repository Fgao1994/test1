package cn.edu.whu.websocket;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class WSTopModel extends LinkableEngine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WSTopModel() {
		super();
		this.setEngineApiAccess();
	}

	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new WSTopModelEngine();
	}

	// add by zmd,2015-03-11
	public double getTimeStep() {
		return ((WSTopModelEngine) (this.engineApiAccess)).getTimeStep();
	}
}
