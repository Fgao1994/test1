package cn.edu.whu.openmi.floodmodels.GisVolumn;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class GisVolumnModel extends LinkableEngine{
	
	private static final long serialVersionUID = 1L;
	
	public GisVolumnModel(){
		super();
		this.setEngineApiAccess();
	}

	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new GisVolumnEngine();
		
	}
	
	public double getTimeStep(){
		return ((GisVolumnEngine)(this.engineApiAccess)).getTimeStep();
	}

}
