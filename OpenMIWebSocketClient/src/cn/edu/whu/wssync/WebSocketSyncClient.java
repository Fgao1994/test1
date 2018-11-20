package cn.edu.whu.wssync;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import cn.edu.whu.test.wshttp.SendSyn;
import cn.edu.whu.test.wshttp.ValueSent;
import cn.edu.whu.websocket.WSModelUtils;

/*
 * Java两个线程，交替执行,实现同步。http://blog.csdn.net/keitho00/article/details/47059547
 * 用单独的线程，来发送数据，主线程用来接收数据。
 */
public class WebSocketSyncClient extends WebSocketClient {
	public static String sendMsg = "value=" + ValueSent.STR_10B;
	long startTime = System.currentTimeMillis();
	private WSSyncTopModelEngine wsSyncTopModelEngine;
	private int counter = 0;

	public WebSocketSyncClient(URI serverUri, Draft draft,WSSyncTopModelEngine engine) {
		super(serverUri, draft);
		this.wsSyncTopModelEngine = engine;
	}

	@Override
	public void onClose(int arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		System.out.println("Quit the connnection.");
		long endTime = System.currentTimeMillis();
//		System.out.println("次数：" + "，时间： " + (endTime - startTime)+ "ms\n数据：" + sendMsg);
	}

	@Override
	public void onError(Exception arg0) {
	}

	@Override
	public void onMessage(String message) {
		synchronized (this.wsSyncTopModelEngine.ENGINE_LOCK) {
			//System.out.println(message);
			Map<String, String> map = WSModelUtils.parseArguments(message);
			String method = map.get(WSModelUtils.METHODKEY);
			if (!method.equals("set")) {
				return;
			}
			String value = map.get(WSSyncTopModelEngine.RunOff_Value_KEY);
			if (value.equalsIgnoreCase("novalue")
					|| value.equalsIgnoreCase("null"))
				value = "0";

			double d1 = 0;
			try {
				d1 = Double.parseDouble(value);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			double[] val = { d1 };
			this.wsSyncTopModelEngine.setValues("Runoff", "Coweeta Runoff",
					new ScalarSet(val));
			this.wsSyncTopModelEngine.returned = true;
			
			//测试程序正确性
//			System.out.println("------------第"+(counter++)+"次接收------------");
			
			this.wsSyncTopModelEngine.ENGINE_LOCK.notify();
		}
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		System.out.println(handshakedata.getHttpStatusMessage());
		System.out.println("Open the connection from the client.");
	}

	public static void main(String[] args){
		String url = "ws://localhost:8080/OpenMIWebSocketServer/ws/topmodel2/topmodel?WatershedArea_SquareMeters=124800&Interception=3&TI=F:/workspace/openmi/TI_raster.txt&R=9.66&OutputData=F:/workspace/openmi/Topmodel_output.txt&ProvOut=F:/workspace/openmi/Topmodel_prov.txt&m=90&Tmax=240000";
		try {
			WebSocketSyncClient client = new WebSocketSyncClient(new URI(url), new Draft_17(),new WSSyncTopModelEngine());
			try {
				client.connectBlocking();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			client.send("string");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
