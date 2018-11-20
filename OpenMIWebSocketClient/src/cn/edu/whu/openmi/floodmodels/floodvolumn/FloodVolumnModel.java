package cn.edu.whu.openmi.floodmodels.floodvolumn;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class FloodVolumnModel extends LinkableEngine{

	private static final long serialVersionUID = 1L;
	
	public FloodVolumnModel(){
		super();
		this.setEngineApiAccess();
	}
	
	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new FloodVolumnEngine();
		//((FloodVolumnEngine)this.engineApiAccess).setBufferInfos(this.bufferInfos);
	}

}
