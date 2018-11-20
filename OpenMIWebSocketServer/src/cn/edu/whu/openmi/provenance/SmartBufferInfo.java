package cn.edu.whu.openmi.provenance;

import nl.alterra.openmi.sdk.buffer.SmartBuffer;

import org.openmi.standard.ITime;
/**
 * 20160114,保存溯源信息
 * @author zhangmingda
 *
 */
public class SmartBufferInfo {
	private String linkId;
	private ITime  time;
	private SmartBuffer smartBuffer;
	public String getLinkId() {
		return linkId;
	}
	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}
	public ITime getTime() {
		return time;
	}
	public void setTime(ITime time) {
		this.time = time;
	}
	public SmartBuffer getSmartBuffer() {
		return smartBuffer;
	}
	public void setSmartBuffer(SmartBuffer smartBuffer) {
		this.smartBuffer = smartBuffer;
	}
	
	
}
