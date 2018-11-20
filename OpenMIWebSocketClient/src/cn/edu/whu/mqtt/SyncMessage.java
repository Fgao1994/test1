package cn.edu.whu.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * 线程之间同步发送的数据
 * 简单实现
 * @author fgao
 *
 */


public class SyncMessage {
	private MqttMessage msg = null;
	private String topic;
	//private int qos;
	
	public MqttMessage getMsg() {
		return msg;
	}

	public void setMsg(MqttMessage msg) {
		this.msg = msg;
	}
	
	public String getTopic(){
		return topic;
	}
	
	public void setTopic(String topic){
		this.topic = topic;
	}
}
