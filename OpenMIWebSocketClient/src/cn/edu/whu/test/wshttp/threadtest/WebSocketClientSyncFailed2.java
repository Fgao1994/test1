package cn.edu.whu.test.wshttp.threadtest;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import cn.edu.whu.test.wshttp.MyHttpClient;
import cn.edu.whu.test.wshttp.ValueSent;

/*
 * Failed
 * 在wc.send(str)和flag = "true";语句之间接收到了onMessage消息，所以变为True之后，不会再调用OnMessage方法，也就是不会再变为False了，一直continue
 * 对方法，采用Synchronize，也会出现类似问题。
 *
 接收：false
发送：value=value_no_i285
发送前false
发送后true
Websocket Recieve message:value=value_no_i285 1467169757556
接收：false
发送：value=value_no_i286
发送前false
Websocket Recieve message:value=value_no_i286 1467169757556
接收：false
发送后true
 */
public class WebSocketClientSyncFailed2 extends WebSocketClient {
	static final int num = 10000;
	public static String sendText = "value="+ValueSent.STR_10B;
	long startTime = System.currentTimeMillis();
	private int count = 0;
	private static  String flag = "false";
	private URI serverUri = null;
	private Draft draft = null;
	private WebSocketClient wc = null;
	public WebSocketClientSyncFailed2(URI serverUri, Draft draft) {
		super(serverUri, draft);
		this.serverUri = serverUri;
		this.draft = draft;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onClose(int arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		System.out.println("Quit the connnection.");
		long endTime = System.currentTimeMillis();
		System.out.println(num + "次交互 spendTime " + (endTime - startTime));
	}

	@Override
	public void onError(Exception arg0) {
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
		this.sendRecieve(false, message);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		// TODO Auto-generated method stub
		System.out.println(handshakedata.getHttpStatusMessage());
		System.out.println("Open the connection from the client.");
		startTime = System.currentTimeMillis();
	}

	public synchronized void sendRecieve(boolean send,String message){
		//如果是发送数据
		if (send) {
			System.out.println("发送："+message);
			System.out.println("发送前"+flag);
			this.wc.send(message);
			flag = "true";
			System.out.println("发送后"+flag);
		}else {
			//如果是接收数据
			count++;
			System.out.println("Websocket Recieve message:" + message + " "+ System.currentTimeMillis());
			if (count == num) {
				this.close();
			}
			flag = "false";
			System.out.println("接收："+flag);
		}
	}
	public  void execute() {
		wc = new WebSocketClientSyncFailed2(this.serverUri, new Draft_17());
		try {
			wc.connectBlocking();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 0; i < num;) {
			if (flag.equals("true")) {
				continue;
			}
			this.sendRecieve(true,  sendText + i);
			i++;
		}

	}

	
	public static void main(String[] args) {
//		execute();
		String url = "ws://localhost:8080/OpenMIWebSocketServer/ws/test/12";
		WebSocketClientSyncFailed2 client = null;
		try {
			client = new WebSocketClientSyncFailed2(new URI(url), new Draft_17());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.execute();
	}
}
