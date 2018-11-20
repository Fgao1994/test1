package cn.edu.whu.openmi.models.topmodel;

import java.util.Calendar;

/**
 * 用来存储TOPMODEL处理的结果
 * 
 * @author zhangmingda
 *
 */
public class TopModelData {
	private double precip, pet, runoff, streamFlow;
	private Calendar time;

	
	public double getPrecip() {
		return precip;
	}

	public void setPrecip(double precip) {
		this.precip = precip;
	}

	public double getPet() {
		return pet;
	}

	public void setPet(double pet) {
		this.pet = pet;
	}

	public double getRunoff() {
		return runoff;
	}

	public void setRunoff(double runoff) {
		this.runoff = runoff;
	}

	public double getStreamFlow() {
		return streamFlow;
	}

	public void setStreamFlow(double streamFlow) {
		this.streamFlow = streamFlow;
	}

	public Calendar getTime() {
		return time;
	}

	public void setTime(Calendar time) {
		this.time = time;
	}

}
