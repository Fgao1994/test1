package cn.edu.whu.websocket.models;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class WebSocketTopModel extends LinkableEngine{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WebSocketTopModel(){
		super();
		this.setEngineApiAccess();
	}
	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new WebSocketTopModelEngine();
	}

}
