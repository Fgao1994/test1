package cn.edu.whu.openmi.models.data;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class PrecipitationModel extends LinkableEngine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public PrecipitationModel(){
		//add by zmd 2015.04.07
		super();
		this.setEngineApiAccess();
	}
	@Override
	protected void setEngineApiAccess() {
		this.engineApiAccess = new PrecipitationEngine();
	}

	public static void main(String[] args){
		PrecipitationModel model = new PrecipitationModel();
		System.out.println(model);
		model.getInputExchangeItemCount();
	}
}
