package cn.edu.whu.openmi.models.hargreaves2;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class Hargreaves extends LinkableEngine{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Hargreaves(){
		super();
		this.setEngineApiAccess();
	}
	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new HargreavesEngine();
	}

}
