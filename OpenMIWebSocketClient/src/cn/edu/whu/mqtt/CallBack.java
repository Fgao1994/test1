package cn.edu.whu.mqtt;


import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;


/**
 *消息回调 
 * @author fgao
 */
public class CallBack implements MqttCallback{

	
	public static boolean flag = true;
	public String Remessage = null;
	private int counter = 0;
	public CallBack(){
		
	}
	
	@Override
	 public void messageArrived(String topic, MqttMessage message) {   
	        
		//setMessage(message.toString());
		try {   
	            //System.out.println("Server returned message:"+message.toString());  
	            //this.Remessage = message.toString();
	           // System.out.println("------------第"+(counter++)+"次接收------------");
	            setMessage(message.toString());
	            if (this.Remessage != null) {
	            	SyncMqttTopModelEngine.returned = true;
	            	this.flag = true;
					
				}
	            //System.out.println(this.Remessage);
	        } catch (Exception e) {   
	            e.printStackTrace();   
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
