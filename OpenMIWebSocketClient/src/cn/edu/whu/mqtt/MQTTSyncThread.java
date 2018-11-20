package cn.edu.whu.mqtt;

//import cn.edu.whu.wssync.SyncMessage;
//import cn.edu.whu.wssync.WebSocketSyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;


/**
 * 用于发送消息的线程SendThread
 * 个人理解：SendThread一直处于wait状态，直到SyncMqttTopModelEngine类get到message后，notify阻塞的线程(SendThread开始执行，然而ReceiveThread仍处
 * 于阻塞)，开始发送消息，此时ReceiveThread处于阻塞状态，发送完消息的SendThread也处于阻塞状态，消息回来后notify阻塞线程，再次开始发送
 *@author fgao 
 * 
 * 
 */

public class MQTTSyncThread extends Thread{
	private MqttClient Client = null;
	private SyncMessage syncMsg;
	private boolean stopped = false;
	public  Object LOCK = new Object();
	public int qos = 2;
	
	private int counter = 0;
	//public String t
	public MQTTSyncThread(MqttClient syncClient,SyncMessage syncMsg){
		this.Client = syncClient;
		this.syncMsg = syncMsg;
	}
	
	
	@Override
	public void run() {
		//while循环保证线程一直运行
		while (!stopped) {
			 synchronized (LOCK) {
				 //等待更新SyncMessage内容
				 try {
					LOCK.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
				 try {
					 this.syncMsg.getMsg().setQos(qos);
					 this.Client.publish(this.syncMsg.getTopic(),this.syncMsg.getMsg());
					 //Thread.sleep(10);
					 
					
					 System.out.println("");
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
				 //测试正确性
				// System.out.println("+++++++实际发送："+this.syncMsg.getMsg());
				// System.out.println("************第"+(counter++)+"发送************");
				 
			}
		 }
		
		System.out.println("The sending thread is stopped.");
	}
	
	public void stopSend(){
		this.stopped = true;
	}

}
