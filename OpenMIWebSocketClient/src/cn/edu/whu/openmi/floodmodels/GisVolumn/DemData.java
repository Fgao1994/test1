package cn.edu.whu.openmi.floodmodels.GisVolumn;

import java.util.ArrayList;

public class DemData {
	
	public Double elevMin , elevMax , scaleX, scaleY;
	public Double[][] demValue;
	
	public DemData(){
	}
	
	public DemData(Double[][] array){
		new DemData(array);
	}
	
	public Double[][] getDemValue(){
		return demValue;
	}
	//?
	public void setDemValue(Double[][] demValue){
		this.demValue = demValue;
	}
	
	public Double getElevMin(){
		return elevMin;
	}
	
	public void setElevMin(Double elevMin){
		this.elevMin = elevMin;
	}
	
	public Double getElevMax(){
		return elevMax;
	}
	
	public void setElevMax(Double elevMax){
		this.elevMax = elevMax;
	}
	
	public Double getScaleX(){
		return scaleX;
	}
	
	public void setScaleX(Double scaleX){
		this.scaleX = scaleX;
	}
	
	public Double getScaleY(){
		return scaleY;
	}
	
	public void setScaleY(Double scaleY){
		this.scaleY = scaleY;
	}

}
