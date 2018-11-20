package cn.edu.whu.wssync;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import cn.edu.whu.openmi.models.data.PrecipitationEngine;
import cn.edu.whu.openmi.models.topmodel.TopModelData;
import cn.edu.whu.openmi.smw.SimpleWrapper;
import cn.edu.whu.websocket.WSModelUtils;


public class WSSyncTopModelEngine extends SimpleWrapper {

	public final static String PET_Value_KEY = "PET_Value";
	public final static String Precip_Value_KEY = "Precip_Value";
	public final static String RunOff_Value_KEY = "Runoff_Value";
	private WebSocketClient wc = null;
	public Object ENGINE_LOCK = new Object();
	public boolean returned = false;
//	private String wsUrl = "ws://localhost:8080/OpenMIWebSocketServer/ws/topmodel2/topmodel?";
	private String wsUrl = "ws://192.168.23.136:8080/OpenMIWebSocketServer/ws/topmodel2/topmodel?";
//	private String wsUrl = "ws://202.114.118.190:8086/OpenMIWebSocketServer/ws/topmodel2/topmodel?";
	private String name = "OpenMIWebSocketClient";
	private boolean flag = false;
	Map<Calendar, TopModelData> outputValues = new LinkedHashMap<Calendar, TopModelData>();
//	private WSSyncLock syncLock = null;
	private WSSyncThread wsSyncThread = null;
//	private Object objLock = new Object();
	private SyncMessage syncMsg = new SyncMessage();
	private WebSocketSyncClient syncClient = null;
	public WSSyncTopModelEngine() {
		InputStream inConfig = PrecipitationEngine.class
				.getClass()
				.getResourceAsStream(
						"/cn/edu/whu/openmi/models/topmodel/Topmodel-Config.xml");
		this.setVariablesFromConfigFile(inConfig);
	}

	@Override
	public void initialize(HashMap properties) {
		// TODO Auto-generated method stub
		super.initialize(properties);
		setInitialized(true);
		int i = 0;
		Iterator iter = properties.entrySet().iterator();
		// kvp格式传递时间独立参数
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			if (i == 0) {
				this.wsUrl += key + "=" + val;
			} else {
				this.wsUrl += "&" + key + "=" + val;
			}
			i++;
		}
         //发起连接
		try {
			syncClient = new WebSocketSyncClient(new URI(this.wsUrl), new Draft_17(),this);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (this.syncClient!=null) {
			try {
				this.syncClient.connectBlocking();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//实例化线程，并启动
		this.wsSyncThread = new WSSyncThread(this.syncClient, syncMsg);
		this.wsSyncThread.start();
	}

	// 由该方法驱动，所以sendText放到该方法中。
	@Override
	public boolean performTimeStep() {
		this.returned = false;
		synchronized (this.wsSyncThread.LOCK) {
			// 在配置文件中，Precipitation必须放在第一位，然后是pet
			ScalarSet input_precip = (ScalarSet) this.getValues(
					"Precipitation", "Coweeta Precipitation");
			ScalarSet input_pet = (ScalarSet) this.getValues("PET", "Coweeta");

			String getReq = WSModelUtils.METHODKEY + "=get&" + PET_Value_KEY
					+ "=" + input_pet.get(0) + "&" + Precip_Value_KEY + "="
					+ input_precip.get(0);

			// 同步发送的数据
			//syncMsg.setMsg(getReq);
			syncMsg.setMsg(WSModelUtils.appendToByte(getReq, 1000));
			//System.out.println(WSModelUtils.appendToByte(getReq, 300).getBytes().length);
			
			//测试发送的数据是否符合预期
//			System.out.println("+++++++预期发送："+getReq);
			
			// 可以发送数据了
			this.wsSyncThread.LOCK.notify();
		}
		
		synchronized (this.ENGINE_LOCK) {
			//如果还没有返回则等待
			if (!this.returned) {
				try {
					this.ENGINE_LOCK.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		try {
			this.syncClient.closeBlocking();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//结束发送消息的线程,先Stop的；运行时结束时，会提示Connection已断开，还是发送消息，暂不处理
		/*this.wsSyncThread.stopSend();
		synchronized (this.wsSyncThread.LOCK) {
			this.wsSyncThread.LOCK.notify();
		}*/
	}

	// 直接用来测试，单个交互过程
	public static void main(String[] args) {
		HashMap<String, String> map = new HashMap<String, String>();
		// map.put("ConfigFile", "H:/workspace/topmodel/configTest2.xml");
		map.put("TI", "F:/workspace/openmi/TI_raster.txt");
		map.put("m", "180");
		map.put("Tmax", "250000");
		map.put("Interception", "3");
		map.put("R", "9.66");
		map.put("WatershedArea_SquareMeters", "0");
		WSSyncTopModelEngine engine = new WSSyncTopModelEngine();
		engine.initialize(map);
		for (int i = 0; i < 100000000; i++) {
			engine.performTimeStep();
		}
	}

	@Override
	public String getComponentID() {
		// TODO Auto-generated method stub
		return "TOPMODEL";
	}

}
