package cn.edu.whu.openmi.floodmodels.Scscn;

import java.util.Calendar;

public class ScscnData {
	private Calendar time;
	private double precip,runoff;
	
	public double getPrecip(){
		return precip;
	}
	
	public void setPrecip(double precip){
		this.precip = precip;
	}
	
	public double getRunoff(){
		return runoff;
	}
	
	public void setRunoff(double runoff){
		this.runoff = runoff;
	}
	
	public Calendar getTime(){
		return time;
	}
	
	public void setTime(Calendar time){
		this.time = time;
	}

}
