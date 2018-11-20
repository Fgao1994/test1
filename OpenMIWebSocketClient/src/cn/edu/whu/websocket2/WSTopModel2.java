package cn.edu.whu.websocket2;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class WSTopModel2 extends LinkableEngine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WSTopModel2() {
		super();
		this.setEngineApiAccess();
	}

	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		if (this.engineApiAccess == null) {
			this.engineApiAccess = new WSTopModelEngine2();
		}
	}

	// add by zmd,2015-03-11
	public double getTimeStep() {
		return ((WSTopModelEngine2) (this.engineApiAccess)).getTimeStep();
	}
}
