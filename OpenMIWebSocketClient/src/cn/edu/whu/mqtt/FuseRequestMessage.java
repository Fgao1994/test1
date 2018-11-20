package cn.edu.whu.mqtt;

import javax.print.attribute.standard.RequestingUserName;

/*import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;*/
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;  
import org.fusesource.mqtt.client.QoS;  
import org.fusesource.mqtt.client.Topic;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.org.apache.regexp.internal.recompile; 

public class FuseRequestMessage {	
	private static Topic topic;
	private static int qos;
	private static String broker;
	private static String clientId;
//	private static MQTT client;
	
    public final static short KEEP_ALIVE = 30;
    public final static boolean CLEAN_START = true;
    public final  static long RECONNECTION_ATTEMPT_MAX=6;  
    public final  static long RECONNECTION_DELAY=2000;  
    public final static int SEND_BUFFER_SIZE=2*1024*1024;
    
    public void setTopic(Topic topic){
    	this.topic = topic;
    } 
    public Topic getTopic(){
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
    
    public static String getMessage(String clientId,String broker, String topic, String payload, int qos) throws Exception{
    	
    	MQTT mqttClient  = new MQTT();
    	mqttClient.setClientId(clientId);
    	mqttClient.setHost(broker);
		mqttClient.setCleanSession(true);
		mqttClient.setReconnectAttemptsMax(RECONNECTION_ATTEMPT_MAX);
		mqttClient.setReconnectDelay(RECONNECTION_DELAY);
		mqttClient.setKeepAlive(KEEP_ALIVE);
		mqttClient.setSendBufferSize(SEND_BUFFER_SIZE);
		BlockingConnection connection = mqttClient.blockingConnection();
		Topic[] topics = {new Topic(topic, QoS.EXACTLY_ONCE)};
		connection.connect();
    	try {
    		
    		connection.publish(topic, payload.getBytes(), QoS.EXACTLY_ONCE, true);
		
    	} catch (Exception me) {
			// TODO: handle exception
            me.printStackTrace();
			
		}
    	try {
    		byte[] qoses = connection.subscribe(topics);   		
    		while(true){
   			Message message = (Message) connection.receive();
    			byte[] Subresults = ((org.fusesource.mqtt.client.Message) message).getPayload();
    	
    			((org.fusesource.mqtt.client.Message) message).ack();
    			return Subresults.toString();
    		}
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
    	
    	
    }

}
