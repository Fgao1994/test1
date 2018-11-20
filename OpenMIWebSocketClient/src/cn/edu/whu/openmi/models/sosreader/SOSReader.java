package cn.edu.whu.openmi.models.sosreader;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class SOSReader extends LinkableEngine{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SOSReader(){
		super();
		this.setEngineApiAccess();
	}
	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new SOSReaderEngine();
	}

}

