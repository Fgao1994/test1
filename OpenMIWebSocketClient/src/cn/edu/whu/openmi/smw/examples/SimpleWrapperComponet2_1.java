package cn.edu.whu.openmi.smw.examples;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class SimpleWrapperComponet2_1 extends LinkableEngine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public SimpleWrapperComponet2_1(){
	}
	@Override
	protected void setEngineApiAccess() {
		this.engineApiAccess = new SimpleWrapper2_1();
	}

}
