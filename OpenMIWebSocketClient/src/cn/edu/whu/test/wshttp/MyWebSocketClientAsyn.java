package cn.edu.whu.test.wshttp;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

public class MyWebSocketClientAsyn extends WebSocketClient{
	static final int num =1500;
	public static String sendMsg = ValueSent.STR_1000B;
	long startTime = System.currentTimeMillis();
	private int count = 0;
	public MyWebSocketClientAsyn(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	@Override
	public void onClose(int arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		System.out.println("Quit the connnection.");
		long endTime = System.currentTimeMillis();
		System.out.println("次数："+count+ "，时间："+ (endTime-startTime)+"ms\n数据："+sendMsg);
	}

	@Override
	public void onError(Exception arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMessage(String message) {
		//验证程序的正确性
//		System.out.println("receive-"+count+":"+message);
		count ++;
		if (count == num) {
			this.close();
		}
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		// TODO Auto-generated method stub
//		System.out.println(handshakedata.getHttpStatusMessage());
		System.out.println("Open the connection from the client.");
//		startTime = System.currentTimeMillis();
	}

	public static void main(String[] args){
		String url = "ws://localhost:8080/OpenMIWebSocketServer/ws/test/12";
		WebSocketClient wc = null;
		try {
			wc = new MyWebSocketClientAsyn(new URI(url), new Draft_17());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			wc.connectBlocking();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (int i=0; i < num; i++) {
			//验证程序的正确性
//			System.out.println("send-"+i+":"+sendMsg);
			String str = sendMsg;
			wc.send(str);
		}
	}
}
