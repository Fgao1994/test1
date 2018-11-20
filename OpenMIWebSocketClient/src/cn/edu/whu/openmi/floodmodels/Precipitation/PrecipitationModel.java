package cn.edu.whu.openmi.floodmodels.Precipitation;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

/**
 *@author FGao 
 * 
 */

public class PrecipitationModel extends LinkableEngine{
	private static final long serialVersionUID = 1L;

	public PrecipitationModel() {
		// TODO Auto-generated constructor stub
		super();
		this.setEngineApiAccess();
	}
	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new PrecipitationEngine();
		
	}
	
	public static void main(String[] args){
		PrecipitationModel model = new PrecipitationModel();
		System.out.println(model);
		model.getInputExchangeItemCount();
		
	}
	

}
