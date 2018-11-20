package cn.edu.whu.test.wshttp;

import org.java_websocket.client.WebSocketClient;

public class SendSyn {
	 public static Object LOCK = new Object();  
	 volatile boolean bShouldSub = true;//这里相当于定义了控制该谁执行的一个信号灯  
	 int num= 10;
	 private WebSocketClient ws = null;
	 public SendSyn(WebSocketClient ws,int num){
		 this.ws = ws;
		 this.num = num;
	 }
	 public void send(String message){
		 for(int i=0;i<num;i++){
			 synchronized (LOCK) {
				 ws.send(message+i);
				 
				 //验证程序的正确性
//				 System.out.println("Send-"+i+"："+message);
				 try {
					LOCK.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		 }
	 }
}
