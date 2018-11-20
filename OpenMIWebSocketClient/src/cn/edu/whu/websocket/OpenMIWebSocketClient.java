package cn.edu.whu.websocket;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

/**
 * @author zhangmingda
 *
 */
public class OpenMIWebSocketClient  {
	private WebSocketClient wc = null;
	private String wsUrl = null;
	private String name = "OpenMIWebSocketClient";

	public OpenMIWebSocketClient(String url) {
		this.wsUrl = url;
		try {
			this.wc = new WebSocketClient(new URI(url), new Draft_17()) {

				@Override
				public void onOpen(ServerHandshake handshakedata) {
					System.out.println(handshakedata.getHttpStatusMessage());
					System.out.println("Open the connection from the client.");
				}

				@Override
				public void onMessage(String message) {
					// System.out.println(message);
					System.out.println("the client named "+ name +" recieve message:" + message);
				}

				@Override
				public void onError(Exception ex) {
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					System.out.println("Quit the connnection.");
				}
			};
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			this.wc = null;
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean connectBlocking() {
		if (this.wc == null) {
			return false;
		}
		boolean flag = false;
		try {
			flag = this.wc.connectBlocking();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	public boolean sendText(String text) {
		if (this.wc == null) {
			return false;
		}
		this.wc.send(text);
		return true;
	}

	public String getUrl() {
		return this.wsUrl;
	}

	public boolean close() {
		if (this.wc == null)
			return false;

		try {
			this.wc.closeBlocking();
			return true;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
//		this.wc.close();
	}

	public static void main(String[] args){
		String url1 = "ws://localhost:8080/OpenMIWebSocketServer/ws/topmodel?p1=parameter1&p2=parameter2";
		OpenMIWebSocketClient client1 = new OpenMIWebSocketClient(url1);
		client1.setName("topmodel");

		if (!client1.connectBlocking()) {
			System.err.println(client1.getName()+"远程连接失败");
			return;
		}
		
		for (int i = 0; i < 5; i++) {
			client1.sendText(client1.getName() + i);
		}

		client1.close();
	}
	public static void main2(String[] args) {
		String url1 = "ws://localhost:8080/OpenMIWebSocketServer/ws/models/topmodel";
		String url2 = "ws://localhost:8080/OpenMIWebSocketServer/ws/models/hargreaves";
		OpenMIWebSocketClient client1 = new OpenMIWebSocketClient(url1);
		client1.setName("topmodel");

		OpenMIWebSocketClient client2 = new OpenMIWebSocketClient(url2);
		client2.setName("Hargreaves");

		if (!client1.connectBlocking()) {
			System.err.println(client1.getName()+"远程连接失败");
			return;
		}
		
		if (!client2.connectBlocking()) {
			System.err.println(client2.getName()+"连接失败");
			return;
		}
		
		
		for (int i = 0; i < 5; i++) {
			client1.sendText(client1.getName() + i);
			client2.sendText(client2.getName() + i);
		}

		client1.close();
		client2.close();

	}
}
