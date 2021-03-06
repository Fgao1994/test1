package cn.edu.whu.httpmodel;

import nl.alterra.openmi.sdk.wrapper.LinkableEngine;

public class HttpTopModel extends LinkableEngine{

	/**
	 * 
	 */
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
	public final static String PET_Value_KEY = "PET_Value";
	public final static String Precip_Value_KEY = "Precip_Value";
	
	public final static String UUIDKEY= "uuid";
	public HttpTopModel(){
		super();
		this.setEngineApiAccess();
	}
	@Override
	protected void setEngineApiAccess() {
		// TODO Auto-generated method stub
		this.engineApiAccess = new HttpTopModelEngine();
		((HttpTopModelEngine)this.engineApiAccess).setBufferInfos(this.bufferInfos);
	}

	//add by zmd,2015-03-11
	public double getTimeStep(){
		return ((HttpTopModelEngine)(this.engineApiAccess)).getTimeStep();
	}
}
