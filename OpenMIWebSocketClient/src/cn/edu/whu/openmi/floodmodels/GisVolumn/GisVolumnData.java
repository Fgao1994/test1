package cn.edu.whu.openmi.floodmodels.GisVolumn;

import java.util.Calendar;


public class GisVolumnData {
	private Calendar time;
	private Double elev_water;
	
	public Calendar getTime(){
		return time;
	}
	
	public void setTime(Calendar time){
		this.time = time;
	}
	
	public Double getElevWater(){
		return elev_water;
	}
	public void setElevWater(Double elev_water){
		this.elev_water = elev_water;
	}
	
}
