package cn.edu.whu.openmi.models.Hargreaves;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class HargreavesModel extends LinkableEngine{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HargreavesModel(){
		super();
		this.setEngineApiAccess();
	}
	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new HargreavesEngine();
		((HargreavesEngine)this.engineApiAccess).setBufferInfos(this.bufferInfos);
	}

}
