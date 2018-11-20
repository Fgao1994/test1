package cn.edu.whu.openmi.smw.examples;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class SimpleWrapperComponet1_2 extends LinkableEngine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SimpleWrapper1_2 engine = null;
	public SimpleWrapperComponet1_2(){
		this.engine = new SimpleWrapper1_2();
	}
	@Override
	protected void setEngineApiAccess() {
		if (this.engineApiAccess == null) {
			this.engineApiAccess = new SimpleWrapper1_2();
		}else {
			this.engineApiAccess = this.engine;
		}
	}

}
