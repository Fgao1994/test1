package cn.edu.whu.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import jdk.nashorn.internal.ir.Flags;


/**
 *连接，sub，pub，断开，获取服务器返回的message
 *@author fgao 
 * 
 */
public class SubPubMessage {
	
	private static String topic;
	private static int qos;
	private static String broker;
	private static String clientId;
	private static MqttClient client;
    private final static short KEEP_ALIVE = 60;
    
    public void setTopic(String topic){
    	this.topic = topic;
    } 
    public String getTopic(){
    	return topic;
    }
    
    public void setQos(int qos){
    	this.qos = qos;
    }
    public int getQos(){
    	return qos;
    }
    
    public void setBroker(String broker){
    	this.broker = broker;
    }
    public String getBroker(){
    	return broker;
    }
    
    public void setClientId(String clientId){
    	this.clientId = clientId;
    }
    public String getClientId(){
    	return clientId;
    }
    
    public static String getMessage(MqttClient sampleClient, String topic, String payload, int qos){
    	
    	try {
    		
            //confirm the callback
            CallBack callback = new CallBack();
        	sampleClient.setCallback(callback);
        
        	/**********subscribe**********/
        	
        	sampleClient.subscribe(topic,qos);
        		//Thread.sleep(1000);
			
        	
        	/**********publish*********/
        	String trMessage = null;
           
         	   /*if (CallBack.flag == true){
                	try {
                		CallBack.flag = false;
                		System.out.println("Client sended message: "+payload);    
                		MqttMessage message = new MqttMessage(payload.getBytes());
                		message.setQos(qos);
                     
                		sampleClient.publish(topic, message);
                     
                     
                		//睡眠等待返回
                		Thread.sleep(10);
                		
                		trMessage = callback.getMessage();
                		System.out.println(trMessage);
        			} catch (Exception e) {
        				// TODO: handle exception
        			}
                }*/
        	try {
        		//CallBack.flag = false;
        		
        		//System.out.println("Client sended message: "+payload);    
        		MqttMessage message = new MqttMessage(payload.getBytes());
        		message.setQos(qos);
             
        		sampleClient.publish(topic, message);
             
             
        		//睡眠等待返回
        		Thread.sleep(10);
        		
        		trMessage = callback.getMessage();
        		//System.out.println(trMessage);
             
        	
         	  return(trMessage);
            //System.out.println("Message published");
            
            //sampleClient.disconnect();
            //System.out.println("Disconnected");
          
           // System.out.println(MqttPublishSample.Remessage);
           
            //连接断开才能获得回调结果？
           
            
            //System.exit(0);
    		
    		
		} catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
            return null;
        }
    	
    }catch(Exception e){
    	e.printStackTrace();
    	return null;
    }
    
    }
}

