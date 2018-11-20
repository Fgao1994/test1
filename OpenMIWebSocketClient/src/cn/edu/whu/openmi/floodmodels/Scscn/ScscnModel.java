package cn.edu.whu.openmi.floodmodels.Scscn;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class ScscnModel extends LinkableEngine{

	private static final long serialVersionUID = 1L;
	
	public ScscnModel() {
		// TODO Auto-generated constructor stub
		super();
		this.setEngineApiAccess();
	}
	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new ScscnEngine();
		//((ScscnEngine)this.engineApiAccess).setBufferInfos(this.bufferInfos);
		//((HargreavesEngine)this.engineApiAccess).setBufferInfos(this.bufferInfos);
		
	}

}
