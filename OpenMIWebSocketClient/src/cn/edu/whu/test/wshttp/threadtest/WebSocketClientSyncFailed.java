package cn.edu.whu.test.wshttp.threadtest;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

/*
 * Failed
 * 在wc.send(str)和flag = "true";语句之间接收到了onMessage消息，所以变为True之后，不会再调用OnMessage方法，也就是不会再变为False了，一直continue
 * 
 */
public class WebSocketClientSyncFailed extends WebSocketClient {
	static final int num = 50000;
	long startTime = System.currentTimeMillis();
	private int count = 0;
	private static String flag = "false";

	public WebSocketClientSyncFailed(URI serverUri, Draft draft) {
		super(serverUri, draft);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
		// System.out.println(message);
		count++;
		System.out.println("Websocket Recieve message:" + message + " "
				+ System.currentTimeMillis());
		if (count == num) {
			this.close();
		}
		flag = "false";
		System.out.println("接收："+flag);

	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		// TODO Auto-generated method stub
		System.out.println(handshakedata.getHttpStatusMessage());
		System.out.println("Open the connection from the client.");
		startTime = System.currentTimeMillis();
	}

	public static void execute() {

		String url = "ws://localhost:8080/OpenMIWebSocketServer/ws/test/12";
		WebSocketClient wc = null;
		try {
			wc = new WebSocketClientSyncFailed(new URI(url), new Draft_17());
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

		for (int i = 0; i < num;) {
			if (flag.equals("true")) {
				continue;
			}
			System.out.println("发送请求" + i);
			String str = "value=value_no_i" + i;
			System.out.println("发送前"+flag);
			wc.send(str);
			flag = "true";
			System.out.println("发送后"+flag);
			i++;
		}

	}

	
	public static void main(String[] args) {
		execute();
		/*String url = "ws://localhost:8080/OpenMIWebSocketServer/ws/test/12";
		MyWebSocketClientSync client = null;
		try {
			client = new MyWebSocketClientSync(new URI(url), new Draft_17());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.execute();*/
	}
}
