package cn.edu.whu.wssync;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class WSSyncTopModel extends LinkableEngine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WSSyncTopModel() {
		super();
		this.setEngineApiAccess();
	}

	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		if (this.engineApiAccess == null) {
			this.engineApiAccess = new WSSyncTopModelEngine();
		}
	}

	// add by zmd,2015-03-11
	public double getTimeStep() {
		return ((WSSyncTopModelEngine) (this.engineApiAccess)).getTimeStep();
	}
}
