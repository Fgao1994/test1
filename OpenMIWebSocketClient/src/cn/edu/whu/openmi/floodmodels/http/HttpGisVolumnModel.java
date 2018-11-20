package cn.edu.whu.openmi.floodmodels.http;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class HttpGisVolumnModel extends LinkableEngine{
	
	private static final long serialVersionUID = 1L;
	public final static String QUANTITYKEY = "quantity";
	public final static String ELEMENTYKEY = "element";
	public final static String TIMEKEY = "time";
	public final static String METHODKEY = "method";
	public final static String VALUEKEY = "value";
	public final static String TIMEFORMAT = "yyyy-MM-dd hh:mm:ss";
	public final static String Method_Initialize= "initialize";
	public final static String Method_getvalue = "getvalue";
	public final static String Method_Finish = "finish";
	public final static String Flood_Volumn_KEY = "Flood_Volumn";
	public final static String UUIDKEY= "uuid";
	public HttpGisVolumnModel() {
		// TODO Auto-generated constructor stub
		super();
		this.setEngineApiAccess();
	}
	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new HttpGisVolumnEngine();
		
	}
	
	public double getTimeStep(){
		return ((HttpGisVolumnEngine)(this.engineApiAccess)).getTimeStep();
	}

}
