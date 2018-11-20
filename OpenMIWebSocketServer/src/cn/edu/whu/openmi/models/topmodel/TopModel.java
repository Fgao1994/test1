package cn.edu.whu.openmi.models.topmodel;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class TopModel extends LinkableEngine{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TopModel(){
		super();
		this.setEngineApiAccess();
	}
	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new TopModelEngine();
		((TopModelEngine)this.engineApiAccess).setBufferInfos(this.bufferInfos);
	}

	//add by zmd,2015-03-11
	public double getTimeStep(){
		return ((TopModelEngine)(this.engineApiAccess)).getTimeStep();
	}
}
