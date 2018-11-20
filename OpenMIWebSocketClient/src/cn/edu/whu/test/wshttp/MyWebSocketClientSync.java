package cn.edu.whu.test.wshttp;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

/*
 * Java两个线程，交替执行,实现同步。http://blog.csdn.net/keitho00/article/details/47059547
 * 用单独的线程，来发送数据，主线程用来接收数据。
 */
public class MyWebSocketClientSync extends WebSocketClient {
	static final int num = 200;
	public static String sendMsg = "value=" + ValueSent.STR_1000B;
	long startTime = System.currentTimeMillis();
	private int count = 0;
	final SendSyn sendSyn = new SendSyn(this, num);

	public MyWebSocketClientSync(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	@Override
	public void onClose(int arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		System.out.println("Quit the connnection.");
		long endTime = System.currentTimeMillis();
		//System.out.println("次数：" + num + "，时间： " + (endTime - startTime)
		//		+ "ms\n数据：" + sendMsg);
		System.out.println("次数：" + num + "，时间： " + (endTime - startTime)
				);
	}

	@Override
	public void onError(Exception arg0) {
	}

	@Override
	public void onMessage(String message) {
		synchronized (SendSyn.LOCK) {
			// 验证程序的正确性
//			System.out.println("Recieve-" + count + ":" + message + " "+ System.currentTimeMillis());
			count++;
			if (count == num) {
				this.close();
			}
			SendSyn.LOCK.notify();
		}
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		// TODO Auto-generated method stub
		System.out.println(handshakedata.getHttpStatusMessage());
		System.out.println("Open the connection from the client.");
		// startTime = System.currentTimeMillis();
	}

	public void execute() {
		try {
			this.connectBlocking();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Thread sendThread = new Thread(new Runnable() {
			@Override
			public void run() {
				sendSyn.send(sendMsg);
			}
		});
		sendThread.run();
	}

	public static void main(String[] args) {
		String url = "ws://192.168.73.150:8080/OpenMIWebSocketServer/ws/test/12";
		MyWebSocketClientSync client = null;
		try {
			client = new MyWebSocketClientSync(new URI(url), new Draft_17());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		client.execute();
	}
	
}
