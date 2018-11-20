package cn.edu.whu.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

public class SyncSubReceive {
	
	private SyncMqttTopModelEngine engine;
	public int qos;
	public MqttClient sampleClient;
	public String topic;
	
	public SyncSubReceive(SyncMqttTopModelEngine engine,MqttClient sampleClient,String topic,int qos){
		
		this.engine = engine;
		this.sampleClient = sampleClient;
		this.topic = topic;
		this.qos = qos;
	}
	
	public void getMessage(){
		
		try {
			synchronized(this.engine.ENGINE_LOCK){
				
				CallBack callback = new CallBack();
	        	sampleClient.setCallback(callback);
	        	
				sampleClient.subscribe(topic,qos);
				
				//SyncMqttTopModelEngine.httpUuid = 
				double[] out = {Double.parseDouble(callback.getMessage())};
				this.engine.setValues("Runoff", "Coweeta Runoff",
						new ScalarSet(out));
				SyncMqttTopModelEngine.returned = true;
				
				//测试程序正确性
//				System.out.println("------------第"+(counter++)+"次接收------------");
				
				this.engine.ENGINE_LOCK.notify();
				//return callback.getMessage();
				
				
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
			//return null;
		}
		
		
		
	}
	

}
