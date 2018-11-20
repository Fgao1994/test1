package cn.edu.whu.mqtt;


import org.eclipse.paho.client.mqttv3.MqttMessage;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;


/**
 *消息回调 
 * @author fgao
 */
public class SyncCallBack implements MqttCallback{

	
	public static boolean flag = true;
	public String Remessage = null;
	public SyncMqttTopModelEngine engine;
	
	public int counter = 0;
	public SyncCallBack(SyncMqttTopModelEngine engine){
		
		this.engine = engine;
		
	}
	
	@Override
	 public void messageArrived(String topic, MqttMessage message) {   
	        
		//setMessage(message.toString());
		//System.out.println("------------第"+(counter++)+"次接收------------");
		synchronized (this.engine.ENGINE_LOCK) {
			
			try {   
	           // System.out.println("Server returned message:"+message.toString());  
	            //this.Remessage = message.toString();
	            setMessage(message.toString());
	            //this.counter++;
	            
	            if (this.Remessage != null) {
	            	SyncMqttTopModelEngine.returned = true;
	            	SyncCallBack.flag = true;
	            	double[] out = {Double.parseDouble(this.Remessage)};
					this.engine.setValues("Runoff", "Coweeta Runoff",
							new ScalarSet(out));
					
					
				}
	            //System.out.println(this.Remessage);
	        } catch (Exception e) {   
	            e.printStackTrace();   
	        }   
			
			//测试程序正确性
			
			
			this.engine.ENGINE_LOCK.notify();
			
			
			
		}
		
	    }
	    @Override
	    public void connectionLost(Throwable cause) {  
	          
	    }   
	    @Override
	    public void deliveryComplete(IMqttDeliveryToken token) {  
	          
	    }  
	    
	    public void setMessage(String message){
	    	this.Remessage = message;
	    }
	    public String getMessage() {
	    	return this.Remessage;
			
		}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	
/*	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}*/

}
